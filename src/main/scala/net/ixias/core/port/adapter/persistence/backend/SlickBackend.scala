/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import slick.driver.JdbcProfile
import scala.collection.mutable.Map

trait SlickBackend[P <: JdbcProfile] extends Backend with SlickDataSource {

  /** The type of Slick Jdbc Driver. */
  type Driver = P

  /** The type of database objects used by this backend. */
  type Database = P#Backend#Database

  /** The cache for Database */
  protected var cache: Map[String, Database] = Map.empty

  /** Get a Database instance from connection pool. */
  def getDatabase(driver: Driver, dsn: String)(implicit ctx: Context): Database = {
    val insensitive = dsn.toLowerCase
    cache.getOrElseUpdate(insensitive,
      driver.backend.Database.forSource(DataSource.forDSN(insensitive).get))
  }
}
