/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence

import ixias.model.{ Identity, Entity }
import ixias.persistence.backend.{ ShadeBackend, ShadeDBActionProvider }

/**
 * The profile for persistence with using the Shade library.
 */
trait ShadeProfile[K <: Identity[_], E <: Entity[K]]
   extends Profile[K, E] with ShadeDBActionProvider
{
  /** The back-end type required by this profile */
  type Backend = ShadeBackend

  /** The back-end implementation for this profile */
  protected lazy val backend = ShadeBackend()

  /** Database Action Helpers */
  val DBAction = ShadeDBAction

  /**
   * The API for using the utility methods with a single import statement.
   * This provides the repository's implicits, the Database connections,
   * and commonly types and objects.
   */
  trait API extends super.API
  val api: API = new API {}
}
