/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc.Result
import scala.language.implicitConversions

trait BaseExtensionMethods {
  val Cursor     = ixias.persistence.model.Cursor
  val JsonHelper = ixias.play.api.mvc.JsonHelper
  val FormHelper = ixias.play.api.mvc.FormHelper

  implicit def convEitherToResult(v: Either[Result, Result]): Result =
    v match { case Right(r) => r case Left(l) => l }
}
