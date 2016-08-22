/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import scala.concurrent.Future
import ixias.util.Logger

import play.api.mvc.{ Request, AnyContent, Result }
import play.api.mvc.Results._
import play.api.libs.json.{ Reads, Writes, JsValue, JsSuccess, JsError }
import ixias.play.api.mvc.Errors._

// JSON 処理定義
//~~~~~~~~~~~~~~~
object Json {

  // -- [ Properties ]----------------------------------------------------------
  /** ロガー定義 */
   protected lazy val logger = Logger.apply

  // -- [ Methods ]-------------------------------------------------------------
  /**
   * JSONデータを返す
   */
  def apply[T](content: T)(implicit writeable: Writes[T]): Result =
    Ok(play.api.libs.json.Json.toJson(content))

  /**
   * JSONデータを処理する
   */
  def bindFromRequest[T](success: T => Future[Result])
    (implicit rds: Reads[T], request: Request[AnyContent]): Future[Result] =
  {
    request.body.asJson match {
      case None => Future.successful(E_BAD_REQUEST)
      case Some(json: JsValue) => json.validate[T] match {
        case ex: JsError => {
          logger.error("Failure." + JsError.toJson(ex).toString())
          Future.successful(E_BAD_REQUEST)
        }
        case v: JsSuccess[T] => {
          logger.debug("Success." + v.get)
          success(v.get)
        }
      }
    }
  }
}
