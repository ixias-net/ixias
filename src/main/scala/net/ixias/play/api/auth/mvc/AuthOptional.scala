/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.auth.mvc

import _root_.play.api.mvc._
import com.google.inject.Inject
import scala.concurrent.Future
import scala.util.{ Right, Left }
import play.api.auth.AuthProfile

/** Provides the utility methods for optional authentication. */
case class AuthOptional @Inject()(val auth: AuthProfile) extends StackAction {

  /** Proceed with the next advice or target method invocation */
  override def proceed[A](req: StackRequest[A])(f: StackRequest[A] => Future[Result]): Future[Result] = {
    implicit val ctx = createStackActionExecutionContext(req)
    auth.restore(req) match {
      case (None,       updater) => super.proceed(req)(f).map(updater)
      case (Some(user), updater) => super.proceed(req.set(AuthProfile.UserKey, user))(f).map(updater)
    }
  }
}
