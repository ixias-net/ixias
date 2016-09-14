/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

import play.api.Application
import play.api.mvc.{ Result, Results }

import scala.concurrent.Future
import ixias.play.api.mvc.{ StackActionBuilder, StackActionRequest }

/**
 * Provides the custom action for authentication.
 */
object Authenticated extends StackActionBuilder with Results {

  /**
   * Authenticate user's session.
   */
  def invokeBlock[A](request: StackActionRequest[A], block: StackActionRequest[A] => Future[Result]): Future[Result] =
    withApplication(request) { implicit app =>
      implicit val ctx = executionContext
      val auth = app.injector.instanceOf(classOf[AuthProfile])
      auth.authenticate(request) flatMap {
        case Left(result)           => Future.successful(result)
        case Right((user, updater)) => block {
          request.set(auth.UserKey, user)
        } map(updater)
      }
    }
}
