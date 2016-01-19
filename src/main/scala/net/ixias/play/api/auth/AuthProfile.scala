/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.auth

import _root_.play.api.Play
import _root_.play.api.mvc._
import scala.util.{ Try, Success, Failure }
import scala.concurrent.duration.Duration

import play.api.auth.mvc.StackRequest
import play.api.auth.data.Container
import play.api.auth.token.{ Token, AuthenticityToken, SignedToken }
import core.domain.model.{ Identity, Entity }

trait AuthProfile extends AuthProfileLike with Results {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of user identity */
  type Id   <: Identity[_]
  /** The type of user entity */
  type User <: Entity[_]
  /** The type of authority roles */
  type Authority >: Null

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
  def resolve(id: Id): Try[Option[User]]

  /** Verifies what user are authorized to do. */
  def authorize(user: User, authority: Option[Authority]): Try[Boolean]

  // --[ Methods ]--------------------------------------------------------------
  /** Invoke this method on login succeeded */
  def loginSucceeded(id: Id)(implicit req: RequestHeader): Result

  /** Invoke this method on logout succeeded */
  def logoutSucceeded(id: Id)(implicit req: RequestHeader): Result

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

// Companion Object
//~~~~~~~~~~~~~~~~~~
object AuthProfile {
  /** The key of attribute for containing required authority roles. */
  case object UserKey      extends StackRequest.AttributeKey[AuthProfile#User]
  /** The key of attribute for containing user data. */
  case object AuthorityKey extends StackRequest.AttributeKey[AuthProfile#Authority]
}

// Feature Template
//~~~~~~~~~~~~~~~~~~
trait AuthProfileLike { self: AuthProfile =>

  // --[ Methods ]--------------------------------------------------------------
  /** Returns authorized user. */
  def loggedIn(implicit req: StackRequest[_]): Option[User] =
    req.get(AuthProfile.UserKey).map(_.asInstanceOf[User])

  /** Returns the result response. */
  def loggedIn[A](f: User => Result)(implicit req: StackRequest[A]): Result =
    loggedIn.fold(authenticationFailed)(f)

  /** Returns the result response of applying $f to user data
    * if the user data is nonempty. Otherwise, evaluates expression `ifEmpty`*/
  def loggedInOrNot[A](ifEmpty: => Result)(f: User => Result)(implicit req: StackRequest[A]): Result =
    loggedIn.fold(ifEmpty)(f)

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
  def loginSucceeded(id: Id)(f: AuthenticityToken => Result)(implicit req: RequestHeader): Result =
    datastore.open(id, sessionTimeout) match {
      case Success(token) => tokenAccessor.put(token)(f(token))
      case Failure(_)     => InternalServerError
    }

  /** Invoke this method on logout succeeded */
  final protected[auth]
  def logoutSucceeded(id: Id)(f: => Result)(implicit req: RequestHeader): Result = {
    tokenAccessor.extract(req) map { datastore.destroy }
    tokenAccessor.discard(f)
  }

  // --[ Methods ]--------------------------------------------------------------
  /** Verifies what user are authenticated to do. */
  final protected[auth]
  def authenticate(implicit req: RequestHeader) : Either[Result, (User, Result => Result)] =
    restore match {
      case (Some(user), updater) => Right(user -> updater)
      case _ => Left(authenticationFailed)
    }

  /** Verifies what user are authorized to do. */
  final protected[auth]
  def authorized(authority: Option[Authority])(implicit req: RequestHeader): Either[Result, (User, Result => Result)] =
    authenticate.right flatMap {
      case (user, updater) => authorize(user, authority) match {
        case Success(_) => Right(user -> updater)
        case Failure(_) => Left(authorizationFailed(user, authority))
      }
    }

  /** Retrieve a user data by the session token in `RequestHeader`. */
  final protected[auth]
  def restore(implicit req: RequestHeader) : (Option[User], Result => Result) =
    extractToken match {
      case None        => (None -> identity)
      case Some(token) => (for {
        Some(uid)  <- datastore.read(token)
        Some(user) <- resolve(uid)
      } yield {
        datastore.setTimeout(token, sessionTimeout)
        Some(user) -> tokenAccessor.put(token) _
      }).getOrElse(None -> identity)
    }
}

