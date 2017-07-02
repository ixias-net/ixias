/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc.Result
import cats.data.EitherT
import cats.instances.future._
import scala.concurrent.{ Future, ExecutionContext }
import scala.language.implicitConversions

trait BaseExtensionMethods {
  val Cursor     = ixias.persistence.model.Cursor
  val JsonHelper = ixias.play.api.mvc.JsonHelper
  val FormHelper = ixias.play.api.mvc.FormHelper
  val AttrHelper = ixias.play.api.mvc.RequestHeaderAttrHelper

  // Either[Result, Result] -> Result
  implicit def convEitherToResult(v: Either[Result, Result]): Result =
    v match { case Right(r) => r case Left(l) => l }

  // Future[Either[Result, Result]] -> Future[Result]
  implicit def convEitherToResult(f: Future[Either[Result, Result]])
    (implicit ec: ExecutionContext): Future[Result] = f.map(convEitherToResult(_))

  // EitherT[Future, Result, Result] -> Future[Result]
  implicit def convEitherToResult(t: EitherT[Future, Result, Result])
    (implicit ec: ExecutionContext): Future[Result] = t.valueOr(v => v)
}
