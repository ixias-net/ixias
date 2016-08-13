/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

import scala.concurrent.Future

import play.api.{ Environment, Mode }
import play.api.mvc.{ RequestHeader, Result, Results }
import play.api.libs.iteratee.Execution.Implicits.trampoline

import ixias.play.api.auth.token._
import ixias.play.api.auth.container.Container

// Feature Template
//~~~~~~~~~~~~~~~~~~
trait AuthProfileLike { self: AuthProfile =>

  implicit val env: Environment

  // --[ Methods ]--------------------------------------------------------------
  /** Returns authorized user. */
  def loggedIn(implicit req: ActionRequest[_]): Option[User] =
    req.get(UserKey).map(_.asInstanceOf[User])

  /** Returns the result response. */
  def loggedIn[A](f: User => Result)
    (implicit req: ActionRequest[A]):
      Result = loggedIn.fold(authenticationFailed)(f)

  /** Returns the result response. */
  def loggedIn[A](f: User => Future[Result])
    (implicit req: ActionRequest[A]):
      Future[Result] = loggedIn.fold(Future(authenticationFailed))(f)

  /** Returns the result response of applying $f to user data
    * if the user data is nonempty. Otherwise, evaluates expression `ifEmpty`*/
  def loggedInOrNot[A](ifEmpty: => Result)(f: User => Result)
    (implicit req: ActionRequest[A]):
      Result = loggedIn.fold(ifEmpty)(f)

  /** Returns the result response of applying $f to user data
    * if the user data is nonempty. Otherwise, evaluates expression `ifEmpty`*/
  def loggedInOrNot[A](ifEmpty: => Result)(f: User => Future[Result])
    (implicit req: ActionRequest[A]):
      Future[Result] = loggedIn.fold(Future(ifEmpty))(f)

  /** Extract a session token in `RequestHeader`. */
  def extractToken(implicit req: RequestHeader): Option[AuthenticityToken] =
    env.mode match {
      case Mode.Prod => tokenAccessor.extract(req)
      case _         => req.headers.get("TEST_AUTH_TOKEN").orElse(tokenAccessor.extract(req))
    }

  /** Extract a signed session token in `RequestHeader`. */
  def extractSignedToken(implicit req: RequestHeader): Option[SignedToken] =
    extractToken.map(Token.signWithHMAC)

  // --[ Methods ]--------------------------------------------------------------
  /** Invoke this method on login succeeded */
  def loginSucceeded(id: Id)(f: AuthenticityToken => Result)(implicit req: RequestHeader): Future[Result] =
    datastore.open(id, sessionTimeout).map { token =>
      tokenAccessor.put(token)(f(token))
    } recover { case _: Throwable => InternalServerError }

  /** Invoke this method on logout succeeded */
  def logoutSucceeded(id: Id)(f: => Result)(implicit req: RequestHeader): Future[Result] = {
    tokenAccessor.extract(req) match {
      case Some(token) => datastore.destroy(token).map(_ => tokenAccessor.discard(f))
      case None        => Future.successful(tokenAccessor.discard(f))
    }
  }

  // --[ Methods ]--------------------------------------------------------------
  /** Verifies what user are authenticated to do. */
  final def authenticate(implicit req: RequestHeader): Future[Either[Result, (User, Result => Result)]] =
    restore.map( _ match {
      case (Some(user), updater) => Right(user -> updater)
      case _                     => Left(authenticationFailed)
    })

  /** Verifies what user are authorized to do. */
  final def authorized(authority: Option[Authority])
    (implicit req: RequestHeader): Future[Either[Result, (User, Result => Result)]] =
    for {
      Some((user, updater)) <- authenticate.map(_.right.toOption)
      authorized            <- authorize(user, authority)
    } yield {
      authorized match {
        case true  => Right(user -> updater)
        case false => Left(authorizationFailed(user, authority))
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
