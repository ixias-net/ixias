/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import scala.util.{ Try, Success }
import scala.collection.mutable.Map
import scala.concurrent.ExecutionContext.Implicits.global
import shade.memcached.Memcached

trait ShadeBackend extends Backend with ShadeDataSource {

  /** The type of database objects used by this backend. */
  type Database = Memcached

  /** The cache for Database */
  protected var cache: Map[String, Memcached] = Map.empty

  /** Get a Database instance from connection pool. */
  def getDatabase(dsn: String)(implicit ctx: Context): Try[Memcached] = {
    val insensitive = dsn.toLowerCase
    cache.get(insensitive) match {
      case Some(v) => Success(v)
      case None    => for {
        ds <- DataSource.forDSN(insensitive)
      } yield { val db = Memcached(ds); cache.update(insensitive, db); db }
    }
  }
}
