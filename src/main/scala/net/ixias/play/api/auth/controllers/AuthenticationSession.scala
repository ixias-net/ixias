/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.auth.controllers

import _root_.play.api.mvc._
import com.google.inject.Inject
import scala.util.{ Success, Failure }

import core.domain.model.{ Identity, Entity }
import play.api.auth.token.AuthenticityToken
import play.api.auth.mvc.StackRequest
import play.api.auth.service.Profile

/** Provides the utility methods for auth session. */
class AuthenticationSession @Inject()(val profile: Profile) extends Controller {

  /** Typedefs */
  type Id        = profile.Id
  type User      = profile.User
  type Authority = profile.Authority

  /** Retrieve a user session data. */
  def loggedIn(implicit req: StackRequest[_]): Option[User] =
    req.get(Profile.UserKey).map(_.asInstanceOf[User])

  /** Invoke this method on login succeeded */
  def loginSucceeded(id: Id)(f: AuthenticityToken => Result)(implicit req: RequestHeader): Result =
    profile.datastore.open(id, profile.sessionTimeout) match {
      case Success(token) => profile.tokenAccessor.put(token)(f(token))
      case Failure(_)     => InternalServerError
    }

  /** Invoke this method on logout succeeded */
  def logoutSucceeded(id: Id)(f: => Result)(implicit req: RequestHeader): Result = {
    profile.tokenAccessor.extract(req) map { profile.datastore.destroy }
    profile.tokenAccessor.discard(f)
  }
}
