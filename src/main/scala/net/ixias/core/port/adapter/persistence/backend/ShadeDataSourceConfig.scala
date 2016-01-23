/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import scala.util.Try
import java.util.concurrent.TimeUnit

trait ShadeDataSourceConfig extends DataSourceConfig { self: ShadeDataSource =>

  // --[ Properties ]-----------------------------------------------------------
  /** The keys of configuration */
  protected val CF_KEY_PREFIX = "keys_prefix"
  protected val CF_OP_TIMEOUT = "operation_timeout"

  // --[ Methods ]--------------------------------------------------------------
  /** Get the list of server addresses, separated by space. */
  protected def getAddresses(dsn: DataSourceName)(implicit ctx: Context): Try[String] =
    for {
      hosts <- getHosts(dsn)
    } yield (hosts.mkString(","))

  /** Get the prefix to be added to used keys when storing/retrieving values
    * useful for having the same Memcached instances used by several
    * applications to prevent them from stepping over each other. */
  protected def getKeysPrefix(dsn: DataSourceName)(implicit ctx: Context): Option[String] =
    getOptionalValue(dsn)(_.getString(CF_KEY_PREFIX))

  /** Get the operation timeout; When the limit is reached,
    * the Future responses finish with Failure(TimeoutException)*/
  protected def getHostSpecIdleTimeout(dsn: DataSourceName)(implicit ctx: Context): Long =
    getOptionalValue(dsn)(_.getDuration(CF_OP_TIMEOUT, TimeUnit.MILLISECONDS)).getOrElse(30.second)
}
