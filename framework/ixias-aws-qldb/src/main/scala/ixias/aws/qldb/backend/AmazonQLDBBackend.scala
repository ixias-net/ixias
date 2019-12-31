/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.backend

import scala.concurrent.Future
import scala.collection.mutable.HashMap
import scala.util.{ Try, Success, Failure }
import ixias.persistence.model.DataSourceName
import ixias.persistence.backend.BasicBackend

import software.amazon.qldb.{ QldbSession, PooledQldbDriver }
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.qldbsession.AmazonQLDBSessionClientBuilder

/**
 * The backend to get a client for AmazonQLDB
 */
object AmazonQLDBBackend extends BasicBackend[QldbSession] with AmazonQLDBConfig {

  val CACHE_DRIVER_MAP  = new HashMap[DataSourceName, PooledQldbDriver]()
  val CACHE_SESSION_MAP = new HashMap[(Long, DataSourceName), QldbSession]()

  /**
   * Get a client to manage Amazon QLDB
   */
  def getDatabase(implicit dsn: DataSourceName): Future[QldbSession] = {
    logger.debug("Get a database dsn=%s hash=%s".format(dsn.toString, dsn.hashCode))
    Future.fromTry(getSession)
  }

  /**
   * Get a Amazon QLDB session
   */
  def getSession(implicit dsn: DataSourceName): Try[QldbSession] =
    this.synchronized {
      val threadId = Thread.currentThread.getId
      CACHE_SESSION_MAP.get((threadId, dsn)) match {
        case Some(session) if !checkSessionIfClosed(session) => {
          Success(session)
        }
        case _ => {
          getDriver.map(driver => {
            val session = driver.getSession
            logger.info("Create a new session. dsn=%s".format(dsn.toString))
            CACHE_SESSION_MAP.update((threadId, dsn), session)
            session
          })
        }
      }
    }

  /**
   * Check if the session is closed
   * [NOTE] Useful only for session verification made with `PooledQldbDriver`
   */
  def checkSessionIfClosed(session: QldbSession): Boolean =
    Try(session.getSessionToken) match {
      case Failure(_) => true
      case Success(_) => false
    }

  /**
   * Get a Amazon QLDB Driver to manage client session
   */
  def getDriver(implicit dsn: DataSourceName): Try[PooledQldbDriver] =
    this.synchronized {
      CACHE_DRIVER_MAP.get(dsn) match {
        case Some(driver) => Success(driver)
        case None         => for {
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
          logger.info("Create a new driver. dsn=%s".format(dsn.toString))
          CACHE_DRIVER_MAP.update(dsn, driver)
          driver
        }
      }
    }
}
