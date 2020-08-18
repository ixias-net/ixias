/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.dynamodb.backend

import scala.util.Try
import com.amazonaws.regions.Regions
import com.amazonaws.auth.{ BasicAWSCredentials, AWSStaticCredentialsProvider }

import ixias.persistence.model.DataSourceName
import ixias.persistence.backend.BasicDatabaseConfig

trait AmazonDynamoDBConfig extends BasicDatabaseConfig {

  // --[ Properties ]-----------------------------------------------------------
  // The keys of configuration
  protected val CF_DDB_ACCESS_KEY  = "access_key_id"
  protected val CF_DDB_SECRET_KEY  = "secret_access_key"
  protected val CF_DDB_REGION      = "region"

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Gets the AWS credentials object.
   */
  def getAWSCredentials(implicit dsn: DataSourceName): Try[AWSStaticCredentialsProvider] =
    for {
      akey <- getAWSAccessKeyId
      skey <- getAWSSecretKey
    } yield new AWSStaticCredentialsProvider(
      new BasicAWSCredentials(akey, skey)
    )

  /**
   * Gets the AWS access key ID for this credentials object.
   */
  protected def getAWSAccessKeyId(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(
      _.get[Option[String]](CF_DDB_ACCESS_KEY)).get
    )

  /**
   * Gets the AWS secret access key for this credentials object.
   */
  protected def getAWSSecretKey(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(
      _.get[Option[String]](CF_DDB_SECRET_KEY)).get
    )

  /**
   * Gets a region enum corresponding to the given region name.
   */
  def getAWSRegion(implicit dsn: DataSourceName): Try[Regions] =
    Try(Regions.fromName(readValue(
      _.get[Option[String]](CF_DDB_REGION)).get
    ))
}
