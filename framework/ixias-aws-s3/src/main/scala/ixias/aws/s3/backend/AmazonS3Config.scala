/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.backend

import scala.util.Try
import scala.concurrent.duration.Duration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.auth.credentials.{ AwsCredentials, AwsBasicCredentials }
import ixias.util.Configuration

trait AmazonS3Config {

  // --[ Properties ]-----------------------------------------------------------
  // The keys of configuration
  protected val CF_S3_ACCESS_KEY            = "access_key_id"
  protected val CF_S3_SECRET_KEY            = "secret_access_key"
  protected val CF_S3_REGION                = "region"
  protected val CF_S3_BUCKET_NAME           = "bucket_name"
  protected val CF_S3_META_TABLE_NAME       = "meta_table_name"
  protected val CF_S3_PRESIGNED_PUT_TIMEOUT = "presigned_put_timeout"
  protected val CF_S3_PRESIGNED_GET_TIMEOUT = "presigned_get_timeout"
  // Common (non-backend-scoped) fallback for the region, so a project can pin
  // it once at the top level instead of repeating it in every backend block.
  protected val CF_AWS_REGION_COMMON        = "aws.region"

  /** The configuration */
  protected val config = Configuration()

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Gets the static AWS credentials configured for this backend, if any.
   *
   * Returns None when either the access key or the secret key is absent. In
   * that case the caller must NOT set static credentials on the client and
   * should let the AWS default credential provider chain resolve them instead
   * -- i.e. run under the server's ExecutionRole (the ECS task role / EC2
   * instance role), which is the recommended setup. Static keys are only for
   * local or non-role environments.
   */
  protected def getAWSCredentials(implicit dsn: DataSourceName): Option[AwsCredentials] =
    for {
      akey <- getAWSAccessKeyId
      skey <- getAWSSecretKey
    } yield AwsBasicCredentials.create(akey, skey)

  /**
   * Gets the AWS access key ID, if configured.
   */
  protected def getAWSAccessKeyId(implicit dsn: DataSourceName): Option[String] =
    readValue(_.get[Option[String]](CF_S3_ACCESS_KEY))

  /**
   * Gets the AWS secret access key, if configured.
   */
  protected def getAWSSecretKey(implicit dsn: DataSourceName): Option[String] =
    readValue(_.get[Option[String]](CF_S3_SECRET_KEY))

  /**
   * Gets a region corresponding to the given region name.
   *
   * Resolution order: the backend-scoped `region` (via `readValue`), then the
   * common top-level `aws.region`. Fails only when neither is set.
   */
  def getAWSRegion(implicit dsn: DataSourceName): Try[Region] =
    Try(Region.of(
      readValue(_.get[Option[String]](CF_S3_REGION))
        .orElse(config.get[Option[String]](CF_AWS_REGION_COMMON))
        .get
    ))

  /**
   * Gets the name of the bucket where this request will upload a new object to.
   * In order to upload the object, users must have Permission.Write permission granted.
   */
  def getBucketName(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(
      _.get[Option[String]](CF_S3_BUCKET_NAME)).get
    )

  /**
   * Gets the duration for which the new pre-signed URL
   * will be accepted to get a file by Amazon S3.
   * Default timeout value : 25 mins
   */
  def getPresignedUrlTimeoutForGet(implicit dsn: DataSourceName): java.time.Duration =
    java.time.Duration.ofMillis(readValue(
      _.get[Option[Duration]](CF_S3_PRESIGNED_GET_TIMEOUT).map(_.toMillis))
       .getOrElse(1500000L)
    )

  /**
   * Gets the duration for which the new pre-signed URL
   * will be accepted to upload a file by Amazon S3.
   * Default timeout value : approx. 8 mins
   */
  def getPresignedUrlTimeoutForUpload(implicit dsn: DataSourceName): java.time.Duration =
    java.time.Duration.ofMillis(readValue(
      _.get[Option[Duration]](CF_S3_PRESIGNED_PUT_TIMEOUT).map(_.toMillis))
       .getOrElse(500000L)
    )

  /**
   * Gets the table name which is containing META-INFO of storage object.
   */
  def getMetaTableName(implicit dsn: DataSourceName): String =
    readValue(_.get[Option[String]](CF_S3_META_TABLE_NAME))
      .getOrElse("aws_s3_file")

  /**
   * Get a value by specified key.
   */
  def readValue[A](f: Configuration => Option[A])(implicit dsn: DataSourceName): Option[A] =
    (dsn.name.toSeq.map(
      name => dsn.path + "." + dsn.resource + "." + name
    ) ++ Seq(
      dsn.path + "." + dsn.resource,
      dsn.path
    )).foldLeft[Option[A]](None) {
      case (prev, path) => prev.orElse(f(config.get[Configuration](path)))
    }
}

