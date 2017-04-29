/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence

import scala.concurrent.{ Future, ExecutionContext }
import ixias.persistence.model.DataSourceName
import ixias.persistence.backend.AmazonSNSBackend
import ixias.persistence.dbio.Execution
import com.amazonaws.services.sns.model.PublishResult

// ファイル管理
//~~~~~~~~~~~~~~
trait AmazonSNSClient {

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
  def publish(message: String): Future[Seq[PublishResult]] =
    backend.isSkip(dsn) match {
      case true  => Future.successful(Seq.empty)
      case false => for {
        client   <- backend.getDatabase(dsn)
        topicSeq <- Future.fromTry(backend.getTopicARN(dsn))
      } yield topicSeq map {
        topic => client.publish(topic, message)
      }
    }
}
