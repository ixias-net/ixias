/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import java.util.concurrent.ConcurrentHashMap

import scala.concurrent.Future
import ixias.persistence.model.DataSourceName


/**
  * The container to manage databse base associated with DSN
  */
trait BasicDatabaseContainer[T] {

  /** Shared store */
  protected var cache = new ConcurrentHashMap[DataSourceName, Future[T]]()

  /**
    * If given DSN is already in this map, returns associated data souce.
    * Otherwise, computes value from given expression `op`, stores with key
    * in map and returns that value.
    */
  def getOrElseUpdate(op: => Future[T])(implicit dsn: DataSourceName): Future[T] =
    cache.computeIfAbsent(dsn, _ => op)
}
