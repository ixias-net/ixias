/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.config

import core.util.ConfigExt._

trait SlickDataSourceConfig extends DataSourceConfig { self: SlickDataSource =>

  // --[ Properties ]-----------------------------------------------------------
  /** The keys of configuration */
  protected val CF_HOSTSPEC_MIN_IDLE      = """min_idle"""
  protected val CF_HOSTSPEC_MAX_POOL_SIZE = """max_pool_size"""

  // --[ Methods ]--------------------------------------------------------------
  /** Get the property controls the minimum number of idle connections that
    * Driver tries to maintain in the pool, including both idle and in-use connections. */
  protected def getHostSpecMinIdle
    (dsn: DataSourceName)(implicit ctx: Context): Option[Int] =
    getOptionalValue(dsn)(_.getInt(CF_HOSTSPEC_MIN_IDLE))

  /** Get the property controls the maximum size that the pool is allowed to reach,
    * including both idle and in-use connections. Basically this value will determine
    * the maximum number of actual connections to the database backend. */
  protected def getHostSpecMaxPoolSize
    (dsn: DataSourceName)(implicit ctx: Context): Option[Int] =
    getOptionalValue(dsn)(_.getInt(CF_HOSTSPEC_MAX_POOL_SIZE))
}
