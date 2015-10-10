/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.core.port.adapter.persistence

import com.typesafe.config.{ Config, ConfigFactory }
import net.ixias.core.domain.model.{ Identity, Entity }

/**
 * The basic functionality that has to be implemented by all repositories.
 */
trait BasicProfile[ID <: Identity[_], E <: Entity[ID]]
    extends RepositoryActionComponent[ID, E] { repository: BasicRepository[ID, E] =>
  trait API {
  }
  val api: API = new API {}
}

trait RepositoryActionComponent[ID <: Identity[_], E <: Entity[ID]] {
  repository: BasicRepository[ID, E] =>
}

trait BasicRepository[ID <: Identity[_], E <: Entity[ID]] extends BasicProfile[ID, E] {
  /** The external interface of this repository which defines the API. */
  val profile: BasicProfile[ID, E] = this

  /** The configuration for this repository */
  final lazy val config: Config = loadRepositoryConfig

  /** Load the configuration for this repository. This can be overridden in
    * user-defined repository subclasses to load different configurations. */
  protected[this] def loadRepositoryConfig: Config = {
    ConfigFactory.load()
  }
}
