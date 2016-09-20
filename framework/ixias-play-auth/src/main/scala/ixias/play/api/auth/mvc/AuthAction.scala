/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

import play.api.mvc._
import scala.concurrent.Future
import ixias.play.api.mvc.{ StackActionRequest, StackActionBuilder }

/**
 * Provides helpers for creating `Action` values.
 */
trait AuthActionBuilder extends StackActionBuilder[StackActionRequest] {
  self =>

  // // --[ Methods ] -------------------------------------------------------------
  final def apply(auth: AuthProfile): StackActionBuilder[StackActionRequest] =
    apply(AuthProfileKey -> auth)

  // --[ Methods ] -------------------------------------------------------------
  /**
   * Invoke the block with a auth profile object.
   */
  def withAuthProfile(request: StackActionRequest[_], block: AuthProfile => Future[Result]): Future[Result] =
    request.get(AuthProfileKey) match {
      case Some(auth) => block(auth)
      case None       => Future.successful(Results.InternalServerError)
    }
}
