/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence

import scala.language.higherKinds
import ixias.model.{ Tagged, Entity, IdStatus }
import ixias.persistence.dbio.{ Execution, EntityIOAction }
import ixias.persistence.lifted.{ Aliases, ExtensionMethodConversions }
import ixias.util.Logger

/**
 * The basic functionality that has to be implemented by all profiles.
 */
private[persistence] trait Profile {

  /** The type of database objects. */
  type Database <: AnyRef

  /** The back-end type required by this profile */
  type Backend  <: ixias.persistence.backend.BasicBackend[Database]

  /** The back-end implementation for this profile */
  protected val backend: Backend

  /** The logger for profile */
  protected lazy val logger  = Logger.apply

  /** The Execution Context */
  protected implicit val ctx = Execution.Implicits.trampoline

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
trait Repository[K <: Tagged[_, _], E[S <: IdStatus] <: Entity[K, S]]
    extends Profile with EntityIOAction[K, E]




