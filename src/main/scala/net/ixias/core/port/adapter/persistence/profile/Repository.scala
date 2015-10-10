/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.core
package port.adapter.persistence.profile

import com.typesafe.config.{ Config, ConfigFactory }
import net.ixias.core.domain.model.{ Identity, Entity }
import net.ixias.core.port.adapter.persistence.lifted._
import net.ixias.core.port.adapter.persistence.backend.DatabaseComponent

/**
 * The basic functionality that has to be implemented by all repositories.
 */
trait BasicProfile[K <: Identity[_], V <: Entity[K]] extends BasicActionComponent[K, V] {
  repository: BasicRepository[K, V] =>

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The identity type of entity */
  type Id = K
  /** The entity type of managed by this profile */
  type Entity = V
  /** The back-end type required by this profile */
  type Backend <: DatabaseComponent

  // --[ Properties ]-----------------------------------------------------------
  /** The back-end implementation for this profile */
  // val backend: Backend

  // trait ExtensionMethodConversions {
  //   /** For extending `Future` methods. */
  //   @inline implicit def extFutureOps[A](a: Future[A]) = new FutureOps[A](a)
  // }

  // trait API extends ExtensionMethodConversions {
    /** Contains an error messsage when a command fails validation. */
    // type Error = String
    /** Used to validate commands received by the system that act on the adapter. */
    // type ValidationNel[A] = scalaz.ValidationNel[Error, A]
 // }

  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  // val api: API
}

trait BasicActionComponent[K <: Identity[_], V <: Entity[K]] {
  repository: BasicRepository[K, V] =>
}

trait BasicRepository[K <: Identity[_], V <: Entity[K]] extends BasicProfile[K, V] {
  /** The external interface of this repository which defines the API. */
  val profile: BasicProfile[K, V] = this

  /** The configuration for this repository */
  final lazy val config: Config = loadRepositoryConfig

  /** Load the configuration for this repository. This can be overridden in
    * user-defined repository subclasses to load different configurations. */
  protected[this] def loadRepositoryConfig: Config = {
    ConfigFactory.load()
  }
}
