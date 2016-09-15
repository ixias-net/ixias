/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration.Duration

import play.api.{ Environment, Mode }
import play.api.mvc.{ RequestHeader, Result, Results }
import play.api.libs.iteratee.Execution

import ixias.model.{ Identity, Entity }
import ixias.play.api.auth.token.{ Token, AuthenticityToken, SignedToken }
import ixias.play.api.auth.container.Container
import ixias.play.api.mvc.StackActionRequest

trait AuthProfile extends Results
{
  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of user identity */
  type Id   <: Identity[_]

  /** The type of user entity */
  type User <: Entity[_]

  /** The type of authority roles */
  type Authority >: Null

  /** The key of attribute for containing required authority roles. */
  case object UserKey      extends StackActionRequest.AttributeKey[User]

  /** The key of attribute for containing user data. */
  case object AuthorityKey extends StackActionRequest.AttributeKey[Authority]

  // --[ Properties ]-----------------------------------------------------------
  /** The enviroment. */
  implicit val env: Environment

  /** Can execute program logic asynchronously */
  implicit val ctx: ExecutionContext = Execution.Implicits.trampoline

  /** The timeout value in `seconds` */
  val sessionTimeout: Duration = Duration.Inf

  /** The accessor for security token. */
  val tokenAccessor: Token

  /** The datastore for security token. */
  val datastore: Container[Id]

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Resolve user by specified user-id.
   */
  protected def resolve(uid: Id): Future[Option[User]]

  /**
   * Verifies what user are authorized to do.
   */
  protected def authorize(user: User, authority: Option[Authority]): Future[Boolean]

  /**
   * Invoked if authentication failed with the credentials provided.
   * This should only be called where an authentication attempt has truly failed
   */
  def authenticationFailed(implicit request: RequestHeader): Result

  /**
   * Invoked if authorization failed.
   * Authorization is the process of allowing an authenticated users to
   * access the resources by checking whether the user has access rights to the system.
   * Authorization helps you to control access rights by granting or
   * denying specific permissions to an authenticated user.
   */
  def authorizationFailed(user: User, authority: Option[Authority])(implicit request: RequestHeader): Result


  // --[ Methods ]--------------------------------------------------------------
  /**
   * Returns authorized user.
   */
  def loggedIn(implicit request: StackActionRequest[_]): Option[User] =
    request.get(UserKey)

  /**
   * Returns the result response.
   */
  def loggedIn[A](f: User => Result)
    (implicit request: StackActionRequest[A]): Result =
    loggedIn.fold(authenticationFailed)(f)

  /**
   * Returns the result response.
   */
  def loggedIn[A](f: User => Future[Result])
    (implicit request: StackActionRequest[A]): Future[Result] =
    loggedIn.fold(Future(authenticationFailed))(f)

  /**
   * Returns the result response of applying $f to user data
   * if the user data is nonempty. Otherwise, evaluates expression `ifEmpty`
   */
  def loggedInOrNot[A](ifEmpty: => Result)(f: User => Result)
    (implicit request: StackActionRequest[A]): Result =
    loggedIn.fold(ifEmpty)(f)

  /**
   * Returns the result response of applying $f to user data
   * if the user data is nonempty. Otherwise, evaluates expression `ifEmpty`
   */
  def loggedInOrNot[A](ifEmpty: => Result)(f: User => Future[Result])
    (implicit request: StackActionRequest[A]): Future[Result] =
    loggedIn.fold(Future(ifEmpty))(f)

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Invoke this method on login succeeded.
   */
  def loginSucceeded(uid: Id)(implicit request: RequestHeader): Future[Result]

  /**
   * Invoke this method on login succeeded.
   */
  def loginSucceeded(uid: Id)(block: AuthenticityToken => Result)(implicit request: RequestHeader): Future[Result] =
    datastore.open(uid, sessionTimeout).map { token =>
      tokenAccessor.put(token)(block(token))
    } recover { case _: Throwable => InternalServerError }

  /**
   * Invoke this method on logout succeeded.
   */
  def logoutSucceeded(uid: Id)(implicit request: RequestHeader): Future[Result]

  /**
   * Invoke this method on logout succeeded.
   */
  def logoutSucceeded(uid: Id)(block: => Result)(implicit request: RequestHeader): Future[Result] =
    tokenAccessor.extract(request) match {
      case Some(token) => datastore.destroy(token).map(_ => tokenAccessor.discard(block))
      case None        => Future.successful(tokenAccessor.discard(block))
    }

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Extract a session token in `RequestHeader`.
   */
  final def extractAuthToken(implicit request: RequestHeader): Option[AuthenticityToken] =
    (env.mode match {
      case Mode.Prod => None
      case _         => request.headers.get("TEST_AUTH_TOKEN")
    }) orElse tokenAccessor.extract(request)

  /**
   * Extract a signed session token in `RequestHeader`.
   */
  def extractSignedToken(implicit request: RequestHeader): Option[SignedToken] =
    extractAuthToken.map(Token.signWithHMAC)

  /**
   * Restore a user data by the session token in `RequestHeader`.
   */
  final def restore(implicit request: RequestHeader) : Future[(Option[User], Result => Result)] =
    extractAuthToken match {
      case None        => Future.successful(None -> identity)
      case Some(token) => (for {
        Some(uid)  <- datastore.read(token)
        Some(user) <- resolve(uid)
        _          <- datastore.setTimeout(token, sessionTimeout)
      } yield {
        Some(user) -> tokenAccessor.put(token) _
      }) recover {
        case ex: Throwable => None -> identity
      }
    }

  /**
   * Verifies what user are authenticated to do.
   */
  final def authenticate(implicit request: RequestHeader): Future[Either[Result, (User, Result => Result)]] =
    restore map {
      case (Some(user), updater) => Right(user -> updater)
      case _                     =>  Left(authenticationFailed)
    }

  /**
   * Verifies what user are authorized to do.
   */
  final def authorize(authority: Option[Authority])(implicit request: RequestHeader): Future[Either[Result, (User, Result => Result)]] =
    for {
      Some((user, updater)) <- authenticate.map(_.right.toOption)
      authorized            <- authorize(user, authority)
    } yield authorized match {
      case true  => Right(user -> updater)
      case false => Left(authorizationFailed(user, authority))
    }
}
