/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence

import scala.util.Failure
import scala.concurrent.{ Future, ExecutionContext }
import com.typesafe.config.{ Config, ConfigFactory }
import org.slf4j.LoggerFactory

import ixias.util.Logger
import ixias.model.{ Identity, Entity }
import ixias.persistence.dbio.EntityIOAction
import ixias.persistence.lifted.{ Aliases, ExtensionMethodConversions }
import ixias.persistence.dbio.Execution

/**
 * The basic functionality that has to be implemented by all profiles.
 */
private[persistence] trait Profile {

  /** The back-end type required by this profile */
  type Backend  <: ixias.persistence.backend.BasicBackend

  /** The type of database objects. */
  type Database  = Backend#Database

  /** The back-end implementation for this profile */
  protected val backend: Backend

  /** The Execution Context */
  protected implicit val ctx: ExecutionContext = Execution.Implicits.trampoline

  /** The logger for profile */
  protected lazy val logger =
    new Logger(LoggerFactory.getLogger(this.getClass.getName))

  /**
   * The API for using the utility methods with a single import statement.
   * This provides the repository's implicits, the Database connections,
   * and commonly types and objects.
   */
  trait API extends Aliases with ExtensionMethodConversions
  val api: API
}

/**
 * The basic repository with IOAction
 */
trait Repository[K <: Identity[_], E <: Entity[K]] extends Profile with EntityIOAction[K, E]
