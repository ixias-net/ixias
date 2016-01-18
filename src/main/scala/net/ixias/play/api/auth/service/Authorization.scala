/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.auth.service

import _root_.play.api.Play
import _root_.play.api.mvc.{ Result, RequestHeader }
import com.google.inject.Inject
import scala.util.{ Success, Failure }

import net.ixias.play.api.auth.token.AuthenticityToken

case class Authorization @Inject()(val auth: Profile) {

  /** Typedefs */
  type Id        = auth.Id
  type User      = auth.User
  type Authority = auth.Authority

  /** Retrieve a user data by the session token in `RequestHeader`. */
  private[auth] def restore(implicit req: RequestHeader) : (Option[User], Result => Result) =
    extractToken(req) match {
      case None        => (None -> identity)
      case Some(token) => (for {
        Some(uid)  <- auth.datastore.read(token)
        Some(user) <- auth.resolve(uid)
      } yield {
        auth.datastore.setTimeout(token, auth.sessionTimeout)
        Some(user) -> auth.tokenAccessor.put(token) _
      }).getOrElse(None -> identity)
    }

  /** Verifies what user are authorized to do. */
  private[auth] def authorized(authority: Authority)(implicit req: RequestHeader): Either[Result, (User, Result => Result)] = {
    // Authentication
    val authenticated = restore match {
      case (Some(user), updater) => Right(user -> updater)
      case _ => Left(auth.authenticationFailed(req))
    }
    // Authorization
    authenticated.right flatMap { case (user, updater) =>
      auth.authorize(user, authority) match {
        case Success(_) => Right(user -> updater)
        case Failure(_) => Left(auth.authorizationFailed(req, user, authority))
      }
    }
  }

  /** Extract a session token in `RequestHeader`. */
  private[auth] def extractToken(req: RequestHeader): Option[AuthenticityToken] =
    Play.isTest(Play.current) match {
      case false => auth.tokenAccessor.extract(req)
      case true  => req.headers.get("TEST_AUTH_TOKEN").orElse(auth.tokenAccessor.extract(req))
    }

}
