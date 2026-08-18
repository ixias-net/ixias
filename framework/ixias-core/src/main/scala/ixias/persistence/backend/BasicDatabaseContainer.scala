/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import java.util.concurrent.ConcurrentHashMap

import scala.util.Failure
import scala.concurrent.Future

import ixias.persistence.dbio.Execution
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
    *
    * A failed result is evicted from the map, so that a temporary failure --
    * a database still resuming from a paused state, a network blip -- is not
    * kept for the lifetime of the process. Without the eviction every later
    * call replays that very failure and only a restart brings the data source
    * back.
    */
  def getOrElseUpdate(op: => Future[T])(implicit dsn: DataSourceName): Future[T] = {
    val result = cache.computeIfAbsent(dsn, _ => op)
    // The eviction happens outside of `computeIfAbsent`: the map must not be
    // modified from within its mapping function. A data source that is still
    // being created is evicted by a callback instead.
    result.value match {
      case Some(Failure(_)) => evict(result)
      case Some(_)          => ()
      case None             => result.onComplete {
        case Failure(_) => evict(result)
        case _          => ()
      }(Execution.trampoline)
    }
    result
  }

  /**
    * Drop the entry of given DSN, but only while it still holds `failed`.
    * A data source another thread has created in the meantime is left as is.
    */
  private def evict(failed: Future[T])(implicit dsn: DataSourceName): Unit = {
    cache.remove(dsn, failed)
    ()
  }
}
