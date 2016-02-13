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
import core.port.adapter.persistence.model.DataSourceName

trait ShadeBackend extends BasicBackend with ShadeDataSource {

  /** The type of database objects used by this backend. */
  type Database = Memcached

  /** The cache for Database */
  protected var cache: Map[Int, Memcached] = Map.empty

  /** Get a Database instance from connection pool. */
  def getDatabase(dsn: DataSourceName)(implicit ctx: Context): Try[Memcached] = {
    cache.get(dsn.hashCode) match {
      case Some(v) => Success(v)
      case None    => for {
        ds <- DataSource.forDSN(dsn)
        db <- Try(Memcached(ds))
        _  <- Try(cache.update(dsn.hashCode, db))
      } yield db
    }
  }
}
