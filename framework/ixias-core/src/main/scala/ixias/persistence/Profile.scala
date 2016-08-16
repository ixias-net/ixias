/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence

import scala.util.Failure
import scala.concurrent.Future
import org.slf4j.LoggerFactory
import com.typesafe.config.{ Config, ConfigFactory }

import ixias.util.Logger
import ixias.model.{ Identity, Entity }
import ixias.persistence.dbio.EntityIOAction
import ixias.persistence.lifted.{ Aliases, ExtensionMethodConversions }

/**
 * The basic functionality that has to be implemented by all profiles.
 */
trait Profile[K <: Identity[_], E <: Entity[K]] extends EntityIOAction[K, E] {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The back-end type required by this profile */
  type Backend  <: ixias.persistence.backend.BasicBackend
  /** The type of database objects. */
  type Database  = Backend#Database

  // --[ Properties ]-----------------------------------------------------------
  /** The back-end implementation for this profile */
  protected val backend: Backend

  /** The configuration for persistence */
  protected lazy val config = loadPersistenceConfig

  /** The logger for profile */
  protected lazy val logger = new Logger(LoggerFactory.getLogger(this.getClass.getName))

  /**
   * Load the configuration for this repository. This can be overridden in
   * user-defined repository subclasses to load different configurations.
   */
  protected def loadPersistenceConfig: Config = ConfigFactory.load()

  /**
   * The API for using the utility methods with a single import statement.
   * This provides the repository's implicits, the Database connections,
   * and commonly types and objects.
   */
  trait API extends Aliases with ExtensionMethodConversions
  val api: API
}
