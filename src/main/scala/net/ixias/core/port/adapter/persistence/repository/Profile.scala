/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import com.typesafe.config.{ Config, ConfigFactory }
import core.domain.model.{ Identity, Entity }
import core.port.adapter.persistence.io.{ EntityIOAction, IOActionContext }
import core.port.adapter.persistence.lifted.ExtensionMethodConversions

/**
 * The basic functionality that has to be implemented by all repositories.
 */
trait Repository[K, V <: Entity[K]] extends Profile with EntityIOAction[K, V]

/**
 * The basic functionality that has to be implemented by all profiles.
 */
trait Profile extends ActionComponent {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The identity type of entity */
  type Id      <: core.domain.model.Identity[_]
  /** The entity type of managed by this profile */
  type Entity  <: core.domain.model.Entity[_]
  /** The back-end type required by this profile */
  type Backend <: core.port.adapter.persistence.backend.Backend
  /** The type of the context used for running IOActions. */
  type Context >: Null <: IOActionContext

  // --[ Properties ]-----------------------------------------------------------
  /** The external interface of this repository which defines the API. */
  val profile: Profile = this

  /** The back-end implementation for this profile */
  val backend: Backend

  /** The configuration for persistence */
  final lazy val config: Config = loadPersistenceConfig

  /** Load the configuration for this repository. This can be overridden in
    * user-defined repository subclasses to load different configurations. */
  protected def loadPersistenceConfig: Config = {
    ConfigFactory.load()
  }

  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  trait API extends ExtensionMethodConversions
  val api: API
}

trait ActionComponent { profile: Profile =>
  /** Create the default IOActionContext for this repository. */
  def createPersistenceActionContext(cfg: Config): Context
  def createPersistenceActionContext(): Context =
      createPersistenceActionContext(profile.config)
}
