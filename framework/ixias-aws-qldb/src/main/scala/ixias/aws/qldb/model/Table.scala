/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.model

import ixias.model._

/**
 * The model of AmazonQLDB table.
 */
trait Table[K <: Document.Id[_], M <: EntityModel[K]] {

  //-- [ Properties ] ----------------------------------------------------------
  /** Data storage location information */
  val dsn:   DataSourceName

  /** Table queries */
  val query: Query

  //-- [ Table Query ] ---------------------------------------------------------
  type Query      <: TableQuery
  type BasicQuery =  TableQuery

  type TableQuery     = ixias.aws.qldb.model.TableQuery[K, M]
  type DataSourceName = ixias.persistence.model.DataSourceName
  val  DataSourceName = ixias.persistence.model.DataSourceName

  //-- [ Utility Methods ] -----------------------------------------------------
  /**
   * Overwrite function if necessary.
   * As you add and change features in your app,
   * you need to modify your entity classes to reflect these adjust.
   */
  def migrate[A](data: A): A = data
}
