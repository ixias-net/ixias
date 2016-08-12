/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

import scala.concurrent.Future
import play.api.mvc.Result
import ixias.play.api.auth.mvc.ActionRequest._

/**
 * Provides the custom action for authentication.
 */
sealed class AuthenticatedOrNot(params: Attribute[_]*)(implicit auth: AuthProfile) extends StackAction(params: _*)
{
  /** Proceed with the next advice or target method invocation */
  override def proceed[A](req: ActionRequest[A])(f: ActionRequest[A] => Future[Result]): Future[Result] = {
    implicit val ctx = getExecutionContext(req)
    auth.restore(req) flatMap {
      case (None,       updater) => super.proceed(req)(f).map(updater)
      case (Some(user), updater) => super.proceed(
        req.set(auth.UserKey, user)
      )(f).map(updater)
    }
  }
}

/**
 * Build a custom action object.
 */
object AuthenticatedOrNot extends StackAuthActionBuilder[AuthenticatedOrNot]
{
  def build(params: Attribute[_]*)(implicit auth: AuthProfile): AuthenticatedOrNot =
    new AuthenticatedOrNot(params: _*)
}
