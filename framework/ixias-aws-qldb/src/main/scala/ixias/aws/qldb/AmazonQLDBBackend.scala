/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb

import scala.concurrent.Future
import ixias.persistence.model.DataSourceName
import ixias.persistence.backend.{ BasicBackend, BasicDatabaseContainer }

import software.amazon.qldb.{ QldbSession, PooledQldbDriver }
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.qldbsession.AmazonQLDBSessionClientBuilder

/**
 * The backend to get a client for AmazonQLDB.
 */
object AmazonQLDBBackend extends BasicBackend[AmazonQLDB] with AmazonQLDBConfig {

  /**
   * Get a client to manage Amazon QLDB.
   */
  def getDatabase(implicit dsn: DataSourceName): Future[AmazonQLDB] = {
    logger.debug("Get a database dsn=%s hash=%s".format(dsn.toString, dsn.hashCode))
    AmazonQLDBDatabaseContainer.getOrElseUpdate {
      Future.fromTry {
        for {
          credentials <- getAWSCredentials
          region      <- getAWSRegion
          ledgerName  <- getLedgerName
        } yield {
          val builder = AmazonQLDBSessionClientBuilder.standard
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(region)
          val driver  = PooledQldbDriver.builder
            .withLedger(ledgerName)
            .withRetryLimit(3)
            .withSessionClientBuilder(builder)
            .build
          AmazonQLDB(driver.getSession)
        }
      } andThen {
        case scala.util.Success(_)  => logger.info("Generated a new client. dsn=%s".format(dsn.toString))
        case scala.util.Failure(ex) => logger.error("Failed to build a client. dsn=%s".format(dsn.toString), ex)
      }
    }
  }
}

// The wrapper for AmazonQLDB client
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
case class AmazonQLDB(underlying: QldbSession) {
  // TODO...
}

// Manage data sources associated with DSN.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object AmazonQLDBDatabaseContainer
    extends BasicDatabaseContainer[AmazonQLDB]
