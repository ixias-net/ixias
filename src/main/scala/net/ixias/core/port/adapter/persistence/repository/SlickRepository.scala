/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core
package port.adapter.persistence.repository

import slick.driver.JdbcProfile
import com.typesafe.config.{ Config, ConfigFactory }
import domain.model.{ Identity, Entity }
import port.adapter.persistence.lifted._
import port.adapter.persistence.backend.SlickBackend

/**
 * The repository for persistence with using the Slick library.
 */
trait SlickRepository[K <: Identity[_], V <: Entity[K], P <: JdbcProfile]
    extends BasicRepository[K, V] with SlickActionComponent[K, V, P] { self =>

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The back-end type required by this profile */
  type Backend  = SlickBackend[P]
  /** The type of database objects. */
  type Database = Backend#Database

  // --[ Properties ]-----------------------------------------------------------
  /** The configured driver. */
  val driver: P
  /** The back-end implementation for this profile */
  val backend = new SlickBackend[P] {}

  // --[ Methods ]--------------------------------------------------------------
  def withDatabase[T](dsn:String)(f: Database => T)(implicit ctx: Context): T =
    f(backend.getDatabase(driver, dsn))

  // --[ API ]------------------------------------------------------------------
  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  trait API extends super.API with driver.API
      with SlickColumnOptionOps
      with SlickColumnTypeOps[P]
  override val api: API = new API { lazy val driver = self.driver }
}

trait SlickActionComponent[K <: Identity[_], V <: Entity[K], P <: JdbcProfile]
    extends BasicActionComponent[K, V] {
  profile: SlickRepository[K, V, P] =>
}
