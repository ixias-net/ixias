/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import scala.util.Try
import core.port.adapter.persistence.io.EntityIOActionContext

trait ShadeDataSource extends DataSource with ShadeDataSourceConfig {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of database source config used by this backend. */
  type DataSource = shade.memcached.Configuration

  /** The type of the database souce config factory used by this backend. */
  type DataSourceFactory = ShadeDataSourceFactory

  /** The type of the context used for running repository Actions */
  type Context = EntityIOActionContext

  // --[ Factory ]--------------------------------------------------------------
  /** Factory methods for creating `DatabSouce` instances with using Shade. */
  trait ShadeDataSourceFactory extends DataSourceFactoryDef {
    def forDSN(name: String)(implicit ctx: Context): Try[DataSource] = ???
  }
}
