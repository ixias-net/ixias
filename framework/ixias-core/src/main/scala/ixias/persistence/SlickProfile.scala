/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence

import slick.driver.JdbcProfile
import ixias.model.{ Identity, Entity }
import ixias.persistence.lifted._
import ixias.persistence.backend.SlickBackend
import ixias.persistence.action.SlickDBActionProvider

/**
 * The profile for persistence with using the Slick library.
 */
trait SlickProfile[K <: Identity[_], E <: Entity[K], P <: JdbcProfile]
   extends Profile[K, E] with SlickDBActionProvider[P]
{ self =>

  /** The type of slick driver */
  type Driver  = P

  /** The back-end type required by this profile */
  type Backend = SlickBackend[P]

  /** The configured driver. */
  protected implicit val driver: Driver

  /** The back-end implementation for this profile */
  protected lazy val backend = SlickBackend[P]

  /** Database Action Helpers */
  val DBAction    = SlickDBAction
  val RunDBAction = SlickRunDBAction

  /**
   * The API for using the utility methods with a single import statement.
   * This provides the repository's implicits, the Database connections,
   * and commonly types and objects.
   */
  trait API extends super.API with driver.API with SlickColumnTypeOps[P] {
    lazy val driver = self.driver
  }
  val api: API = new API {}
}
