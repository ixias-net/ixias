/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.dynamodb.backend

import scala.concurrent.Future
import ixias.persistence.model.DataSourceName
import ixias.persistence.backend.{ BasicBackend, BasicDatabaseContainer }
import com.amazonaws.services.dynamodbv2.{ AmazonDynamoDB, AmazonDynamoDBClientBuilder }

/**
 * The backend to get a client for AmazonDynamoDB
 */
object AmazonDynamoDBBackend extends BasicBackend[AmazonDynamoDB] with AmazonDynamoDBConfig {

  /**
   * The container to manage client resource associated with DSN
   */
  object CACHE_DBB_CLIENT extends BasicDatabaseContainer[AmazonDynamoDB]

  /**
   * Get a client to manage Amazon DynamoDB
   */
  def getDatabase(implicit dsn: DataSourceName): Future[AmazonDynamoDB] = {
    logger.debug("Get a database dsn=%s hash=%s".format(dsn.toString, dsn.hashCode))
    CACHE_DBB_CLIENT.getOrElseUpdate {
      Future.fromTry {
        for {
          region      <- getAWSRegion
          credentials <- getAWSCredentials
        } yield AmazonDynamoDBClientBuilder
          .standard
          .withRegion(region)
          .withCredentials(credentials)
          .build
      } andThen {
        case scala.util.Success(_) => logger.info("Create a new client. dsn=%s".format(dsn.toString))
        case scala.util.Failure(_) => logger.info("Failed to create a client. dsn=%s".format(dsn.toString))
      }
    }
  }
}
