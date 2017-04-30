/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence

import scala.concurrent.{ Future, ExecutionContext }
import ixias.persistence.model.DataSourceName
import ixias.persistence.lifted.Aliases
import ixias.persistence.backend.AmazonSNSBackend
import ixias.persistence.dbio.Execution
import ixias.util.Logging

// Amazon SNS
//~~~~~~~~~~~~
trait AmazonSNS extends Aliases with Logging {

  // --[ Typedefs ]-------------------------------------------------------------
  type PublishResult = com.amazonaws.services.sns.model.PublishResult

  // --[ Alias ]----------------------------------------------------------------
  val DataSourceName = ixias.persistence.model.DataSourceName

  // --[ Properties ]-----------------------------------------------------------
  /** The data source name */
  implicit val dsn: DataSourceName

  /** The backend */
  protected lazy val backend = AmazonSNSBackend()

  /** The Execution Context */
  protected implicit val ctx: ExecutionContext = Execution.Implicits.trampoline

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Sends a message to a topic's subscribed endpoints.
   */
  def publish(message: String): Future[Seq[PublishResult]] = {
    backend.isSkip(dsn) match {
      case true  => Future.successful(Seq.empty)
      case false => for {
        client   <- backend.getDatabase(dsn)
        topicSeq <- Future.fromTry(backend.getTopicARN(dsn))
      } yield topicSeq map {
        topic => {
          val msgFmt = "AWS-SNS :: publish a message. topic = %s, message = %s, result = %s"
          val result = client.publish(topic, message)
          logger.info(msgFmt.format(topic, message, result.toString()))
          result
        }
      }
    }
  }
}
