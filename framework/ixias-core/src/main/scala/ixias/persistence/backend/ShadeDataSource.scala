/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.concurrent.Future
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit

import ixias.persistence.model.DataSourceName

trait ShadeDataSource extends BasicDataSource with ShadeConfig {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of database source config used by this backend. */
  type DataSource = shade.memcached.Configuration

  /** The type of the database souce config factory used by this backend. */
  type DataSourceFactory = ShadeDataSourceFactory

  // --[ Properties ]-----------------------------------------------------------
  /** The database factory */
  lazy val DataSource = new ShadeDataSourceFactory{}

  // --[ Factory ]--------------------------------------------------------------
  /** Factory methods for creating `DatabSouce` instances with using Shade. */
  trait ShadeDataSourceFactory extends DataSourceFactoryDef {
    def forDSN(dsn: DataSourceName): Future[DataSource] =
      Future.fromTry(
        for {
          addresses <- getAddresses(dsn)
        } yield {
          shade.memcached.Configuration(
            addresses        = addresses,
            keysPrefix       = Some(getKeysPrefix(dsn)),
            operationTimeout = getHostSpecIdleTimeout(dsn)
          )
        }
      )
  }
}
