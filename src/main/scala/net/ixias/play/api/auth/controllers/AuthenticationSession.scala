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

import play.api.auth.AuthProfile
import play.api.auth.mvc.StackRequest
import play.api.auth.token.AuthenticityToken
import core.domain.model.{ Identity, Entity }

/** Provides the utility methods for auth session. */
class AuthenticationSession @Inject()(val auth: AuthProfile) extends Controller {

  /** Typedefs */
  type Id        = auth.Id
  type User      = auth.User
  type Authority = auth.Authority

  /** Retrieve a user session data. */
  def loggedIn(implicit req: StackRequest[_]): Option[User] =
    req.get(AuthProfile.UserKey).map(_.asInstanceOf[User])

  /** Invoke this method on login succeeded */
  def loginSucceeded(id: Id)(f: AuthenticityToken => Result)(implicit req: RequestHeader): Result =
    auth.datastore.open(id, auth.sessionTimeout) match {
      case Success(token) => auth.tokenAccessor.put(token)(f(token))
      case Failure(_)     => InternalServerError
    }

  /** Invoke this method on logout succeeded */
  def logoutSucceeded(id: Id)(f: => Result)(implicit req: RequestHeader): Result = {
    auth.tokenAccessor.extract(req) map { auth.datastore.destroy }
    auth.tokenAccessor.discard(f)
  }
}
