/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import java.sql.Connection
import scala.concurrent.Future
import scala.util.{ Success, Failure }

import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }
import slick.jdbc.{ JdbcProfile, JdbcBackend, JdbcDataSource }
import ixias.persistence.model.DataSourceName

/**
 * The slick backend to handle the database and session.
 */
case class SlickBackend[P <: JdbcProfile](val driver: P)
   extends BasicBackend[P#Backend#Database] with SlickConfig {

  /** Get a Database instance from connection pool. */
  def getDatabase(implicit dsn: DataSourceName): Future[Database] =
    SlickDatabaseContainer.getOrElseUpdate {
      (for {
        ds <- createDataSource
        db <- Future(driver.backend.Database.forSource(ds))
      } yield db) andThen {
        case Success(_) => logger.info("Created a new data souce. dsn=%s".format(dsn.toString))
        case Failure(_) => logger.info("Failed to create a data souce. dsn=%s".format(dsn.toString))
      }
    }

  /** Create a JdbcDataSource from DSN (Database Souce Name) */
  def createDataSource(implicit dsn: DataSourceName): Future[HikariCPDataSource] =
    Future.fromTry {
      for {
        driver <- getDriverClassName
        url    <- getJdbcUrl
      } yield {
        val hconf = new HikariConfig()
        hconf.setDriverClassName(driver)
        hconf.setJdbcUrl(url)
        hconf.setPoolName(dsn.toString)
        hconf.addDataSourceProperty("useSSL", false)

        // Optional properties.
        getUserName                  map hconf.setUsername
        getPassword                  map hconf.setPassword
        getHostSpecReadOnly          map hconf.setReadOnly
        getHostSpecMinIdle           map hconf.setMinimumIdle
        getHostSpecMaxPoolSize       map hconf.setMaximumPoolSize
        getHostSpecConnectionTimeout map hconf.setConnectionTimeout
        getHostSpecIdleTimeout       map hconf.setIdleTimeout
        HikariCPDataSource(new HikariDataSource(hconf), hconf)
      }
    }

  /** The DataSource */
  sealed case class HikariCPDataSource (
    val ds:    HikariDataSource,
    val hconf: HikariConfig
  ) extends JdbcDataSource {

    /** The maximum pool size. */
    val maxConnections: Option[Int] = None

    /** Create a new Connection or get one from the pool */
    def createConnection(): Connection = ds.getConnection()

    /**
     * If this object represents a connection pool managed directly by Slick, close it.
     * Otherwise no action is taken.
     */
    def close(): Unit = ds.close()
  }
}

/** Manage data sources associated with DSN */
object SlickDatabaseContainer extends BasicDatabaseContainer[JdbcBackend#Database]
