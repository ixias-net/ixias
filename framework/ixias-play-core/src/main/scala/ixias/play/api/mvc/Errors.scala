/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.http.Status._
import play.api.libs.json.{ Json => PlayJson }

import ixias.util.EnumOf
import ixias.util.Logging
import ixias.play.api.json.JsValueError

// エラー定義
//~~~~~~~~~~~~
case class Error(
  val status:  Status,
  val code:    Int,
  val message: Option[String] = None
) extends Logging {

  /** HTTPレスポンスを返す処理 */
  def apply: Result = {
    val message = this.message.getOrElse("An error occurred in the client.")
    logger.info("code:%d, message:%s".format(code, message))
    status(PlayJson.toJson(JsValueError(code, Some(message))))
  }

  /** HTTPレスポンスを返す処理(例外情報の記録) */
  def apply(ex: Throwable): Result = {
    val message = this.message.getOrElse("An error occurred in the client.")
    logger.error("code:%d, message:%s".format(code, message), ex)
    status(PlayJson.toJson(JsValueError(code, Some(message))))
  }
}

trait Errors {
  val E_NOT_FOUND       = Error(NotFound,            NOT_FOUND,             Some("Not found resouce."))
  val E_BAD_REQUEST     = Error(BadRequest,          BAD_REQUEST,           Some("Bad request."))
  val E_AUTHENTICATION  = Error(Unauthorized,        UNAUTHORIZED,          Some("Authentication failure."))
  val E_AUTHRIZATION    = Error(Unauthorized,        UNAUTHORIZED,          Some("Authorization failure."))
  val E_INTERNAL_SERVER = Error(InternalServerError, INTERNAL_SERVER_ERROR, Some("Internal server error."))

  import scala.language.implicitConversions
  implicit def convert(v: Error): Result = v.apply
}

object Errors extends Errors
