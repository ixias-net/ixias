/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.util.Try
import scala.concurrent.duration.Duration
import ixias.persistence.model.DataSourceName

trait SlickConfig extends BasicDatabaseConfig {

  // --[ Properties ]-----------------------------------------------------------
  /** The keys of configuration */
  protected val CF_HOSTSPEC_MIN_IDLE           = "min_idle"
  protected val CF_HOSTSPEC_MAX_POOL_SIZE      = "max_pool_size"
  protected val CF_HOSTSPEC_CONNECTION_TIMEOUT = "connection_timeout"
  protected val CF_HOSTSPEC_IDLE_TIMEOUT       = "idle_timeout"

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Get the property controls the minimum number of idle connections that
   * Driver tries to maintain in the pool, including both idle and in-use connections.
   */
  protected def getHostSpecMinIdle(implicit dsn: DataSourceName): Option[Int] =
    readValue(_.get[Option[Int]](CF_HOSTSPEC_MIN_IDLE))

  /**
   * Get the property controls the maximum size that the pool is allowed to reach,
   * including both idle and in-use connections. Basically this value will determine
   * the maximum number of actual connections to the database backend.
   */
  protected def getHostSpecMaxPoolSize(implicit dsn: DataSourceName): Option[Int] =
    readValue(_.get[Option[Int]](CF_HOSTSPEC_MAX_POOL_SIZE))

  /**
   * Get the maximum number of milliseconds that a client will wait for
   * a connection from the pool. If this time is exceeded without
   * a connection becoming available, a SQLException will be thrown from
   */
  protected def getHostSpecConnectionTimeout(implicit dsn: DataSourceName): Option[Long] =
    readValue(_.get[Option[Duration]](CF_HOSTSPEC_CONNECTION_TIMEOUT).map(_.toMillis))

  /**
   * This property controls the maximum amount of time (in milliseconds) that
   * a connection is allowed to sit idle in the pool.
   * Whether a connection is retired as idle or not is subject to
   * a maximum variation of +30 seconds, and average variation of +15 seconds.
   * A connection will never be retired as idle before this timeout.
   * A value of 0 means that idle connections are never removed from the pool.
   */
  protected def getHostSpecIdleTimeout(implicit dsn: DataSourceName): Option[Long] =
    readValue(_.get[Option[Duration]](CF_HOSTSPEC_IDLE_TIMEOUT).map(_.toMillis))

  /**
   * Get the JDBC Url
   */
  protected def getJdbcUrl(implicit dsn: DataSourceName): Try[String] =
    for {
      driver  <- getDriverClassName
      builder <- SlickJdbcUrlBuilderProvider.resolve(driver)
      jdbcUrl <- builder.buildUrl
    } yield jdbcUrl
}
