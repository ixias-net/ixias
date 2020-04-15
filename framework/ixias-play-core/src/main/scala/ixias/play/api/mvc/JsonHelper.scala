/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc.Results._
import play.api.mvc.{ Request, AnyContent, Result }
import play.api.libs.json.{ Reads, Writes, JsSuccess, JsError }
import ixias.util.Logger

// Helper for JSON
//~~~~~~~~~~~~~~~~~~
trait  JsonHelper
object JsonHelperDefault extends JsonHelper {

  // -- [ Properties ]----------------------------------------------------------
  protected lazy val logger = Logger.apply

  // -- [ Methods ]-------------------------------------------------------------
  /**
   * Build a result object as JSON response.
   */
  def toJson[T](o: T)(implicit tjs: Writes[T]): Result =
    Ok(play.api.libs.json.Json.toJson(o))

  /**
   * To bind request data to a `T` component.
   */
  def bindFromRequest[T](implicit request: Request[AnyContent], rds: Reads[T]): Either[Result, T] =
    request.body.asJson match {
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
