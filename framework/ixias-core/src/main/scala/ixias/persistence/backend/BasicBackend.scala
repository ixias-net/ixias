/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.concurrent.Future
import scala.collection.mutable.Map
import scala.reflect.runtime.universe.TypeTag

import ixias.util.Logger
import ixias.persistence.model.DataSourceName
import ixias.persistence.dbio.Execution.Implicits.trampoline

/**
 * Backend for the basic database and session handling features.
 */
trait BasicBackend extends BasicDataSource {

  /** The logger for profile */
  protected lazy val logger  = Logger.apply

  /** The type of database objects used by this backend. */
  type Database <: AnyRef

  /** Get a Database instance from connection pool. */
  def getDatabase(dsn: DataSourceName): Future[Database]

}

/**
 * Manage data sources associated with DSN
 */
private[backend] trait BasicBackendContainer[T] {

  /** Shared store */
  protected var resources: Map[Int, T] = Map.empty[Int, T]

  /** Optionally returns the value associated with a DSN */
  def get(dsn: DataSourceName)(implicit tag: TypeTag[T]): Future[Option[T]] = {
    val key = dsn.hashCode * 31 + tag.hashCode
    Future(resources.get(key))
  }

  /** Add a new data souce. */
  def update(dsn: DataSourceName, db: T)(implicit tag: TypeTag[T]): Future[T] = {
    val key = dsn.hashCode * 31 + tag.hashCode
    Future({ resources.update(key, db); db })
  }

  /**
   * If given DSN is already in this map, returns associated data souce.
   * Otherwise, computes value from given expression `op`, stores with key
   * in map and returns that value.
   */
  def getOrElseUpdate(dsn: DataSourceName)(op: => Future[T])(implicit tag: TypeTag[T]): Future[T] = {
    get(dsn).flatMap {
      case Some(db) => Future.successful(db)
      case None     => op.flatMap(db => update(dsn, db))
    }
  }
}
