/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.backend

import scala.concurrent.{ Future, ExecutionContextExecutor }
import scala.util.{ Success, Failure }
import ixias.util.Logger
import ixias.util.ChainSyntax
import ixias.persistence.dbio.Execution
import software.amazon.awssdk.auth.credentials.{ AwsCredentialsProvider, StaticCredentialsProvider }
import software.amazon.awssdk.services.s3.{ S3Client, S3Configuration }
import software.amazon.awssdk.services.s3.presigner.S3Presigner

/**
 * The backend to get a client for AmazonS3.
 */
object AmazonS3Backend extends AmazonS3Config with ChainSyntax {

  /** The logger for profile */
  protected lazy val logger  = Logger.apply

  /** The Execution Context */
  protected implicit val ctx: ExecutionContextExecutor = Execution.Implicits.trampoline

  /**
   * The service configuration shared by the client and the presigner.
   * Path style access keeps a bucket name in the URL path instead of
   * the host name, so both must be built with the same setting to make
   * a pre-signed URL match the URL the client itself would address.
   */
  protected val serviceConfiguration =
    S3Configuration.builder.pathStyleAccessEnabled(true).build

  /** Get a client to manage Amazon S3. */
  def getClient(implicit dsn: DataSourceName): Future[AmazonS3] = {
    logger.debug("Get a database dsn=%s hash=%s".format(dsn.toString, dsn.hashCode))
    Future.fromTry(
      getAWSRegion.map { region =>
        // Attach static credentials only when configured; otherwise fall
        // through to the default provider chain (the server ExecutionRole /
        // ECS task role), which is the recommended setup.
        val credentials: Option[AwsCredentialsProvider] =
          getAWSCredentials.map(StaticCredentialsProvider.create)
        val client = S3Client.builder
          .region(region)
          .serviceConfiguration(serviceConfiguration)
          .pipe(b => credentials.fold(b)(c => b.credentialsProvider(c)))
          .build
        val presigner = S3Presigner.builder
          .region(region)
          .serviceConfiguration(serviceConfiguration)
          .pipe(b => credentials.fold(b)(c => b.credentialsProvider(c)))
          .build
        AmazonS3(client, presigner)
      }
    ) andThen {
      case Success(_) => logger.info("Generated a new client. dsn=%s".format(dsn.toString))
      case Failure(_) => logger.info("Failed to build a client. dsn=%s".format(dsn.toString))
    }
  }

  // The wrapper for AmazonS3 client
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  protected case class AmazonS3(underlying: S3Client, presigner: S3Presigner) {
    import java.io.InputStream
    import scala.jdk.CollectionConverters._
    import ixias.aws.s3.model._
    import software.amazon.awssdk.core.sync.RequestBody
    import software.amazon.awssdk.services.s3.model._
    import software.amazon.awssdk.services.s3.presigner.model.{ GetObjectPresignRequest, PutObjectPresignRequest }

    /**
     * Gets the object stored in Amazon S3 under the specified bucket and key.
     * The caller is responsible for closing the returned stream.
     */
    def load(file: File): Future[InputStream] =
      Future(underlying.getObject(GetObjectRequest.builder
        .bucket(file.bucket)
        .key(file.key)
        .build
      ))

    /**
     * Gets a pre-signed URL for accessing an Amazon S3 resource.
     */
    def genPresignedUrlForAccess(file: File)(implicit dsn: DataSourceName): Future[java.net.URL] =
      Future(presigner.presignGetObject(GetObjectPresignRequest.builder
        .signatureDuration(getPresignedUrlTimeoutForGet)
        .getObjectRequest(GetObjectRequest.builder
          .bucket(file.bucket)
          .key(file.key)
          .build)
        .build
      ).url)

    /**
     * Gets a pre-signed URL to upload an Amazon S3 resource.
     * The client uploading through it must send a matching `Content-Type` header.
     */
    def genPresignedUrlForUpload(file: File)(implicit dsn: DataSourceName): Future[java.net.URL] =
      Future(presigner.presignPutObject(PutObjectPresignRequest.builder
        .signatureDuration(getPresignedUrlTimeoutForUpload)
        .putObjectRequest(PutObjectRequest.builder
          .bucket(file.bucket)
          .key(file.key)
          .contentType(file.typedef)
          .build)
        .build
      ).url)

    /**
     * Uploads a new object to the specified Amazon S3 bucket.
     * `contentLength` is required since the SDK has to know the payload size
     * before it starts streaming the content.
     */
    def upload(file: File, content: java.io.InputStream, contentLength: Long): Future[Unit] =
      Future(underlying.putObject(
        PutObjectRequest.builder
          .bucket(file.bucket)
          .key(file.key)
          .contentLength(contentLength)
          .build,
        RequestBody.fromInputStream(content, contentLength)
      ))

    /**
     * Uploads the specified file to Amazon S3 under the specified bucket and key name.
     */
    def upload(file: File, content: java.io.File): Future[Unit] =
      Future(underlying.putObject(
        PutObjectRequest.builder
          .bucket(file.bucket)
          .key(file.key)
          .build,
        RequestBody.fromFile(content)
      ))

    /**
     * Deletes the specified object in the specified bucket.
     */
    def remove(file: File): Future[Unit] =
      Future(underlying.deleteObject(DeleteObjectRequest.builder
        .bucket(file.bucket)
        .key(file.key)
        .build
      ))

    /**
     * Deletes the file object list in the specified bucket.
     *
     * A batch delete can fail for only some of its keys. Where v1 raised
     * `MultiObjectDeleteException` for that case, v2 completes normally and
     * reports the failures in the response body, so they have to be turned
     * back into a failure here.
     */
    def bulkRemove(bucket: String, fileSeq: Seq[File]): Future[Unit] =
      Future(underlying.deleteObjects(DeleteObjectsRequest.builder
        .bucket(bucket)
        .delete(Delete.builder.objects(
          fileSeq.map(f => ObjectIdentifier.builder.key(f.key).build).asJava
        ).build)
        .build
      )) map { response =>
        val errors = response.errors.asScala
        if (errors.nonEmpty) throw new IllegalStateException(
          "Failed to delete %d of %d objects in the bucket %s. %s".format(
            errors.size, fileSeq.size, bucket,
            errors.map(e => "%s (%s: %s)".format(e.key, e.code, e.message)).mkString(", ")
          )
        )
      }
  }
}
