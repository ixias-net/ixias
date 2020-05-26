/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.http.Status._
import play.api.libs.json.Json
import ixias.util.Logging
import ixias.play.api.json.JsValueError

/**
 * The component to represent error response.
 */
case class Error(status: Status, code: Int, message: Option[String] = None) extends Logging {

  /** Build a JSON response. */
  def toResult: Result = {
    val message = this.message.getOrElse("An error occurred in the client.")
    logger.info("code:%d, message:%s".format(code, message))
    status(Json.toJson(JsValueError(code, Some(message))))
  }

  /** Build a JSON response with error infomation. */
  def toResult(ex: Throwable): Result = {
    val message = this.message.getOrElse("An error occurred in the client.")
    logger.error("code:%d, message:%s".format(code, message), ex)
    status(Json.toJson(JsValueError(code, Some(message))))
  }
}

trait Errors {
  val E_NOT_FOUND       = Error(NotFound,            NOT_FOUND,             Some("Not found resource."))
  val E_BAD_REQUEST     = Error(BadRequest,          BAD_REQUEST,           Some("Bad request."))
  val E_AUTHENTICATION  = Error(Unauthorized,        UNAUTHORIZED,          Some("Authentication failure."))
  val E_AUTHRIZATION    = Error(Unauthorized,        UNAUTHORIZED,          Some("Authorization failure."))
  val E_INTERNAL_SERVER = Error(InternalServerError, INTERNAL_SERVER_ERROR, Some("Internal server error."))

  import scala.language.implicitConversions
  implicit def convert(v: Error): Result = v.toResult
}

object Errors extends Errors
