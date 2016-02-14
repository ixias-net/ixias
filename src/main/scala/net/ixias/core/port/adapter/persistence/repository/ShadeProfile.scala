/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import core.domain.model.Entity
import core.port.adapter.persistence.backend.ShadeBackend

/**
 * The profile for persistence with using the Shade library.
 */
trait ShadeProfile[K, E <: Entity[K]] extends Profile[K, E] {

  /** The back-end type required by this profile */
  type Backend  = ShadeBackend

  /** The back-end implementation for this profile */
  val backend = new ShadeBackend {}

  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  trait API extends super.API
  val api: API = new API {}
}
