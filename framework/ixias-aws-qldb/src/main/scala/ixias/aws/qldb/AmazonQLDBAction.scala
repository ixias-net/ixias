/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb

import scala.concurrent.Future
import ixias.persistence.action.BasicAction
import ixias.persistence.model.DataSourceName
import software.amazon.qldb.TransactionExecutor

trait AmazonQLDBActionProvider { self: AmazonQLDBProfile =>

  /**
   * The Request of Invocation.
   */
  sealed case class Request(
    val dsn: DataSourceName
  )

  /**
   * The base action to execute query.
   */
  private object Action extends BasicAction[Request, TransactionExecutor] {
    /**
     *  Run block process
     */
    def invokeBlock[A](req: Request, block: TransactionExecutor => Future[A]): Future[A] =
      (for {
        db      <- backend.getDatabase(req.dsn)
        session  = db.underlying
        value   <- session.execute(tx => block(tx))
      } yield value) andThen {
        case scala.util.Failure(ex) => logger.error(
          "The database action failed. dsn=%s".format(req.dsn.toString), ex)
      }
  }
}
