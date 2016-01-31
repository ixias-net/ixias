/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import scala.util.{ Try, Success, Failure }
import scala.util.control.NonFatal
import com.typesafe.config.Config
import core.domain.model.Entity
import core.port.adapter.persistence.backend.ShadeBackend
import core.port.adapter.persistence.io.EntityIOActionContext

/**
 * The base repository for persistence with using the Shade library.
 */
trait ShadeRepository[K, V <: Entity[K]] extends Repository[K, V] with ShadeProfile

/**
 * The profile for persistence with using the Shade library.
 */
trait ShadeProfile extends Profile with ShadeActionComponent { self =>

  type This >: this.type <: ShadeProfile
  /** The back-end type required by this profile */
  type Backend  = ShadeBackend
  /** The type of database objects. */
  type Database = Backend#Database
  /** The type of the context used for running repository Actions */
  type Context =  EntityIOActionContext

  /** The back-end implementation for this profile */
  val backend = new ShadeBackend {}

  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  trait API extends super.API
  val api: API = new API {}

  /** Run the supplied function with a default action context. */
  def withActionContext[T](f: Context => T): Try[T] =
    Try { f(createPersistenceActionContext()) }

  /** Run the supplied function with a database object by using pool database session. */
  def withDatabase[T](dsn:String)(f: Database => T)(implicit ctx: Context): Try[T] =
    try Success(f(backend.getDatabase(dsn))) catch {
      case NonFatal(ex)  => { actionLogger.error("The database action failed. dsn=" + dsn, ex); Failure(ex) }
      case ex: Throwable => { actionLogger.error("The database action failed. dsn=" + dsn, ex); throw ex    }
    }
}

trait ShadeActionComponent extends ActionComponent { profile: ShadeProfile =>
  /** Create the default IOActionContext for this repository. */
  def createPersistenceActionContext(cfg: Config): Context =
     EntityIOActionContext(config = cfg)
}

