/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.concurrent.Future
import ixias.persistence.model.DataSourceName

trait BasicDataSource
{
  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of database source config used by this backend. */
  type DataSource <: AnyRef

  /** The type of the database souce config factory used by this backend. */
  type DataSourceFactory <: DataSourceFactoryDef

  // --[ Properties ]-----------------------------------------------------------
  /** The database factory */
  val DataSource: DataSourceFactory

  /** The factory to create a database source config. */
  trait DataSourceFactoryDef {
    /** Load a configuration for persistent database. */
    def forDSN(dsn: DataSourceName): Future[DataSource]
  }
}


