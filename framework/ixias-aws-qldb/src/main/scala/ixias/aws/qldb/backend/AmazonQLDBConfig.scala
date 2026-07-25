/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.backend

import scala.util.Try
import com.amazonaws.regions.Regions
import com.amazonaws.auth.{ AWSCredentials, BasicAWSCredentials }

import ixias.persistence.model.DataSourceName
import ixias.persistence.backend.BasicDatabaseConfig

trait AmazonQLDBConfig extends BasicDatabaseConfig {

  // --[ Properties ]-----------------------------------------------------------
  // The keys of configuration
  protected val CF_QLDB_ACCESS_KEY  = "access_key_id"
  protected val CF_QLDB_SECRET_KEY  = "secret_access_key"
  protected val CF_QLDB_REGION      = "region"
  protected val CF_QLDB_LEDGER_NAME = "ledger_name"
  // Common (non-backend-scoped) fallback for the region.
  protected val CF_AWS_REGION_COMMON = "aws.region"

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Gets the static AWS credentials configured for this backend, if any.
   *
   * Returns None when either key is absent, in which case the caller must let
   * the AWS default credential provider chain resolve credentials instead --
   * i.e. run under the server's ExecutionRole (the recommended setup).
   */
  def getAWSCredentials(implicit dsn: DataSourceName): Option[AWSCredentials] =
    for {
      akey <- getAWSAccessKeyId
      skey <- getAWSSecretKey
    } yield new BasicAWSCredentials(akey, skey)

  /**
   * Gets the AWS access key ID, if configured.
   */
  protected def getAWSAccessKeyId(implicit dsn: DataSourceName): Option[String] =
    readValue(_.get[Option[String]](CF_QLDB_ACCESS_KEY))

  /**
   * Gets the AWS secret access key, if configured.
   */
  protected def getAWSSecretKey(implicit dsn: DataSourceName): Option[String] =
    readValue(_.get[Option[String]](CF_QLDB_SECRET_KEY))

  /**
   * Gets a region enum corresponding to the given region name.
   *
   * Resolution order: the backend-scoped `region`, then the common top-level
   * `aws.region`. Fails only when neither is set.
   */
  def getAWSRegion(implicit dsn: DataSourceName): Try[Regions] =
    Try(Regions.fromName(
      readValue(_.get[Option[String]](CF_QLDB_REGION))
        .orElse(config.get[Option[String]](CF_AWS_REGION_COMMON))
        .get
    ))

  /**
   * Gets the name of the Ledger
   */
  def getLedgerName(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(
      _.get[Option[String]](CF_QLDB_LEDGER_NAME)).get
    )
}
