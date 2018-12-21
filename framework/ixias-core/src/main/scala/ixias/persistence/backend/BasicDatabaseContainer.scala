/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
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
  def get(implicit dsn: DataSourceName): Option[T] =
    cache.get(dsn.hashCode)

  /** Add a new data souce. */
  def update(db: T)(implicit dsn: DataSourceName): Unit =
    cache.update(dsn.hashCode, db)

  /**
   * If given DSN is already in this map, returns associated data souce.
   * Otherwise, computes value from given expression `op`, stores with key
   * in map and returns that value.
   */
  def getOrElseUpdate(op: => Future[T])(implicit dsn: DataSourceName): Future[T] =
    get match {
      case Some(db) => Future.successful(db)
      case None     => op.map(db => { update(db); db })
    }
}
