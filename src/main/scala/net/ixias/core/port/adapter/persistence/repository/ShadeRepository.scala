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
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.config.Config

import core.domain.model.Entity
import core.port.adapter.persistence.backend.ShadeBackend
import core.port.adapter.persistence.io.EntityIOActionContext


/**
 * The base repository for persistence with using the Shade library.
 */
trait ShadeRepository[K, E <: Entity[K]] extends Repository[K, E] with ShadeProfile

/**
 * The profile for persistence with using the Shade library.
 */
trait ShadeProfile extends BasicProfile with ShadeActionComponent { self =>

  type This >: this.type <: ShadeProfile

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

trait ShadeActionComponent extends ActionComponent { profile: ShadeProfile =>
  /** Create the default IOActionContext for this repository. */
  def createPersistenceActionContext(cfg: Config): Context =
     EntityIOActionContext(config = cfg)
}

