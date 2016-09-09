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
import ixias.play.api.mvc.{ StackAction, StackActionFunction, StackActionRequest }

/**
 * Provides the custom action for authorization.
 */
object Authorized extends StackActionFunction with Results {
  implicit val ctx = executionContext

  /**
   * Authorize user's session.
   */
  def invokeBlock[A](request: StackActionRequest[A], block: StackActionRequest[A] => Future[Result]): Future[Result] =
    getInjector(request).map(_.instanceOf(classOf[AuthProfile])) match {
      case None       => Future.successful(InternalServerError)
      case Some(auth) => auth.authorize(request.get(auth.AuthorityKey))(request) flatMap {
        case Left(result)           => Future.successful(result)
        case Right((user, updater)) => block {
          request.set(auth.UserKey, user)
        } map(updater)
      }
    }
}
