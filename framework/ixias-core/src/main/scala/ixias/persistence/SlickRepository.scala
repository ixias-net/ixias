/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence

import slick.jdbc.JdbcProfile
import ixias.model.{ @@, EntityModel }
import ixias.persistence.lifted._
import ixias.persistence.backend.SlickBackend
import ixias.persistence.action.SlickDBActionProvider

/**
 * The profile for persistence with using the Slick library.
 */
trait SlickProfile[P <: JdbcProfile]
    extends Profile with SlickDBActionProvider[P] { self =>

  /** The type of slick driver */
  type Driver   = P

  /** The type of database objects. */
  type Database = P#Backend#Database

  /** The back-end type required by this profile */
  type Backend  = SlickBackend[P]

  /** The configured driver. */
  protected val driver: Driver

  /** The back-end implementation for this profile */
  protected lazy val backend = SlickBackend(driver)

  /** Database Action Helpers */
  protected val DBAction    = SlickDBAction
  protected val RunDBAction = SlickRunDBAction

  /**
   * The API for using the utility methods with a single import statement.
   * This provides the repository's implicits, the Database connections,
   * and commonly types and objects.
   */
  trait API extends super.API
      with driver.API
      with SlickQueryOps
      with SlickColumnTypeOps[P]
      with SlickRepOps[P] {
    lazy val driver = self.driver
  }
  trait APIUnsafe extends API with SlickRepUnsafeOps[P]
  val api:       API       = new API       {}
  val apiUnsafe: APIUnsafe = new APIUnsafe {}
}

/**
 * The repository for persistence with using the Slick library.
 */
trait SlickRepository[K <: @@[_, _], M <: EntityModel[K], P <: JdbcProfile]
    extends Repository[K, M] with SlickProfile[P] {
  trait API extends super.API
      with SlickDBIOActionOps[K, M]
  override val api: API = new API {}
}
