/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

import scala.util.{ Try, Success, Failure }
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Play
import play.api.mvc._
import ixias.model.{ Identity, Entity }
import ixias.play.api.auth.token._

trait AuthProfile extends AuthProfileLike with Results
{
  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of user identity */
  type Id   <: Identity[_]
  /** The type of user entity */
  type User <: Entity[_]
  /** The type of authority roles */
  type Authority >: Null

  /** The key of attribute for containing required authority roles. */
  case object UserKey      extends AttributeKey[User]
  /** The key of attribute for containing user data. */
  case object AuthorityKey extends AttributeKey[Authority]

  // --[ Properties ]-----------------------------------------------------------
  /** The cookie name */
  val cookieName: String = "sid"

  /** The timeout value in `seconds` */
  val sessionTimeout: Duration = Duration.Inf

  /** The accessor for security token. */
  val tokenAccessor: Token

  /** The datastore for security token. */
  val datastore: Container[Id]

  // --[ Methods ]--------------------------------------------------------------
  /** Resolve user by specified user-id. */
  def resolve(id: Id): Future[Option[User]]

  /** Verifies what user are authorized to do. */
  def authorize(user: User, authority: Option[Authority]): Future[Boolean]

  // --[ Methods ]--------------------------------------------------------------
  /** Invoke this method on login succeeded */
  def loginSucceeded(id: Id)(implicit req: RequestHeader): Future[Result]

  /** Invoke this method on logout succeeded */
  def logoutSucceeded(id: Id)(implicit req: RequestHeader): Future[Result]

  /** Invoked if authentication failed with the credentials provided.
    * This should only be called where an authentication attempt has truly failed */
  def authenticationFailed(implicit req: RequestHeader): Result

  /** Invoked if authorization failed.
    * Authorization is the process of allowing an authenticated users to
    * access the resources by checking whether the user has access rights to the system.
    * Authorization helps you to control access rights by granting or
    * denying specific permissions to an authenticated user. */
  def authorizationFailed(user: User, authority: Option[Authority])(implicit req: RequestHeader): Result
}

// Feature Template
//~~~~~~~~~~~~~~~~~~
trait AuthProfileLike { self: AuthProfile =>

  // --[ Methods ]--------------------------------------------------------------
  /** Returns authorized user. */
  def loggedIn(implicit req: ActionRequest[_]): Option[User] =
    req.get(UserKey).map(_.asInstanceOf[User])

  /** Returns the result response. */
  def loggedIn[A](f: User => Result)(implicit req: ActionRequest[A]): Result =
    loggedIn.fold(authenticationFailed)(f)

  /** Returns the result response. */
  def loggedIn[A](f: User => Future[Result])(implicit req: ActionRequest[A]): Future[Result] =
    loggedIn.fold(Future(authenticationFailed))(f)

  /** Returns the result response of applying $f to user data
    * if the user data is nonempty. Otherwise, evaluates expression `ifEmpty`*/
  def loggedInOrNot[A](ifEmpty: => Result)(f: User => Result)(implicit req: ActionRequest[A]): Result =
    loggedIn.fold(ifEmpty)(f)

  /** Returns the result response of applying $f to user data
    * if the user data is nonempty. Otherwise, evaluates expression `ifEmpty`*/
  def loggedInOrNot[A](ifEmpty: => Result)(f: User => Future[Result])(implicit req: ActionRequest[A]): Future[Result] =
    loggedIn.fold(Future(ifEmpty))(f)

  /** Extract a session token in `RequestHeader`. */
  def extractToken(implicit req: RequestHeader): Option[AuthenticityToken] =
    Play.isTest(Play.current) match {
      case false => tokenAccessor.extract(req)
      case true  => req.headers.get("TEST_AUTH_TOKEN").orElse(tokenAccessor.extract(req))
    }

  /** Extract a signed session token in `RequestHeader`. */
  def extractSignedToken(implicit req: RequestHeader): Option[SignedToken] =
    extractToken.map(Token.signWithHMAC)

  // --[ Methods ]--------------------------------------------------------------
  /** Invoke this method on login succeeded */
  final protected[auth]
  def loginSucceeded(id: Id)(f: AuthenticityToken => Result)(implicit req: RequestHeader): Future[Result] =
    datastore.open(id, sessionTimeout).map { token =>
      tokenAccessor.put(token)(f(token))
    } recover { case _: Throwable => InternalServerError }

  /** Invoke this method on logout succeeded */
  final protected[auth]
  def logoutSucceeded(id: Id)(f: => Result)(implicit req: RequestHeader): Future[Result] = {
    tokenAccessor.extract(req) match {
      case Some(token) => datastore.destroy(token).map(_ => tokenAccessor.discard(f))
      case None        => Future.successful(tokenAccessor.discard(f))
    }
  }

  // --[ Methods ]--------------------------------------------------------------
  /** Verifies what user are authenticated to do. */
  final def authenticate(implicit req: RequestHeader) : Future[Either[Result, (User, Result => Result)]] =
    restore.map( _ match {
      case (Some(user), updater) => Right(user -> updater)
      case _ => Left(authenticationFailed)
    })

  /** Verifies what user are authorized to do. */
  final def authorized(authority: Option[Authority])(implicit req: RequestHeader): Future[Either[Result, (User, Result => Result)]] =
    for {
      Some((user, updater)) <- authenticate.map(_.right.toOption)
      authorized            <- authorize(user, authority)
    } yield {
      authorized match {
        case true  => Right(user -> updater)
        case false =>  Left(authorizationFailed(user, authority))
      }
    }

  /** Retrieve a user data by the session token in `RequestHeader`. */
  final def restore(implicit req: RequestHeader) : Future[(Option[User], Result => Result)] =
    extractToken match {
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
}

