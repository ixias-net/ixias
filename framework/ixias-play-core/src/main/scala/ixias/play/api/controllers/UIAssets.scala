/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.controllers

import akka.util.ByteString
import akka.stream.scaladsl.Source
import scala.concurrent.ExecutionContext

import play.api.{ Mode, Environment, Configuration }
import play.api.http.{ HttpEntity, FileMimeTypes, HttpErrorHandler }
import play.api.mvc.{ Action, AnyContent }
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.streams.IterateeStreams
import play.api.Logger

import controllers.{ AssetsBuilder, DefaultAssetsMetadata }

@javax.inject.Singleton
class UIAssets @javax.inject.Inject() (
  env:           Environment,
  conf:          Configuration,
  errorHandler:  HttpErrorHandler,
  meta:          DefaultAssetsMetadata,
  fileMimeTypes: FileMimeTypes
)(implicit ec: ExecutionContext) extends AssetsBuilder(errorHandler, meta) {

  import controllers.Assets._

  protected val CF_ASSETS_DEV_DIR = "assets.dev.dirs"

  /** The logger */
  private lazy val logger = Logger(getClass)

  /** Assets Handler */
  override def versioned(path: String, file: Asset): Action[AnyContent] = {
    env.mode match {
      case Mode.Prod => super.versioned(path, file)
      case _         => devAssetHandler(file.name)
    }
  }

  /** 開発モード時にAssetsを提供するディレクトリ・リスト */
  val basePaths: Seq[java.io.File] =
    conf.getOptional[Seq[String]](CF_ASSETS_DEV_DIR) match {
      case Some(dirs) => dirs.map(env.getFile).filter(_.exists)
      case None       => Seq(
        env.getFile("ui"),
        env.getFile("ui/src"),
        env.getFile("ui/build"),
        env.getFile("ui/dist"),
        env.getFile("target/web/public/main")
      ).filter(_.exists)
    }

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
        val data   = Enumerator.fromStream(new java.io.FileInputStream(file))
        val source = Source.fromPublisher(IterateeStreams.enumeratorToPublisher(data)).map(ByteString.apply)
        logger.info(s"serving $file")
        Ok.sendEntity(HttpEntity.Streamed(source, None, None))
          .as(fileMimeTypes.forFileName(file.toString).getOrElse("application/octet-stream"))
          .withHeaders(CACHE_CONTROL -> "no-store")
      }
      case None => {
        NotFound("404 - Page not found error. path=" + request.path)
      }
    }
  }
}
