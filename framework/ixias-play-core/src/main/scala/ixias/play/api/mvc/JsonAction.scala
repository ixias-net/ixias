/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc.Results._
import play.api.mvc.{ Request, AnyContent, Result }
import play.api.data.validation.ValidationError
import play.api.libs.json.{ Reads, Writes, JsSuccess, JsError }
import scala.concurrent.Future

import ixias.play.api.mvc.Errors._
import ixias.util.Logger

// Helper for JSON
//~~~~~~~~~~~~~~~~~~
object JsonAction {

  // -- [ Properties ]----------------------------------------------------------
  protected lazy val logger = Logger.apply

  // -- [ Methods ]-------------------------------------------------------------
  /**
   * Build a result object as JSON response.
   */
  def toJson[T](content: T)(implicit writeable: Writes[T]): Result =
    Ok(play.api.libs.json.Json.toJson(content))

  /**
   * To bind request data to a `T` component.
   */
  def bindFromRequest[T](implicit request: Request[AnyContent], rds: Reads[T]): Either[JsError, T] =
    request.body.asJson match {
      case None       => Left(JsError())
      case Some(json) => json.validate[T] match {
        case JsSuccess(v, _) => Right(v)
        case error: JsError  => {
          logger.error(JsError.toJson(error).toString())
          Left(error)
        }
      }
    }
}
