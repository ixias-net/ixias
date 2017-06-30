/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.backend

import scala.concurrent.Future
import scala.util.{ Success, Failure }
import ixias.util.Logger
import ixias.persistence.dbio.Execution
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder

/**
 * The backend to get a client for AmazonS3.
 */
object AmazonS3Backend extends AmazonS3Config {

  /** The logger for profile */
  protected lazy val logger  = Logger.apply

  /** The Execution Context */
  protected implicit val ctx = Execution.Implicits.trampoline

  /** Get a client to manage Amazon S3. */
  def getClient(implicit dsn: DataSourceName): Future[AmazonS3] = {
    logger.debug("Get a database dsn=%s hash=%s".format(dsn.toString, dsn.hashCode))
    Future.fromTry(
      for {
        credentials <- getAWSCredentials
        region      <- getAWSRegion
      } yield {
        val conf = new ClientConfiguration
        conf.setConnectionTimeout(getConnectionTimeout.toInt)
        AmazonS3(AmazonS3ClientBuilder.standard
          .withCredentials(new AWSStaticCredentialsProvider(credentials))
          .withRegion(region)
          .build)
      }
    ) andThen {
      case Success(_) => logger.info("Generated a new client. dsn=%s".format(dsn.toString))
      case Failure(_) => logger.info("Failed to build a client. dsn=%s".format(dsn.toString))
    }
  }

  // The wrapper for AmazonS3 client
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  protected case class AmazonS3(underlying: com.amazonaws.services.s3.AmazonS3) {
  }
}

