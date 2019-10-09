/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.dbio

import software.amazon.qldb.{ QldbSession, TransactionExecutor }
import ixias.aws.qldb.model.TableQuery

/**
 * Database
 */
case class Database(session: QldbSession) {
  import collection.JavaConverters._
  type SqlStatement = TableQuery#SqlStatement

  /** Execute query */
  def execute(stmt: SqlStatement) =
    session.execute(stmt.query, stmt.params.asJava)

  /** Transaction block */
  def execute[A](block: DatabaseTransactionExecutor => A): A =
    session.execute(tx => block(DatabaseTransactionExecutor(tx)))
}

case class DatabaseTransactionExecutor(tx: TransactionExecutor) {
  import collection.JavaConverters._

  type SqlStatement = TableQuery#SqlStatement

  /** Execute query with transaction */
  def execute(stmt: SqlStatement) =
    tx.execute(stmt.query, stmt.params.asJava)
}

