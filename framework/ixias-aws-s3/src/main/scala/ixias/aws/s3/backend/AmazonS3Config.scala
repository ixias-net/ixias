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
import com.amazonaws.regions.Regions
import com.amazonaws.auth.{ AWSCredentials, BasicAWSCredentials }
import ixias.util.Configuration

trait AmazonS3Config {

  // --[ Properties ]-----------------------------------------------------------
  // The keys of configuration
  protected val CF_S3_ACCESS_KEY            = "access_key_id"
  protected val CF_S3_SECRET_KEY            = "secret_access_key"
  protected val CF_S3_REGION                = "region"
  protected val CF_S3_BUCKET_NAME           = "bucket_name"
  protected val CF_S3_CONNECTION_TIMEOUT    = "connection_timeout"
  protected val CF_S3_META_TABLE_NAME       = "meta_table_name"
  protected val CF_S3_PRESIGNED_PUT_TIMEOUT = "presigned_put_timeout"
  protected val CF_S3_PRESIGNED_GET_TIMEOUT = "presigned_get_timeout"

  /** The configuration */
  protected val config = Configuration()

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Gets the AWS credentials object.
   */
  protected def getAWSCredentials(implicit dsn: DataSourceName): Try[AWSCredentials] =
    for {
      akey <- getAWSAccessKeyId
      skey <- getAWSSecretKey
    } yield new BasicAWSCredentials(akey, skey)

  /**
   * Gets the AWS access key ID for this credentials object.
   */
  protected def getAWSAccessKeyId(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(
      _.get[Option[String]](CF_S3_ACCESS_KEY)).get
    )

  /**
   * Gets the AWS secret access key for this credentials object.
   */
  protected def getAWSSecretKey(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(
      _.get[Option[String]](CF_S3_SECRET_KEY)).get
    )

  /**
   * Gets a region enum corresponding to the given region name.
   */
  def getAWSRegion(implicit dsn: DataSourceName): Try[Regions] =
    Try(Regions.fromName(readValue(
      _.get[Option[String]](CF_S3_REGION)).get
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
   * Gets the amount of time to wait (in milliseconds)
   * when initially establishing a connection before giving up and timing out.
   */
  def getConnectionTimeout(implicit dsn: DataSourceName): Long =
    readValue(
      _.get[Option[Duration]](CF_S3_CONNECTION_TIMEOUT).map(_.toMillis)
    ).getOrElse(30000L)

  /**
   * Gets the expiration date at which point
   * the new pre-signed URL will no longer be accepted to get a file by Amazon S3.
   * Default timeout value : 15 mins
   */
  def getPresignedUrlTimeoutForGet(implicit dsn: DataSourceName): java.util.Date =
    new java.util.Date(System.currentTimeMillis() + readValue(
      _.get[Option[Duration]](CF_S3_PRESIGNED_GET_TIMEOUT).map(_.toMillis))
       .getOrElse(1500000L)
    )

  /**
   * Gets the expiration date at which point
   * the new pre-signed URL will no longer be accepted to upload a file by Amazon S3.
   * Default timeout value : 5 mins
   */
  def getPresignedUrlTimeoutForUpload(implicit dsn: DataSourceName): java.util.Date =
    new java.util.Date(System.currentTimeMillis() + readValue(
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

