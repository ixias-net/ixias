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
import ixias.persistence.io.EntityIOActionContext

trait ShadeDataSource extends BasicDataSource with ShadeDataSourceConfig
{
  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of database source config used by this backend. */
  type DataSource = shade.memcached.Configuration

  /** The type of the database souce config factory used by this backend. */
  type DataSourceFactory = ShadeDataSourceFactory

  /** The type of the context used for running repository Actions */
  type Context = EntityIOActionContext

  // --[ Properties ]-----------------------------------------------------------
  /** The database factory */
  lazy val DataSource = new ShadeDataSourceFactory{}

  // --[ Factory ]--------------------------------------------------------------
  /** Factory methods for creating `DatabSouce` instances with using Shade. */
  trait ShadeDataSourceFactory extends DataSourceFactoryDef {
    def forDSN(dsn: DataSourceName)(implicit ctx: Context): Future[DataSource] =
      Future.fromTry(
        for {
          addresses <- getAddresses(dsn)
        } yield {
          shade.memcached.Configuration(
            addresses        = addresses,
            keysPrefix       = getKeysPrefix(dsn),
            operationTimeout = FiniteDuration(getHostSpecIdleTimeout(dsn), TimeUnit.MILLISECONDS)
          )
        }
      )
  }
}
