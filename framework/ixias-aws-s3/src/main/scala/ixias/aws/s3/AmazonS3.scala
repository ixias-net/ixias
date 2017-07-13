/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3

import slick.jdbc.JdbcProfile
import com.amazonaws.services.s3.model.S3ObjectInputStream
import scala.concurrent.Future

import ixias.aws.s3.model.{ File, FileResource }
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
   * Get File object.
   */
  def get(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(FileTable, "slave") { slick =>
      slick.unique(id).result.headOption
    }

  /**
   * Get File object as `FileResource`.
   */
  def getResource(id: Id): Future[FileResource] =
    get(id) map {
      case None       => FileResource(id)
      case Some(file) => FileResource(file)
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

  // --[ Methods ]--------------------------------------------------------------
  /** @see addViaPresignedUrl */
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
      url       <- client.genPresignedUrl(file.v)
      Some(fid) <- RunDBAction(FileTable) { slick =>
        slick returning slick.map(_.id) += file.v
      }
    } yield (File.Id(fid), url.toString())

  /**
   * Remove the file information and a file object at S3.
   */
  def remove(id: Id): Future[Option[EntityEmbeddedId]] =
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
}
