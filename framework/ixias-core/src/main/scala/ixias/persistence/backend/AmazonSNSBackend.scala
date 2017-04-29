/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.util.{ Success, Failure }
import scala.concurrent.Future

import com.amazonaws.services.sns.AmazonSNS
import ixias.persistence.model.DataSourceName
import ixias.persistence.dbio.Execution.Implicits.trampoline

case class AmazonSNSBackend() extends BasicBackend with AmazonSNSDataSouce {

  /** The type of database objects used by this backend. */
  type Database = AmazonSNS

  /** Get a Database instance from connection pool. */
  def getDatabase(dsn: DataSourceName): Future[AmazonSNS] = {
    logger.debug("Get a database dsn=%s hash=%s".format(dsn.toString, dsn.hashCode))
    DataSource.forDSN(dsn) andThen {
      case Success(_) => logger.info("Created a new data souce. dsn=%s".format(dsn.toString))
      case Failure(_) => logger.info("Failed to create a data souce. dsn=%s".format(dsn.toString))
    }
  }
}
