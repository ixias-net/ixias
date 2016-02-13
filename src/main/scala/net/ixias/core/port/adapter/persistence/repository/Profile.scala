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
import scala.concurrent.ExecutionContext.Implicits.global

import core.util.Logger
import core.domain.model.{ Identity, Entity }
import core.port.adapter.persistence.io.EntityIOAction
import core.port.adapter.persistence.lifted.ExtensionMethodConversions

/**
 * The basic functionality that has to be implemented by all repositories.
 */
trait Repository[K, E <: Entity[K]] extends Profile with EntityIOAction[K, E]

/**
 * The basic functionality that has to be implemented by all profiles.
 */
trait Profile extends ActionComponent {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The identity type of entity */
  type Id       <: core.domain.model.Identity[_]
  /** The entity type of managed by this profile */
  type Entity   <: core.domain.model.Entity[_]
  /** The back-end type required by this profile */
  type Backend  <: core.port.adapter.persistence.backend.BasicBackend
  /** The type of database objects. */
  type Database = backend.Database
  /** The type of the context used for running IOActions. */
  type Context  = backend.Context

  // --[ Properties ]-----------------------------------------------------------
  /** The external interface of this repository which defines the API. */
  val profile: Profile = this

  /** The back-end implementation for this profile */
  val backend: Backend

  /** The configuration for persistence */
  protected lazy val config = loadPersistenceConfig

  /** The logger for profile */
  protected lazy val actionLogger = new Logger(
    LoggerFactory.getLogger(classOf[Profile].getName+".action"))

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

  /** Run the supplied function with a database object by using pool database session. */
  def withDatabase[T](dsn: String)(f: Database => Future[T])(implicit ctx: Context): Future[T] =
    (for {
      db    <- Future.fromTry(backend.getDatabase(dsn)(ctx))
      value <- f(db)
    } yield value) andThen {
      case Failure(ex) => actionLogger.error("The database action failed. dsn=" + dsn, ex)
    }

  /** Create the default IOActionContext for this repository. */
  def createPersistenceActionContext(cfg: Config): Context
  def createPersistenceActionContext(): Context =
      createPersistenceActionContext(profile.config)
}
