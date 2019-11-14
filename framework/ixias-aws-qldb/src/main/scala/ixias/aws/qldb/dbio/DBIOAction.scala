/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.dbio

import scala.concurrent.{ Future, ExecutionContext }
import software.amazon.qldb.{ QldbSession, TransactionExecutor }
import ixias.aws.qldb.model.{ SqlStatement, ConvOps }

/**
 * Executor for database IO/Action.
 */
case class DBIOAction(session: QldbSession) extends ConvOps {

  /**
   * Execute query
   */
  def execute(stmt: SqlStatement): Future[stmt.Result] =
    Future.fromTry(stmt.execute(session))

  /**
   * Transaction block
   * Since transaction commit is called when the Executor ends,
   * it waits for the end of block processing.
   */
  def transaction[A](block: DBIOActionWithTxt => Future[A]): Future[A] =
    session.execute(tx => {
      import scala.concurrent.Await
      import scala.concurrent.duration.Duration
      import java.util.concurrent.ForkJoinPool
      implicit val ex = ExecutionContext.fromExecutor(new ForkJoinPool())
      Await.ready(
        Future.unit.flatMap(_ => block(DBIOActionWithTxt(tx))),
        Duration.Inf
      )
    })
}

/**
 * Executor for database IO/Action with transaction.
 */
case class DBIOActionWithTxt(tx: TransactionExecutor) extends ConvOps {

  /**
   * Execute query with transaction
   */
  def execute(stmt: SqlStatement): Future[stmt.Result] =
    Future.fromTry(stmt.execute(tx))
}

