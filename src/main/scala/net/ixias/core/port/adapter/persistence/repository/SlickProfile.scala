/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import slick.driver.JdbcProfile
import core.domain.model.Entity
import core.port.adapter.persistence.lifted._
import core.port.adapter.persistence.backend.SlickBackend

/**
 * The profile for persistence with using the Slick library.
 */
trait SlickProfile[K, E <: Entity[K], P <: JdbcProfile]
    extends Profile[K, E] with SlickAction[P] with ExtensionMethodConversions { self =>

  /** The type of slick driver */
  type Driver  = P

  /** The back-end type required by this profile */
  type Backend = SlickBackend[Driver]

  /** The configured driver. */
  val driver: Driver

  /** The back-end implementation for this profile */
  protected implicit lazy val backend = new SlickBackend[P] { val driver = self.driver }

  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  trait API extends super.API with driver.API
      with SlickColumnOptionOps
      with SlickColumnTypeOps[Driver] {
    lazy val driver = self.driver
  }
  val api: API = new API {}
}
