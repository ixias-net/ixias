/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence

import ixias.model.{ @@, EntityModel }
import ixias.persistence.dbio.{ Execution, EntityIOAction }
import ixias.persistence.lifted.{ Aliases, ExtensionMethods }

import ixias.util.Logger
import org.slf4j.LoggerFactory

/**
 * The basic functionality that has to be implemented by all profiles.
 */
trait Profile {

  /** The type of database objects. */
  type Database <: AnyRef

  /** The back-end type required by this profile */
  type Backend  <: ixias.persistence.backend.BasicBackend[Database]

  /** The back-end implementation for this profile */
  protected val backend: Backend

  /** The logger for profile */
  protected lazy val logger  =
    new Logger(LoggerFactory.getLogger(this.getClass.getName))

  /** The Execution Context */
  protected implicit val ctx = Execution.Implicits.trampoline

  /**
   * The API for using the utility methods with a single import statement.
   * This provides the repository's implicits, the Database connections,
   * and commonly types and objects.
   */
  trait API extends Aliases with ExtensionMethods
  val api: API
}

/**
 * The basic repository with IOAction
 */
trait Repository[K <: @@[_, _], M <: EntityModel[K]]
    extends Profile with EntityIOAction[K, M]
