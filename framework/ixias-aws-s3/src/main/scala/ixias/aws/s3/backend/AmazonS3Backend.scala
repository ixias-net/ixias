/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.backend

import scala.concurrent.Future
import scala.util.{ Success, Failure }
import ixias.util.Logger
import ixias.persistence.dbio.Execution
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder

/**
 * The backend to get a client for AmazonS3.
 */
object AmazonS3Backend extends AmazonS3Config {

  /** The logger for profile */
  protected lazy val logger  = Logger.apply

  /** The Execution Context */
  protected implicit val ctx = Execution.Implicits.trampoline

  /** Get a client to manage Amazon S3. */
  def getClient(implicit dsn: DataSourceName): Future[AmazonS3] = {
    logger.debug("Get a database dsn=%s hash=%s".format(dsn.toString, dsn.hashCode))
    Future.fromTry(
      for {
        credentials <- getAWSCredentials
        region      <- getAWSRegion
      } yield {
        val conf = new ClientConfiguration
        conf.setConnectionTimeout(getConnectionTimeout.toInt)
        AmazonS3(AmazonS3ClientBuilder.standard
          .withCredentials(new AWSStaticCredentialsProvider(credentials))
          .withRegion(region)
          .withPathStyleAccessEnabled(true)
          .build)
      }
    ) andThen {
      case Success(_) => logger.info("Generated a new client. dsn=%s".format(dsn.toString))
      case Failure(_) => logger.info("Failed to build a client. dsn=%s".format(dsn.toString))
    }
  }

  // The wrapper for AmazonS3 client
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  protected case class AmazonS3(underlying: com.amazonaws.services.s3.AmazonS3) {
    import ixias.aws.s3.model._
    import com.amazonaws.HttpMethod
    import com.amazonaws.services.s3.model._

    /** Gets the object stored in Amazon S3 under the specified bucket and key. */
    def load(file: File): Future[S3Object] =
      Future(underlying.getObject(new GetObjectRequest(
        file.bucket,
        file.key
      )))

    /**
     * Gets a pre-signed URL for accessing an Amazon S3 resource.
     */
    def genPresignedUrlForAccess(file: File)(implicit dsn: DataSourceName): Future[java.net.URL] =
      Future({
        val req = new GeneratePresignedUrlRequest(file.bucket, file.key)
        req.setMethod(HttpMethod.GET)
        req.setExpiration(getPresignedUrlTimeoutForGet)
        underlying.generatePresignedUrl(req)
      })

    /**
     * Gets a pre-signed URL to upload an Amazon S3 resource.
     */
    def genPresignedUrlForUpload(file: File)(implicit dsn: DataSourceName): Future[java.net.URL] =
      Future({
        val req = new GeneratePresignedUrlRequest(file.bucket, file.key)
        req.setMethod(HttpMethod.PUT)
        req.setContentType(file.typedef)
        req.setExpiration(getPresignedUrlTimeoutForUpload)
        underlying.generatePresignedUrl(req)
      })

    /**
     * Uploads a new object to the specified Amazon S3 bucket.
     */
    def upload(s3object: S3Object): Future[Unit] =
      Future(underlying.putObject(new PutObjectRequest(
        s3object.getBucketName,
        s3object.getKey,
        s3object.getObjectContent,
        s3object.getObjectMetadata
      )))

    /**
     * Uploads the specified file to Amazon S3 under the specified bucket and key name.
     */
    def upload(file: File, content: java.io.File): Future[Unit] =
      Future(underlying.putObject(new PutObjectRequest(
        file.bucket,
        file.key,
        content
      )))

    /**
     * Deletes the specified object in the specified bucket.
     */
    def remove(file: File): Future[Unit] =
      Future(underlying.deleteObject(new DeleteObjectRequest(file.bucket, file.key)))

    /**
     * Deletes the file object list in the specified bucket.
     */
    def bulkRemove(bucket: String, fileSeq: Seq[File]): Future[Unit] = {
      Future(underlying.deleteObjects(new DeleteObjectsRequest(bucket).withKeys(fileSeq.map(file => file.key).toArray:_*)))
    }
  }
}

