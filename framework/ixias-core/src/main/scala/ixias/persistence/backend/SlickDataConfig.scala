/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.util.Try
import java.util.concurrent.TimeUnit
import ixias.persistence.model.DataSourceName

trait SlickDataConfig extends BasicDataConfig {

  // --[ Properties ]-----------------------------------------------------------
  /** The keys of configuration */
  protected val CF_HOSTSPEC_MIN_IDLE           = "min_idle"
  protected val CF_HOSTSPEC_MAX_POOL_SIZE      = "max_pool_size"
  protected val CF_HOSTSPEC_CONNECTION_TIMEOUT = "connection_timeout"
  protected val CF_HOSTSPEC_IDLE_TIMEOUT       = "idle_timeout"

  /** Jdbc Url formats */
  protected val JDBC_URL_FORMAT_MYSQL    = """jdbc:mysql://%s/%s"""
  protected val JDBC_URL_FORMAT_MYSQL_LB = """jdbc:mysql:loadbalance://%s/%s"""

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Get the property controls the minimum number of idle connections that
   * Driver tries to maintain in the pool, including both idle and in-use connections.
   */
  protected def getHostSpecMinIdle(dsn: DataSourceName): Option[Int] =
    readValue(dsn)(_.getInt(CF_HOSTSPEC_MIN_IDLE))

  /**
   * Get the property controls the maximum size that the pool is allowed to reach,
   * including both idle and in-use connections. Basically this value will determine
   * the maximum number of actual connections to the database backend.
   */
  protected def getHostSpecMaxPoolSize(dsn: DataSourceName): Option[Int] =
    readValue(dsn)(_.getInt(CF_HOSTSPEC_MAX_POOL_SIZE))

  /**
   * Get the maximum number of milliseconds that a client will wait for
   * a connection from the pool. If this time is exceeded without
   * a connection becoming available, a SQLException will be thrown from
   */
  protected def getHostSpecConnectionTimeout(dsn: DataSourceName): Option[Long] =
    readValue(dsn)(_.getMilliseconds(CF_HOSTSPEC_CONNECTION_TIMEOUT))

  /**
   * This property controls the maximum amount of time (in milliseconds) that
   * a connection is allowed to sit idle in the pool.
   * Whether a connection is retired as idle or not is subject to
   * a maximum variation of +30 seconds, and average variation of +15 seconds.
   * A connection will never be retired as idle before this timeout.
   * A value of 0 means that idle connections are never removed from the pool.
   */
  protected def getHostSpecIdleTimeout(dsn: DataSourceName): Option[Long] =
    readValue(dsn)(_.getMilliseconds(CF_HOSTSPEC_IDLE_TIMEOUT))

  /**
   * Get the JDBC Url
   */
  protected def getJdbcUrl(dsn: DataSourceName): Try[String] =
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
}
