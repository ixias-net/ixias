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

case class Authorization @Inject()(val profile: Profile) {

  /** Typedefs */
  type Id        = profile.Id
  type User      = profile.User
  type Authority = profile.Authority

  /** Verifies what user are authenticated to do. */
  private[auth] def authenticate(implicit req: RequestHeader) : Either[Result, (User, Result => Result)] =
    restore match {
      case (Some(user), updater) => Right(user -> updater)
      case _ => Left(profile.authenticationFailed(req))
    }

  /** Verifies what user are authorized to do. */
  private[auth] def authorized(authority: Option[Authority])(implicit req: RequestHeader): Either[Result, (User, Result => Result)] =
    authenticate.right flatMap {
      case (user, updater) => profile.authorize(user, authority) match {
        case Success(_) => Right(user -> updater)
        case Failure(_) => Left(profile.authorizationFailed(req, user, authority))
      }
    }

  /** Retrieve a user data by the session token in `RequestHeader`. */
  private[auth] def restore(implicit req: RequestHeader) : (Option[User], Result => Result) =
    extractToken(req) match {
      case None        => (None -> identity)
      case Some(token) => (for {
        Some(uid)  <- profile.datastore.read(token)
        Some(user) <- profile.resolve(uid)
      } yield {
        profile.datastore.setTimeout(token, profile.sessionTimeout)
        Some(user) -> profile.tokenAccessor.put(token) _
      }).getOrElse(None -> identity)
    }

  /** Extract a session token in `RequestHeader`. */
  private[auth] def extractToken(req: RequestHeader): Option[AuthenticityToken] =
    Play.isTest(Play.current) match {
      case false => profile.tokenAccessor.extract(req)
      case true  => req.headers.get("TEST_AUTH_TOKEN").orElse(profile.tokenAccessor.extract(req))
    }

}
