/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.concurrent.Future
import java.sql.Connection

import slick.jdbc.JdbcDataSource
import ixias.persistence.model.DataSourceName

trait SlickDataSource extends BasicDataSource with SlickDataSourceConfig
{
  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of database source config used by this backend. */
  type DataSource = HikariCPDataSource

  /** The type of the database souce config factory used by this backend. */
  type DataSourceFactory = HikariCPDataSourceFactory

  // --[ Properties ]-----------------------------------------------------------
  /** The database factory */
  lazy val DataSource = new HikariCPDataSourceFactory{}

  // --[ DataSource ]-----------------------------------------------------------
  case class HikariCPDataSource (
    val ds:    com.zaxxer.hikari.HikariDataSource,
    val hconf: com.zaxxer.hikari.HikariConfig
  ) extends JdbcDataSource {

    /** Create a new Connection or get one from the pool */
    def createConnection(): Connection = ds.getConnection()

    /**
     * If this object represents a connection pool managed directly by Slick, close it.
     * Otherwise no action is taken.
     */
    def close(): Unit = ds.close()
  }

  // --[ Factory ]--------------------------------------------------------------
  /**
   * Factory methods for creating `DatabSouce` instances with using HikariCP.
   */
  trait HikariCPDataSourceFactory extends DataSourceFactoryDef {
    import com.zaxxer.hikari._

    /** Create a JdbcDataSource from DSN (Database Souce Name) */
    def forDSN(dsn: DataSourceName): Future[DataSource] =
      Future.fromTry{
        for {
          driver <- getDriverClassName(dsn)
          url    <- getJdbcUrl(dsn)
        } yield {
          val hconf = new HikariConfig()
          hconf.setDriverClassName(driver)
          hconf.setJdbcUrl(url)
          hconf.setPoolName(dsn.toString)

          // Optional properties.
          getUserName(dsn)                  map hconf.setUsername
          getPassword(dsn)                  map hconf.setPassword
          getHostSpecReadOnly(dsn)          map hconf.setReadOnly
          getHostSpecMinIdle(dsn)           map hconf.setMinimumIdle
          getHostSpecMaxPoolSize(dsn)       map hconf.setMaximumPoolSize
          getHostSpecConnectionTimeout(dsn) map hconf.setConnectionTimeout
          getHostSpecIdleTimeout(dsn)       map hconf.setIdleTimeout

          HikariCPDataSource(new HikariDataSource(hconf), hconf)
        }
      }
  }
}

