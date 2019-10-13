/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.dbio

import collection.JavaConverters._
import scala.concurrent.{ Future, ExecutionContext }
import software.amazon.qldb.{ QldbSession, TransactionExecutor }
import ixias.aws.qldb.model.{ SqlStatement, ConvOps }

/**
 * Executor for database IO/Action.
 */
case class DBIOAction(session: QldbSession) extends ConvOps {

  /** Execute query */
  def execute[A](stmt: SqlStatement)
    (implicit ctag: reflect.ClassTag[A], ex: ExecutionContext): Future[Seq[A]] =
    Future {
      session.execute(stmt.query, stmt.params.asJava)
        .toModelSeq(ctag)
    }

  /** Transaction block */
  def transaction[A](block: DBIOActionWithTxt => Future[A]): Future[A] =
    session.execute(tx => block(DBIOActionWithTxt(tx)))
}

/**
 * Executor for database IO/Action with transaction.
 */
case class DBIOActionWithTxt(tx: TransactionExecutor) extends ConvOps {

  /** Execute query with transaction */
  def execute[A](stmt: SqlStatement)
    (implicit ctag: reflect.ClassTag[A], ex: ExecutionContext): Future[Seq[A]] =
    Future {
      tx.execute(stmt.query, stmt.params.asJava)
        .toModelSeq(ctag)
    }
}

