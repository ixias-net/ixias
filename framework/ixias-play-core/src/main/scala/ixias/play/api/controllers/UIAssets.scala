/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.controllers

import controllers.{ AssetsBuilder, DefaultAssetsMetadata }
import play.api.{ Mode, Environment, Configuration }
import play.api.mvc.{ Action, AnyContent, InjectedController }
import play.api.http.LazyHttpErrorHandler
import play.api.Logger

@javax.inject.Singleton
class UIAssets @javax.inject.Inject() (
  env:  Environment,
  conf: Configuration,
  meta: DefaultAssetsMetadata
) extends AssetsBuilder(LazyHttpErrorHandler, meta) with InjectedController {

  import controllers.Assets._

  /** ロガーの取得 */
  private lazy val logger = Logger(getClass)

  /** Assetsハンドラー */
  override def versioned(path: String, file: Asset): Action[AnyContent] = {
    env.mode match {
      case Mode.Prod => super.versioned(path, file)
      case _         => devAssetHandler(file.name)
    }
  }

  /** 開発モード時にAssetsを提供するディレクトリ・リスト */
  val basePaths: Seq[java.io.File] =
    (conf.get[Seq[String]]("assets.dev.dirs") match {
      case dirs if dirs.size > 0 => dirs.map(env.getFile)
      case _ => Seq(
        env.getFile("ui"),
        env.getFile("ui/src"),
        env.getFile("ui/build"),
        env.getFile("ui/dist"),
        env.getFile("target/web/public/main")
      )
    }).filter(_.exists)

  /** Assetsハンドラー : 開発モード */
  private def devAssetHandler(file: String): Action[AnyContent] = Action { implicit request =>
    val resource = basePaths.foldLeft[Option[java.io.File]](None) {
      case (prev, path) => prev match {
        case Some(_) => prev
        case None    => {
          val fullPath = path + "/" + file
          val resource = new java.io.File(fullPath)
          if (resource.isFile) Some(resource) else None
        }
      }
    }
    resource match {
      case Some(file) => {
        val stream = new java.io.FileInputStream(file)
        val source = akka.stream.scaladsl.StreamConverters.fromInputStream(() => stream)
        logger.info(s"Serving $file")
        Ok.chunked(source)
          .as(fileMimeTypes.forFileName(file.toString).getOrElse("application/octet-stream"))
          .withHeaders(CACHE_CONTROL -> "no-store")
      }
      case None => NotFound("404 - Page not found error. path=" + request.path)
    }
  }
}
