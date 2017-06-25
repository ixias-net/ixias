/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.util.Try
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import ixias.persistence.model.DataSourceName

trait ShadeConfig extends BasicDatabaseConfig {

  // --[ Properties ]-----------------------------------------------------------
  /** The keys of configuration */
  protected val CF_KEY_PREFIX = "keys_prefix"
  protected val CF_OP_TIMEOUT = "operation_timeout"

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Get the list of server addresses, separated by space.
   */
  protected def getAddresses(dsn: DataSourceName): Try[String] =
    getHosts(dsn).map(_.mkString(","))

  /**
   * Get the prefix to be added to used keys when storing/retrieving values
   * useful for having the same Memcached instances used by several
   * applications to prevent them from stepping over each other.
   */
  protected def getKeysPrefix(dsn: DataSourceName): String =
    readValue(dsn)(_.get[Option[String]](CF_KEY_PREFIX)).getOrElse(dsn.database + "#")

  /**
   * Get the operation timeout; When the limit is reached,
   * the Future responses finish with Failure(TimeoutException)
   */
  protected def getHostSpecIdleTimeout(dsn: DataSourceName): FiniteDuration =
    readValue(dsn)(_.get[Option[FiniteDuration]](CF_OP_TIMEOUT))
      .getOrElse(FiniteDuration(30000, TimeUnit.MILLISECONDS))
}
