/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import scala.concurrent.Future
import scala.collection.mutable.Map
import scala.concurrent.ExecutionContext.Implicits.global

import slick.driver.JdbcProfile
import core.port.adapter.persistence.model.DataSourceName

trait SlickBackend[P <: JdbcProfile] extends BasicBackend with SlickDataSource {

  /** The type of Slick Jdbc Driver. */
  type Driver = P

  /** The type of database objects used by this backend. */
  type Database = P#Backend#Database

  /** The configured driver. */
  val driver: P

  /** The cache for Database */
  protected var cache: Map[Int, Database] = Map.empty

  /** Get a Database instance from connection pool. */
  def getDatabase(dsn: DataSourceName)(implicit ctx: Context): Future[Database] = {
    cache.get(dsn.hashCode) match {
      case Some(v) => Future.successful(v)
      case None    => for {
        ds <- DataSource.forDSN(dsn)
        db <- Future(driver.backend.Database.forSource(ds))
        _  <- Future(cache.update(dsn.hashCode, db))
      } yield db
    }
  }
}
