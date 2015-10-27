/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.auth.mvc

import _root_.play.api.mvc.Result
import scala.concurrent.Future

import play.api.mvc.StackAction
import play.api.auth.AuthProfile

/** Provides the utility methods for authentication. */
trait AuthAction extends Action with StackAction with Authorization {
  self: AuthProfile =>

  // --[ Properties ]-----------------------------------------------------------
  /** Import class types for StackRequest attributes. */
  import StackRequest._

  /** The key of attribute for containing required authority roles. */
  case object UserKey extends AttributeKey[User]

  // --[ Methods ]--------------------------------------------------------------
  override def proceed[A](req: StackRequest[A])(f: StackRequest[A] => Future[Result]): Future[Result] = {
    implicit val (request, ctx) = (req, createStackActionExecutionContext(req))
    restoreUser recover {
      case _ => None -> identity[Result] _
    } flatMap {
      case (Some(user), updater) => super.proceed(req.set(UserKey, user))(f).map(updater)
      case (None, _)             => authenticationFailed(req)
    }
  }
}
