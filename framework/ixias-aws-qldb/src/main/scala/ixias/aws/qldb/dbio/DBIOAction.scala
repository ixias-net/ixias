/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.dbio

import collection.JavaConverters._
import software.amazon.qldb.{ QldbSession, TransactionExecutor, Result }
import ixias.aws.qldb.model.{ SqlStatement, ConvOps }

/**
 * Executor for database IO/Action.
 */
case class DBIOAction(session: QldbSession) extends ConvOps {

  /** Execute query */
  def execute[A](stmt: SqlStatement)(implicit conv: Result => A): A =
    conv(session.execute(stmt.query, stmt.params.asJava))

  /** Transaction block */
  def execute[A](block: DBIOActionWithTxt => A): A =
    session.execute(tx => block(DBIOActionWithTxt(tx)))
}

/**
 * Executor for database IO/Action with transaction.
 */
case class DBIOActionWithTxt(tx: TransactionExecutor) extends ConvOps {

  /** Execute query with transaction */
  def execute[A](stmt: SqlStatement)(implicit conv: Result => A): A =
    conv(tx.execute(stmt.query, stmt.params.asJava))
}

