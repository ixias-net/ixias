/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.util.{ Success, Failure }
import scala.concurrent.Future
import shade.memcached.{ Memcached, Configuration }
import ixias.persistence.model.DataSourceName

/**
 * The shade backend to handle the database and session.
 */
case class ShadeBackend()
   extends BasicBackend[Memcached] with ShadeConfig
{
  /** Get a Database instance from connection pool. */
  def getDatabase(implicit dsn: DataSourceName): Future[Database] = {
    logger.debug("Get a database dsn=%s hash=%s".format(dsn.toString, dsn.hashCode))
    ShadeDatabaseContainer.getOrElseUpdate {
      (for {
        conf <- createConfiguration
        db    = Memcached(conf)
      } yield db) andThen {
        case Success(_) => logger.info("Created a new data souce. dsn=%s".format(dsn.toString))
        case Failure(_) => logger.info("Failed to create a data souce. dsn=%s".format(dsn.toString))
      }
    }
  }

  /** Create a configuration for shade client to access memcached */
  def createConfiguration(implicit dsn: DataSourceName): Future[Configuration] =
    Future.fromTry {
      for {
        addresses <- getAddresses
      } yield {
        shade.memcached.Configuration(
          addresses        = addresses,
          keysPrefix       = Some(getKeysPrefix),
          operationTimeout = getHostSpecIdleTimeout
        )
      }
    }
}

/** Manage data sources associated with DSN. */
object ShadeDatabaseContainer extends BasicDatabaseContainer[Memcached]
