/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.sns.backend

import scala.concurrent.Future
import scala.util.{ Success, Failure }
import ixias.util.Logger
import ixias.persistence.dbio.Execution
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.sns.{ AmazonSNS, AmazonSNSClientBuilder }

/**
 * The backend to get a client for AmazonSNS.
 */
object AmazonSNSBackend extends AmazonSNSConfig {

  /** The logger for profile */
  protected lazy val logger  = Logger.apply

  /** The Execution Context */
  protected implicit val ctx = Execution.Implicits.trampoline

  /** Get a Client to manage Amazon SNS. */
  def getClient(implicit dsn: DataSourceName): Future[AmazonSNS] = {
    logger.debug("Get a database dsn=%s hash=%s".format(dsn.toString, dsn.hashCode))
    Future.fromTry(
      for {
        credentials <- getAWSCredentials
        region      <- getAWSRegion
      } yield AmazonSNSClientBuilder.standard
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withRegion(region)
        .build
    ) andThen {
      case Success(_) => logger.info("Generated a new client. dsn=%s".format(dsn.toString))
      case Failure(_) => logger.info("Failed to build a client. dsn=%s".format(dsn.toString))
    }
  }
}
