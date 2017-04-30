/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.concurrent.Future
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.sns.{ AmazonSNS, AmazonSNSClientBuilder }
import ixias.persistence.model.DataSourceName

trait AmazonSNSDataSouce extends BasicDataSource with AmazonSNSDataConfig {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of database source config used by this backend. */
  type DataSource = AmazonSNS

  /** The type of the database souce config factory used by this backend. */
  type DataSourceFactory = AmazonSNSDataSourceFactory

  // --[ Properties ]-----------------------------------------------------------
  /** The database factory */
  lazy val DataSource = new AmazonSNSDataSourceFactory {}

  // --[ Factory ]--------------------------------------------------------------
  /** Factory methods to create a client for AmazonSNS. */
  trait AmazonSNSDataSourceFactory extends DataSourceFactoryDef {
    def forDSN(dsn: DataSourceName): Future[AmazonSNS] =
      Future.fromTry(
        for {
          credentials <- getAWSCredentials(dsn)
          region      <- getAWSRegion(dsn)
        } yield AmazonSNSClientBuilder.standard()
          .withCredentials(new AWSStaticCredentialsProvider(credentials))
          .withRegion(region)
          .build()
      )
  }
}
