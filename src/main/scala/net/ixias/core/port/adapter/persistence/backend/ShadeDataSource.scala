/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import scala.util.Try
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit

import core.port.adapter.persistence.model.DataSourceName
import core.port.adapter.persistence.io.EntityIOActionContext

trait ShadeDataSource extends BasicDataSource with ShadeDataSourceConfig {

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
    import DataSourceName.Implicits._
    def forDSN(name: String)(implicit ctx: Context): Try[DataSource] =
      for {
        addresses <- getAddresses(name)
      } yield {
        shade.memcached.Configuration(
          addresses        = addresses,
          keysPrefix       = getKeysPrefix(name),
          operationTimeout = FiniteDuration(getHostSpecIdleTimeout(name), TimeUnit.MILLISECONDS)
        )
      }
  }
}
