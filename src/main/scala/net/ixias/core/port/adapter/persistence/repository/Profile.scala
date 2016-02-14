/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import org.slf4j.LoggerFactory
import com.typesafe.config.{ Config, ConfigFactory }
import scala.concurrent.Future
import scala.util.Failure

import core.util.Logger
import core.domain.model.{ Identity, Entity }
import core.port.adapter.persistence.io.EntityIOAction
import core.port.adapter.persistence.lifted.ExtensionMethodConversions

/**
 * The basic functionality that has to be implemented by all profiles.
 */
trait Profile[K, E <: Entity[K]] extends EntityIOAction[K, E] {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The identity type of entity */
  type Id       <: core.domain.model.Identity[_]
  /** The entity type of managed by this profile */
  type Entity   <: core.domain.model.Entity[_]
  /** The back-end type required by this profile */
  type Backend  <: core.port.adapter.persistence.backend.BasicBackend
  /** The type of database objects. */
  type Database = Backend#Database
  /** The type of the context used for running IOActions. */
  type Context  = Backend#Context

  // --[ Properties ]-----------------------------------------------------------
  /** The back-end implementation for this profile */
  protected implicit val backend: Backend

  /** The configuration for persistence */
  protected lazy val config = loadPersistenceConfig

  /** The logger for profile */
  protected lazy val logger = new Logger(LoggerFactory.getLogger(this.getClass.getName))

  /** Load the configuration for this repository. This can be overridden in
    * user-defined repository subclasses to load different configurations. */
  protected def loadPersistenceConfig: Config = ConfigFactory.load()

  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  trait API extends ExtensionMethodConversions
  val api: API
}
