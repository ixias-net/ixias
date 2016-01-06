/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.config

import scala.util.Try
import core.util.ConfigExt._

trait SlickDataSourceConfig extends DataSourceConfig { self: SlickDataSource =>

  // --[ Properties ]-----------------------------------------------------------
  /** The keys of configuration */
  protected val CF_HOSTSPEC_MIN_IDLE      = """min_idle"""
  protected val CF_HOSTSPEC_MAX_POOL_SIZE = """max_pool_size"""

  /** Jdbc Url formats */
  protected val JDBC_URL_FORMAT_MYSQL    = """jdbc:mysql://%s/%s"""
  protected val JDBC_URL_FORMAT_MYSQL_LB = """jdbc:mysql:loadbalance://%s/%s"""

  // --[ Methods ]--------------------------------------------------------------
  /** Get the JDBC Url */
  protected def getJdbcUrl(dsn: DataSourceName)(implicit ctx: Context): Try[String] =
    for {
      hosts    <- getHosts(dsn)
      database <- getDatabaseName(dsn)
      driver   <- getDriverClassName(dsn)
    } yield {
      driver match {
        case "com.mysql.jdbc.Driver" => hosts.size match {
          case 1 => JDBC_URL_FORMAT_MYSQL.format(hosts.head, database)
          case _ => JDBC_URL_FORMAT_MYSQL_LB.format(hosts.mkString(","), database)
        }
        case _ => throw new Exception(s"""Could not resolve the JDBC vendor format. '$driver'""")
      }
    }

  /** Get the property controls the minimum number of idle connections that
    * Driver tries to maintain in the pool, including both idle and in-use connections. */
  protected def getHostSpecMinIdle(dsn: DataSourceName)(implicit ctx: Context): Option[Int] =
    getOptionalValue(dsn)(_.getInt(CF_HOSTSPEC_MIN_IDLE))

  /** Get the property controls the maximum size that the pool is allowed to reach,
    * including both idle and in-use connections. Basically this value will determine
    * the maximum number of actual connections to the database backend. */
  protected def getHostSpecMaxPoolSize(dsn: DataSourceName)(implicit ctx: Context): Option[Int] =
    getOptionalValue(dsn)(_.getInt(CF_HOSTSPEC_MAX_POOL_SIZE))
}
