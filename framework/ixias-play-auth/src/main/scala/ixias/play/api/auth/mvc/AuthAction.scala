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
trait AuthActionBuilder extends StackActionBuilder {
  self =>

  /** The key of attribute for containing required authority roles. */
  case object AuthProfileKey extends StackActionRequest.AttributeKey[AuthProfile]

  // --[ Methods ] -------------------------------------------------------------
  /**
   * Invoke the block with a auth profile object.
   */
  def withAuthProfile(request: StackActionRequest[_], block: AuthProfile => Future[Result]): Future[Result] =
    request.get(AuthProfileKey) match {
      case Some(auth) => block(auth)
      case None       => Future.successful(Results.InternalServerError)
    }

  // --[ Methods ] -------------------------------------------------------------
  /**
   * Constructs an `Action` with default content, and no request parameter.
   */
  final def apply(auth: AuthProfile, block: => Result): Action[AnyContent] =
    apply(BodyParsers.parse.ignore(AnyContentAsEmpty: AnyContent), AuthProfileKey -> auth)(_ => block)

  /**
   * Constructs an `Action` with default content
   */
  final def apply(auth: AuthProfile, block: StackActionRequest[AnyContent] => Result): Action[AnyContent] =
    apply(BodyParsers.parse.default, AuthProfileKey -> auth)(block)

  // --[ Methods ] -------------------------------------------------------------
  /**
   * Constructs an `Action` that returns a future of a result,
   * with default content, and no request parameter.
   */
  final def async(auth: AuthProfile, block: => Future[Result]): Action[AnyContent] =
    async(BodyParsers.parse.ignore(AnyContentAsEmpty: AnyContent), AuthProfileKey -> auth)(_ => block)

  /**
   * Constructs an `Action` that returns a future of a result, with default content
   */
  final def async(auth: AuthProfile, block: StackActionRequest[AnyContent] => Future[Result]): Action[AnyContent] =
    async(BodyParsers.parse.default, AuthProfileKey -> auth)(block)
}
