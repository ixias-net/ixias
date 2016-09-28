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
import ixias.play.api.mvc.{ StackActionRequest, StackActionBuilder }

/**
 * Provides the custom action for authorization.
 */
object Authorized extends AuthActionBuilder {

  // // --[ Methods ] -------------------------------------------------------------
  final def apply[A](auth: AuthProfile[A], authority: A): StackActionBuilder[StackActionRequest] =
    apply(
      AuthProfileKey    -> auth,
      auth.AuthorityKey -> authority
    )

  /**
   * Authorize user's session.
   */
  def invokeBlock[A](request: StackActionRequest[A], block: StackActionRequest[A] => Future[Result]): Future[Result] =
    withAuthProfile[AnyRef](request, {
      implicit val ctx = executionContext
      auth => auth.authorize(request.get(auth.AuthorityKey))(request) flatMap {
        case Left(result)           => Future.successful(result)
        case Right((user, updater)) => block {
          request.set(auth.UserKey, user)
        } map(updater)
      }
    })

}
