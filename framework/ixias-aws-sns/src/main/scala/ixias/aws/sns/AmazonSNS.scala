/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.sns

import scala.util.{ Try, Success, Failure }
import scala.concurrent.{ Future, ExecutionContext }
import com.amazonaws.services.sns.model.PublishResult

import ixias.persistence.lifted.Aliases
import ixias.persistence.dbio.Execution
import ixias.util.Logging

// Amazon SNS
//~~~~~~~~~~~~
trait AmazonSNS extends Aliases with Logging {

  // --[ Alias ]----------------------------------------------------------------
  /** The data source name */
  val  DataSourceName = ixias.aws.sns.backend.DataSourceName
  type DataSourceName = ixias.aws.sns.backend.DataSourceName
  implicit val dsn: DataSourceName

  // --[ Properties ]-----------------------------------------------------------
  /** The backend */
  protected val backend = ixias.aws.sns.backend.AmazonSNSBackend

  /** The Execution Context */
  protected implicit val ctx: ExecutionContext = Execution.Implicits.trampoline

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Sends a message to a topic's subscribed endpoints.
   */
  def publish(message: String): Future[Seq[PublishResult]] =
    backend.isSkip(dsn) match {
      case true  => {
        backend.getTopicARN(dsn) map { topic =>
          logger.info("AWS-SNS :: skip to publish a message. topic = %s, message = %s".format(topic, message))
        }
        Future.successful(Seq.empty)
      }
      case false => for {
        client    <- backend.getClient(dsn)
        topicSeq  <- Future.fromTry(backend.getTopicARN(dsn))
        resultSeq <- Future.sequence {
          topicSeq map { topic =>
            Future.fromTry {
              Try(client.publish(topic, message))
            } andThen {
              case Success(result) => logger.info(
                "AWS-SNS :: publish a message. topic = %s, message = %s, result = %s"
                  .format(topic, message, result.toString()))
              case Failure(ex)     => logger.error(
                "AWS-SNS :: failed to publish a message. topic = %s, message = %s"
                  .format(topic, message), ex)
            }
          }
        }
      } yield resultSeq
    }
}
