/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core
package port.adapter.persistence
package profile

import io.IOAction
import backend.DatabaseComponent
import lifted.ExtensionMethodConversions
import com.typesafe.config.{ Config, ConfigFactory }

/**
 * The basic functionality that has to be implemented by all repositories.
 */
trait BasicProfile extends BasicActionComponent {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The identity type of entity */
  type Id      <: domain.model.Identity[_]
  /** The entity type of managed by this profile */
  type Entity  <: domain.model.Entity[Id]
  /** The back-end type required by this profile */
  type Backend <: DatabaseComponent

  // --[ Properties ]-----------------------------------------------------------
  /** The external interface of this repository which defines the API. */
  val profile: BasicProfile = this

  /** The back-end implementation for this profile */
  val backend: Backend

  /** The configuration for persistence */
  final lazy val config: Config = loadPersistenceConfig

  // --[ Methods ]--------------------------------------------------------------
  /** Load the configuration for this repository. This can be overridden in
    * user-defined repository subclasses to load different configurations. */
  protected[this] def loadPersistenceConfig: Config = {
    ConfigFactory.load()
  }

  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  trait API extends ExtensionMethodConversions {
    type Error            = IOAction#Error
    type ValidationNel[A] = IOAction#ValidationNel[A]
  }
  val api: API
}

trait BasicActionComponent { profile: BasicProfile =>
}
