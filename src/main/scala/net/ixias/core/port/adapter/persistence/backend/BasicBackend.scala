/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import scala.concurrent.Future
import core.port.adapter.persistence.model.DataSourceName

/** Backend for the basic database and session handling features. */
trait BasicBackend extends BasicDataSource {

  /** The type of database objects used by this backend. */
  type Database <: AnyRef

  /** Get a Database instance from connection pool. */
  def getDatabase(dsn: DataSourceName)(implicit ctx: Context): Future[Database]
}
