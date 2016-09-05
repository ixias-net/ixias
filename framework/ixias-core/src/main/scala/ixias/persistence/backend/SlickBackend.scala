/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.util.{ Success, Failure }
import scala.concurrent.Future

import slick.jdbc.JdbcBackend
import slick.driver.JdbcProfile
import ixias.persistence.model.DataSourceName
import ixias.persistence.dbio.Execution.Implicits.trampoline

case class SlickBackend[P <: JdbcProfile](val driver: P)
   extends BasicBackend with SlickDataSource
{
  /** The type of database objects used by this backend. */
  type Database = P#Backend#Database

  /** Get a Database instance from connection pool. */
  def getDatabase(dsn: DataSourceName): Future[Database] = {
    logger.debug("Get a database dsn=%s hash=%s".format(dsn.toString, dsn.hashCode))
    SlickBackendContainer.getOrElseUpdate(dsn) {
      (for {
        ds <- DataSource.forDSN(dsn)
        db <- Future(driver.backend.Database.forSource(ds))
      } yield db) andThen {
        case Success(_) => logger.info("Created a new data souce. dsn=%s".format(dsn.toString))
        case Failure(_) => logger.info("Failed to create a data souce. dsn=%s".format(dsn.toString))
      }
    }
  }
}

/** Manage data sources associated with DSN */
object SlickBackendContainer extends BasicBackendContainer[JdbcBackend#Database]
