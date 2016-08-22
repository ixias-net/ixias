/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc.{ Request, Result }
import play.api.mvc.Results._
import play.api.data.Forms._
import play.api.data.{ Mapping, Form => PlayForm }
import play.api.i18n.Lang.defaultLang

import ixias.util.Logger
import scala.concurrent.Future
import ixias.play.api.mvc.Errors._

// フォーム処理
//~~~~~~~~~~~~~~
object FormAction {

  /** ログ定義 */
  protected lazy val logger = Logger.apply

  /** フォームデータ処理 : 正常処理のみ */
  def bindFromRequest[T](mapping: Mapping[T])(success: T => Future[Result])
    (implicit request: Request[_]): Future[Result] =
    PlayForm(mapping).bindFromRequest.fold(
      v => {
        logger.info("Failure." + v.errors)
        Future.successful(E_BAD_REQUEST)
      },
      v => {
        logger.debug("Success." + v)
        success(v)
      }
    )
}
