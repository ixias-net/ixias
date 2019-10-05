/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb

import ixias.persistence.model.DataSourceName

/**
 * The model of AmazonQLDB table.
 */
trait Table[T] {

  //-- [ Table Manifest ] ------------------------------------------------------
  /** The type of table row. */
  type Record = T

  //-- [ Required properties ] -------------------------------------------------
  /** Data storage location information. */
  val dsn: DataSourceName

  /** Version of data model. */
  val version: Int = 1

  //-- [ Utility Methods ] -----------------------------------------------------
  /**
   * Overwrite function if necessary.
   * As you add and change features in your app,
   * you need to modify your entity classes to reflect these adjust.
   */
  def migrate(version: Int, data: T): T = data
}
