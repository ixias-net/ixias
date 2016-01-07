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
import core.port.adapter.persistence.io.{ EntityIOAction, EntityIOActionContext }

/**
 * The profile for persistence that
 * does not assume the existence driver for database abstract layer.
 */
trait Repository extends Profile with RepositoryActionComponent {

  /** The type of the context used for running repository Actions */
  type Context =  EntityIOActionContext

  /** The back-end implementation for this profile */
  val backend: Backend

  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  trait API extends super.API
  val api: API = new API {}

}

trait RepositoryActionComponent extends ActionComponent { profile: Repository =>
  /** Create the default EntityIOActionContext for this repository. */
  def createPersistenceActionContext(): Context =
    createPersistenceActionContext(profile.config)
  def createPersistenceActionContext(c: Config): Context =
    EntityIOActionContext(config = c)
}

trait RepositoryRepository[K <: Identity[_], V <: Entity[K]]
    extends Repository with EntityIOAction[K, V] {
  /** The identity type of entity */
  type Id      = K
  /** The entity type of managed by this profile */
  type Entity  = V
}

