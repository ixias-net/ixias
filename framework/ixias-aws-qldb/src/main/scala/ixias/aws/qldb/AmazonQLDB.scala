/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb

import ixias.model._
import ixias.persistence.{ Profile, Repository }
import ixias.aws.qldb.backend.{ AmazonQLDBBackend, AmazonQLDBActionProvider }

trait AmazonQLDBProfile extends Profile
    with AmazonQLDBActionProvider {

  // --[ Typedefs ]-------------------------------------------------------------
  /** The type of database objects. */
  type Database = AmazonQLDBBackend.type#Database

  /** The back-end type required by this profile */
  type Backend  = AmazonQLDBBackend.type

  // --[ Alias ]----------------------------------------------------------------
  val DataSourceName = ixias.persistence.model.DataSourceName

  // --[ Properties ]-----------------------------------------------------------
  /** The back-end implementation for this profile */
  protected lazy val backend = AmazonQLDBBackend

  /**
   * The API for using the utility methods with a single import statement.
   * This provides the repository's implicits, the Database connections,
   * and commonly types and objects.
   */
  trait API extends super.API
  val api: API = new API {}
}

/**
 * The repository for persistence with using the Slick library.
 */
trait AmazonQLDBRepository[K <: @@[_, _], M <: EntityModel[K]]
    extends Repository[K, M] with AmazonQLDBProfile {
  trait API extends super.API
  override val api: API = new API {}
}
