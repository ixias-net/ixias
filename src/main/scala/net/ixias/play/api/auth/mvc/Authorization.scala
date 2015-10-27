/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.auth.mvc

import _root_.play.api.Play
import _root_.play.api.mvc._

import play.api.auth.token._
import play.api.auth.AuthProfile
import scala.concurrent.{ ExecutionContext, Future }

/** Provides the utility methods for authorization. */
trait Authorization { self: AuthProfile with Action =>

  /** Verifies what user are authorized to do. */
  def authorized(authority: Authority)
    (implicit request: RequestHeader, context: ExecutionContext)
      : Future[Either[Result, (User, Result => Result)]] = {
    restoreUser collect {
      case (Some(user), updater) => Right(user -> updater)
    } recoverWith {
      case _ => authenticationFailed(request).map(Left.apply)
    } flatMap {
      case Right((user, updater)) => authorize(user, authority) collect {
        case true => Right(user -> updater)
      } recoverWith {
        case _ => authorizationFailed(request, user, Some(authority)).map(Left.apply)
      }
      case Left(result) => Future.successful(Left(result))
    }
  }

  /** Retrieve a user data by the session token in `RequestHeader`. */
  private[mvc] def restoreUser
    (implicit request: RequestHeader, context: ExecutionContext)
      : Future[(Option[User], Result => Result)] = {
    (for {
      token <- extractToken(request)
    } yield for {
      Some(uid)  <- datastore.read(token)
      Some(user) <- resolve(uid)
      _          <- datastore.setTimeout(token, sessionTimeout)
    } yield {
      Option(user) -> tokenAccessor.put(token) _
    }) getOrElse {
      Future.successful(Option.empty -> identity)
    }
  }

  /** Extract a session token in `RequestHeader`. */
  private[mvc] def extractToken(request: RequestHeader): Option[AuthenticityToken] = {
    if (Play.isTest(Play.current)) {
      request.headers.get("TEST_AUTH_TOKEN").orElse(tokenAccessor.extract(request))
    } else {
      tokenAccessor.extract(request)
    }
  }
}
