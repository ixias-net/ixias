/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.controllers

import controllers.{ AssetsBuilder, DefaultAssetsMetadata }
import play.api.{ Mode, Environment, Configuration }
import play.api.mvc.{ Action, AnyContent, ControllerComponents, BaseController }
import play.api.http.LazyHttpErrorHandler
import play.api.Logger

@javax.inject.Singleton
class UIAssets @javax.inject.Inject() (
  env:  Environment,
  conf: Configuration,
  meta: DefaultAssetsMetadata,
  protected val controllerComponents: ControllerComponents
) extends AssetsBuilder(LazyHttpErrorHandler, meta) with BaseController {

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

  // import javax.activation.MimetypesFileTypeMap

  /** Assetsハンドラー : 開発モード */
  private def devAssetHandler(file: String): Action[AnyContent] = {
    val path = basePaths.foldLeft[Option[String]](None) {
      case (prev, path) => prev match {
        case Some(_) => prev
        case None    => {
          val fullPath = path + "/" + file
          val exists   = (new java.io.File(fullPath)).isFile
          if (exists) Some(path.getName) else None
        }
      }
    }
    path match {
      case Some(path) => at(path, file, aggressiveCaching = false)
      case None       => Action { implicit request =>
        NotFound("404 - Page not found error. path=" + request.path)
      }
    }
  }
}
