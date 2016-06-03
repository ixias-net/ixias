/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

import scala.concurrent.Future
import play.api.mvc.Result

/**
 * Provides the custom action for authentication.
 */
sealed class Authorized(params: Attribute[_]*)(implicit val auth: AuthProfile) extends StackAction(params: _*)
{
  /** Proceed with the next advice or target method invocation */
  override def proceed[A](req: ActionRequest[A])(f: ActionRequest[A] => Future[Result]): Future[Result] = {
    implicit val ctx = getExecutionContext(req)
    auth.authorized(req.get(auth.AuthorityKey))(req) flatMap {
      case Left(result)           => Future.successful(result)
      case Right((user, updater)) => super.proceed(
        req.set(auth.UserKey, user)
      )(f).map(updater)
    }
  }
}

/**
 * Build a custom action object.
 */
object Authorized extends StackAuthActionBuilder[Authorized]
{
  def build(params: Attribute[_]*)(implicit auth: AuthProfile): Authorized =
    new Authorized(params: _*)
}
