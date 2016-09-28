/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.controllers

import play.api._
import play.api.mvc.{ Action, AnyContent, Controller }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Logger
import play.api.libs.streams.Streams
import play.api.libs.iteratee.Enumerator

import akka._
import akka.stream.scaladsl._
import scala.concurrent.Future
import scala.collection.JavaConversions._

import javax.inject._
import java.io.{ File, FileInputStream }

@Singleton
class UIAssets @Inject() (env: Environment, conf: Configuration) extends Controller {

  /** ロガーの取得 */
  private lazy val logger = Logger(getClass)

  /** Assetsハンドラー */
  def versioned(file: String): Action[AnyContent] =
    env.mode match {
      case Mode.Prod => {
        import controllers.Assets._
        controllers.Assets.versioned("/public", file)
      }
      case _ => devAssetHandler(file)
    }

  /** 開発モード時にAssetsを提供するディレクトリ・リスト */
  val basePaths: List[java.io.File] =
    conf.getStringList("assets.dev.dirs") match {
      case Some(dirs) => dirs.map(env.getFile).toList
      case _          => List(
        env.getFile("ui"),
        env.getFile("ui/src"),
        env.getFile("ui/build"),
        env.getFile("ui/dist"),
        env.getFile("target/web/public/main")
      )
    }
  import javax.activation.MimetypesFileTypeMap

  /** Assetsハンドラー : 開発モード */
  private def devAssetHandler(file: String): Action[AnyContent] = Action.async { request =>
    Future {
      val paths   = basePaths.view.map(new File(_, file)).filter(_.exists)
      val results = paths.map(f => f.isFile match {
        case false => Forbidden(views.html.defaultpages.unauthorized())
        case true  => {
          logger.info(s"Serving $file")
          val in: FileInputStream = new FileInputStream(f)
          val content: Enumerator[Array[Byte]] = Enumerator.fromStream(in) //, 1024*1024)
          val source: Source[Array[Byte], NotUsed] = Source.fromPublisher(Streams.enumeratorToPublisher(content))
          Ok.chunked(source)
            .as(play.api.libs.MimeTypes.forFileName(file).getOrElse("application/octet-stream"))
            .withHeaders(CACHE_CONTROL -> "no-store")
        }
      })
      results.headOption.getOrElse(
        NotFound("404 - Page not found error." + request.path))
    }
  }
}
