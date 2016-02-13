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
import slick.driver.JdbcProfile

trait SlickBackend[P <: JdbcProfile] extends Backend with SlickDataSource {

  /** The type of Slick Jdbc Driver. */
  type Driver = P

  /** The type of database objects used by this backend. */
  type Database = P#Backend#Database

  /** The configured driver. */
  val driver: P

  /** The cache for Database */
  protected var cache: Map[String, Database] = Map.empty

  /** Get a Database instance from connection pool. */
  def getDatabase(dsn: String)(implicit ctx: Context): Try[Database] = {
    val insensitive = dsn.toLowerCase
    cache.get(insensitive) match {
      case Some(v) => Success(v)
      case None    => for {
        ds <- DataSource.forDSN(insensitive)
      } yield { val db = driver.backend.Database.forSource(ds); cache.update(insensitive, db); db }
    }
  }
}
