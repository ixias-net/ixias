/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.util.{ Success, Failure }
import scala.concurrent.Future

import shade.memcached.Memcached
import ixias.persistence.model.DataSourceName
import ixias.persistence.dbio.Execution.Implicits.trampoline

case class ShadeBackend() extends BasicBackend with ShadeDataSource
{
  /** The type of database objects used by this backend. */
  type Database = Memcached

  /** Get a Database instance from connection pool. */
  def getDatabase(dsn: DataSourceName): Future[Memcached] = {
    logger.debug("Get a database dsn=%s hash=%s".format(dsn.toString, dsn.hashCode))
    ShadeBackendContainer.getOrElseUpdate(dsn) {
      (for {
        ds <- DataSource.forDSN(dsn)
        db <- Future(Memcached(ds))
      } yield db) andThen {
        case Success(_) => logger.info("Created a new data souce. dsn=%s".format(dsn.toString))
        case Failure(_) => logger.info("Failed to create a data souce. dsn=%s".format(dsn.toString))
      }
    }
  }
}

/**
 * Manage data sources associated with DSN.
 */
object ShadeBackendContainer extends BasicBackendContainer[Memcached]
