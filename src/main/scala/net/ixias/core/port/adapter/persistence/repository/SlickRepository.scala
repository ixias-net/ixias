/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import scala.util.{ Try, Failure }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import slick.dbio.{ DBIOAction, NoStream }
import slick.driver.JdbcProfile
import com.typesafe.config.Config

import core.domain.model.Entity
import core.port.adapter.persistence.model.{ Table, Converter }
import core.port.adapter.persistence.lifted._
import core.port.adapter.persistence.backend.SlickBackend
import core.port.adapter.persistence.io.EntityIOActionContext

/**
 * The base repository for persistence with using the Slick library.
 */
trait SlickRepository[K, E <: Entity[K], P <: JdbcProfile]
    extends Repository[K, E] with SlickProfile[P]

/**
 * The profile for persistence with using the Slick library.
 */
trait SlickProfile[P <: JdbcProfile] extends Profile
    with SlickActionComponent[P] with ExtensionMethodConversions { self =>

  type This >: this.type <: SlickProfile[P]

  /** The back-end type required by this profile */
  type Backend  = SlickBackend[P]

  /** The configured driver. */
  val driver: P

  /** The back-end implementation for this profile */
  val backend = new SlickBackend[P] { val driver = self.driver }

  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  trait API extends super.API with driver.API
      with SlickColumnOptionOps
      with SlickColumnTypeOps[P] {
    lazy val driver = self.driver
  }
  val api: API = new API {}
}

trait SlickActionComponent[P <: JdbcProfile]
    extends ActionComponent { profile: SlickProfile[P] =>

  /** Create the default IOActionContext for this repository. */
  def createPersistenceActionContext(cfg: Config): Context =
     EntityIOActionContext(config = cfg)
}

