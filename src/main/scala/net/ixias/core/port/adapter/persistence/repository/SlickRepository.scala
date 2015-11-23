/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import scala.reflect.ClassTag
import slick.driver.JdbcProfile
import core.domain.model.{ Identity, Entity }
import core.port.adapter.persistence.lifted._
import core.port.adapter.persistence.backend.SlickBackend

/**
 * The profile for persistence with using the Slick library.
 */
trait SlickProfile[P <: JdbcProfile]
    extends BasicProfile with SlickActionComponent[P] { self =>

  /** The back-end type required by this profile */
  type Backend  = SlickBackend[P]
  /** The type of database objects. */
  type Database = Backend#Database

  /** The configured driver. */
  val driver: P

  /** The back-end implementation for this profile */
  val backend = new SlickBackend[P] {}

  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  trait API extends super.API with driver.API
      with SlickColumnOptionOps
      with SlickColumnTypeOps[P] {
    lazy val driver = self.driver
    implicit lazy val ctx = createPersistenceActionContext()
  }
  override val api: API = new API {}

  /** Run the supplied function with a database object by using pool database session. */
  def withDatabase[T](dsn:String)(f: Database => T)(implicit ctx: Context): T =
    f(backend.getDatabase(driver, dsn))
}

trait SlickActionComponent[P <: JdbcProfile] extends BasicActionComponent {
  profile: SlickProfile[P] =>
}

trait SlickRepository[K <: Identity[_], V <: Entity[K], P <: JdbcProfile]
    extends BasicRepository[K, V] with SlickProfile[P]

