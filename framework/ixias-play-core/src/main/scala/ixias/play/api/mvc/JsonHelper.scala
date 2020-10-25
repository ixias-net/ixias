/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc._
import play.api.mvc.Results.BadRequest
import play.api.libs.json.{ Reads, JsSuccess, JsError }

// Helper for JSON
//~~~~~~~~~~~~~~~~~~
trait JsonHelper {

  /**
   * To bind request data to a `T` component.
   */
  def bindFromRequest[T]
    (implicit req: Request[AnyContent], reads: Reads[T]): Either[Result, T]
}

/**
 * Default helper
 */
object JsonHelper extends JsonHelper with ixias.util.Logging {

  /**
   * To bind request data to a `T` component.
   */
  def bindFromRequest[T]
    (implicit req: Request[AnyContent], reads: Reads[T]): Either[Result, T] =
    req.body.asJson match {
      case None       => Left(BadRequest)
      case Some(json) => json.validate[T] match {
        case JsSuccess(v, _) => Right(v)
        case JsError(errs)   => {
          logger.error(JsError.toJson(errs).toString())
          Left(BadRequest(JsError.toJson(errs)))
        }
      }
    }
}
