/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import scala.collection.mutable.Map
import scala.concurrent.ExecutionContext.Implicits.global
import shade.memcached.Memcached

trait ShadeBackend extends Backend with ShadeDataSource {

  /** The cache for Database */
  protected var cache: Map[String, Memcached] = Map.empty

  /** Get a Database instance from connection pool. */
  def getDatabase(dsn: String)(implicit ctx: Context): Memcached = {
    val insensitive = dsn.toLowerCase
    cache.getOrElseUpdate(insensitive,
      Memcached(DataSource.forDSN(insensitive).get))
  }
}
