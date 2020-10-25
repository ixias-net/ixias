/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc._
import cats.data.EitherT
import cats.instances.future._
import scala.concurrent.Future
import scala.language.implicitConversions

trait BaseExtensionMethods { self: BaseControllerHelpers =>
  val Cursor     = ixias.persistence.model.Cursor
  val AttrHelper = ixias.play.api.mvc.RequestHeaderAttrHelper
  val FormHelper: ixias.play.api.mvc.FormHelper = ixias.play.api.mvc.FormHelper
  val JsonHelper: ixias.play.api.mvc.JsonHelper = ixias.play.api.mvc.JsonHelper

  /** The ExecutionContext with using on Playframework. */
  implicit lazy val executionContext = defaultExecutionContext

  // --[ Methods ] -------------------------------------------------------------
  // Either[Result, Result] -> Result
  implicit def convEitherToResult(v: Either[Result, Result]): Result =
    v match { case Right(r) => r case Left(l) => l }

  // Future[Either[Result, Result]] -> Future[Result]
  implicit def convEitherToResult(f: Future[Either[Result, Result]]): Future[Result] =
    f.map(convEitherToResult(_))

  // EitherT[Future, Result, Result] -> Future[Result]
  implicit def convEitherToResult(t: EitherT[Future, Result, Result]): Future[Result] =
    t.valueOr(v => v)

  // --[ Methods ] -------------------------------------------------------------
  def DeviceDetection: ActionBuilder[Request, AnyContent] = DeviceDetectionBuilder(parse.default)
  val DeviceDetectionAttrKey = ixias.play.api.mvc.DeviceDetectionAttrKey
}
