/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.concurrent.Future
import ixias.persistence.model.DataSourceName
import ixias.persistence.dbio.Execution
import ixias.util.Logger
import org.slf4j.LoggerFactory

/**
 * The backend to handle the database and session.
 */
trait BasicBackend[T] extends BasicDatabaseConfig {

  /** The type of database used by this backend. */
  type Database = T

  /** The Execution Context */
  protected implicit val ctx = Execution.Implicits.trampoline

  /** The logger for profile */
  protected lazy val logger  =
    new Logger(LoggerFactory.getLogger(this.getClass.getName))

  /** Get a Database instance from connection pool. */
  def getDatabase(implicit dsn: DataSourceName): Future[T]
}
