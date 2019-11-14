/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.backend

import scala.concurrent.Future
import ixias.persistence.action.BasicAction

import ixias.aws.qldb.model.Table
import ixias.aws.qldb.dbio.DBIOAction
import ixias.aws.qldb.AmazonQLDBProfile

trait AmazonQLDBActionProvider { self: AmazonQLDBProfile =>

  /**
   * The Request of Invocation.
   */
  sealed case class DBActionRequest[T <: Table[_, _]](table: T)

  /**
   * The base action to execute query.
   */
  sealed case class DBAction[T <: Table[_, _]]()
      extends BasicAction[DBActionRequest[T], (DBIOAction, T#Query)] {
    type Request          = DBActionRequest[T]
    type BlockFunction[A] = ((DBIOAction, T#Query)) => Future[A]

    /** Invoke the block. */
    def invokeBlock[A](req: Request, block: BlockFunction[A]): Future[A] =
      (for {
        session <- backend.getDatabase(req.table.dsn)
        value   <- block(DBIOAction(session) -> req.table.query)
      } yield value) andThen {
        case scala.util.Failure(ex) => logger.error(
          "The database action failed. dsn=%s".format(req.table.dsn.toString), ex)
      }
  }

  /**
   * Execute database action.
   */
  object RunDBAction {
    def apply[A, B, T <: Table[_, _]]
      (table: T)
      (block: ((DBIOAction, T#Query)) => Future[A])
      (implicit conv: A => B): Future[B] =
      DBAction[T].invokeBlock(DBActionRequest(table), block)
        .map(conv)
  }
}
