/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.concurrent.Future
import scala.collection.mutable.Map
import ixias.persistence.model.DataSourceName
import ixias.persistence.dbio.Execution.Implicits.trampoline

/**
 * The container to manage databse base associated with DSN
 */
private[backend] trait BasicDatabaseContainer[T] {

  /** Shared store */
  protected var cache: Map[Int, T] = Map.empty[Int, T]

  /** Optionally returns the value associated with a DSN */
  def get(dsn: DataSourceName): Option[T] =
    cache.get(dsn.hashCode)

  /** Add a new data souce. */
  def update(dsn: DataSourceName, db: T): Unit =
    cache.update(dsn.hashCode, db)

  /**
   * If given DSN is already in this map, returns associated data souce.
   * Otherwise, computes value from given expression `op`, stores with key
   * in map and returns that value.
   */
  def getOrElseUpdate(dsn: DataSourceName)(op: => Future[T]): Future[T] =
    get(dsn) match {
      case Some(db) => Future.successful(db)
      case None     => op.map(db => { update(dsn, db); db })
    }
}
