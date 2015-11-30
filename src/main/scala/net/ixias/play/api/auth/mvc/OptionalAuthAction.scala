/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.play.api.auth.mvc

import play.api.mvc.Result
import scala.concurrent.Future
import net.ixias.play.api.mvc.StackAction
import net.ixias.play.api.auth.AuthProfile

/** Provides the utility methods for authorization. */
trait OptionalAuthAction extends Action with StackAction with Authorization {
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
      case (user, updater) => super.proceed(
        user.map(u => req.set(UserKey, u)).getOrElse(req))(f).map(updater)
    }
  }
}
