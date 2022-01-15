/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3

import slick.jdbc.JdbcProfile
import com.amazonaws.services.s3.model.S3ObjectInputStream
import scala.concurrent.Future

import ixias.aws.s3.model.File
import ixias.aws.s3.persistence.SlickResource
import ixias.persistence.SlickRepository

// S3 management repository
//~~~~~~~~~~~~~~~~~~~~~~~~~~~
trait AmazonS3Repository[P <: JdbcProfile]
    extends SlickRepository[File.Id, File, P] with SlickResource[P]
{
  import api._

  // --[ Alias ]----------------------------------------------------------------
  /** The data source name */
  val  DataSourceName = ixias.aws.s3.backend.DataSourceName
  type DataSourceName = ixias.aws.s3.backend.DataSourceName
  implicit val dsn: DataSourceName

  // --[ Properties ]-----------------------------------------------------------
  /** The backend */
  protected val s3 = ixias.aws.s3.backend.AmazonS3Backend

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Get file object.
   */
  def get(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(FileTable, "slave") { slick =>
      slick.unique(id).result.headOption
    }

  /**
   * Get file object with a pre-signed URL for accessing an Amazon S3 resource.
   */
  def getWithPresigned(id: Id): Future[Option[EntityEmbeddedId]] =
    get(id) flatMap {
      case None       => Future.successful(None)
      case Some(file) => for {
        client <- s3.getClient
        url    <- client.genPresignedUrlForAccess(file.v)
      } yield Some(file.map(_.copy(presignedUrl = Some(url))))
    }

  /**
   * Get file object as `Entity` with a input stream for it.
   */
  def getWithContent(id: Id): Future[Option[(EntityEmbeddedId, S3ObjectInputStream)]] =
    get(id) flatMap {
      case None       => Future.successful(None)
      case Some(file) => for {
        client   <- s3.getClient
        s3object <- client.load(file.v)
      } yield Some((file, s3object.getObjectContent()))
    }

  /**
   * Finds file objects by set of file ids.
   */
  def filter(ids: Seq[Id]): Future[Seq[EntityEmbeddedId]] =
    RunDBAction(FileTable, "slave") { slick =>
      slick.filter(_.id inSet ids).result
    }

  /**
   * Finds file objects with a pre-signed URL by set of file ids.
   */
  def filterWithPresigned(ids: Seq[Id]): Future[Seq[EntityEmbeddedId]] =
    for {
      client   <- s3.getClient
      fileSeq1 <- RunDBAction(FileTable, "slave") { slick =>
        slick.filter(_.id inSet ids).result
      }
      fileSeq2 <- Future.sequence(fileSeq1.map {
        file => for {
          url <- client.genPresignedUrlForAccess(file)
        } yield file.copy(presignedUrl = Some(url))
      })
    } yield fileSeq2

  // --[ Methods ]--------------------------------------------------------------
  @deprecated("use `add` or `addViaPresignedUrl` method", "2.0")
  def add(file: EntityWithNoId): Future[Id] =
    Future.failed(new UnsupportedOperationException)

  /**
   * Save the file information.
   * At the same time upload a specified file to S3.
   */
  def add(file: EntityWithNoId, content: java.io.File): Future[Id] =
    for {
      client    <- s3.getClient
      _         <- client.upload(file.v, content)
      Some(fid) <- RunDBAction(FileTable) { slick =>
        slick returning slick.map(_.id) += file.v
      }
    } yield File.Id(fid)

  /**
   * Save the file information.
   * However, the file body content has not yet been uploaded to S3.
   * After this method called, upload a file via presigned URL.
   */
  def addViaPresignedUrl(file: EntityWithNoId): Future[(Id, String)] =
    for {
      client    <- s3.getClient
      url       <- client.genPresignedUrlForUpload(file.v)
      Some(fid) <- RunDBAction(FileTable) { slick =>
        slick returning slick.map(_.id) += file.v
      }
    } yield (File.Id(fid), url.toString())

  // --[ Methods ]--------------------------------------------------------------
  @deprecated("use `update` or `updateViaPresignedUrl` method", "2.0")
  def update(file: EntityEmbeddedId): Future[Option[EntityEmbeddedId]] =
    Future.failed(new UnsupportedOperationException)

  /**
   * Update the file information.
   * At the same time upload a specified file to S3.
   */
  def update(file: EntityEmbeddedId, content: java.io.File): Future[Option[EntityEmbeddedId]] =
    for {
      client <- s3.getClient
      _      <- client.upload(file.v, content)
      old    <- RunDBAction(FileTable) { slick =>
        for {
          old <- slick.unique(file.id).result.headOption
          _   <- slick.unique(file.id).update(file.v)
        } yield old
      }
    } yield old

  /**
   * Update the file information.
   * However, the file body content has not yet been uploaded to S3.
   * After this method called, upload a file via presigned URL.
   */
  def updateViaPresignedUrl(file: EntityEmbeddedId): Future[(Option[EntityEmbeddedId], String)] =
    for {
      client <- s3.getClient
      url    <- client.genPresignedUrlForUpload(file.v)
      old    <- RunDBAction(FileTable) { slick =>
        for {
          old <- slick.unique(file.id).result.headOption
          _   <- slick.unique(file.id).update(file.v)
        } yield old
      }
    } yield (old, url.toString())

  /**
   * Update the sepecified file's image size.
   */
  def updateImageSize(fid: Id, size: File.ImageSize): Future[Option[EntityEmbeddedId]] =
    RunDBAction(FileTable) { slick =>
      for {
        old <- slick.unique(fid).result.headOption
        _   <- slick.unique(fid).map(
          c => (c.width, c.height)
        ).update((Some(size.width), Some(size.height)))
      } yield old
    }

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Remove the file information.
   */
  def remove(id: Id): Future[Option[EntityEmbeddedId]] =
    for {
      old <- RunDBAction(FileTable) { slick =>
        for {
          old <- slick.unique(id).result.headOption
          _   <- slick.unique(id).delete
        } yield old
      }
    } yield old
  
  /**
   * Remove the file information list.
   */
  def bulkRemove(idSeq: Seq[Id]): Future[Seq[EntityEmbeddedId]] =
    for {
      fileSeq <- RunDBAction(FileTable) { slick =>
        val rows = slick.filter(_.id inSet idSeq)
        for {
          oldSeq <- rows.result
          _      <- rows.delete
        } yield oldSeq
      }
    } yield fileSeq

  /**
   * Erase the file information and a physical file object at S3.
   */
  def erase(id: Id): Future[Option[EntityEmbeddedId]] =
    for {
      fileOpt <- RunDBAction(FileTable) { slick =>
        for {
          old <- slick.unique(id).result.headOption
          _   <- slick.unique(id).delete
        } yield old
      }
      _ <- fileOpt match {
        case None       => Future.successful(())
        case Some(file) => s3.getClient.map(_.remove(file))
      }
    } yield fileOpt

  /**
   * Erase the file information list and the physical file object list at S3.
   */
  def bulkErase(idSeq: Seq[Id]): Future[Seq[EntityEmbeddedId]] =
    for {
      fileSeq <- RunDBAction(FileTable) { slick =>
        val rows = slick.filter(_.id inSet idSeq)
        for {
          oldSeq <- rows.result
          _      <- rows.delete
        } yield oldSeq
      }
      _ <- s3.getClient.map(_.bulkRemove(fileSeq(0).bucket, fileSeq))
    } yield fileSeq

}
