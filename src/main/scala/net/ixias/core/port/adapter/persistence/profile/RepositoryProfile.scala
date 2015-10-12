/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core
package port.adapter.persistence.profile

import com.typesafe.config.{ Config, ConfigFactory }
import domain.model.{ Identity, Entity }
import port.adapter.persistence.io.EntityIOAction
import port.adapter.persistence.backend.RepositoryBackend

/**
 * The basic functionality that has to be implemented by all repositories.
 */
trait RepositoryProfile[K <: Identity[_], V <: Entity[K]]
    extends BasicProfile with RepositoryActionComponent[K, V] {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The identity type of entity */
  type Id      = K
  /** The entity type of managed by this profile */
  type Entity  = V
  /** The back-end type required by this profile */
  type Backend = RepositoryBackend

  // --[ Properties ]-----------------------------------------------------------
  /** The back-end implementation for this profile */
  val backend: Backend = new RepositoryBackend {}

  // --[ Methods ]--------------------------------------------------------------
  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  trait API extends super.API {
  }
  val api: API = new API {}
}

trait RepositoryActionComponent[K <: Identity[_], V <: Entity[K]]
    extends BasicActionComponent with EntityIOAction[K, V] {
  profile: RepositoryProfile[K, V] =>
}
