/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb

import com.amazon.ion.IonValue
import software.amazon.qldb.{ QldbSession, TransactionExecutor }
import ixias.persistence.model.DataSourceName

/**
 * The model of AmazonQLDB table.
 */
trait Table[T] {

  //-- [ Properties ] ----------------------------------------------------------
  /** Data storage location information */
  val dsn:       DataSourceName

  /** Table name */
  val tableName: String

  /** Table name */
  val tableStmt: Map[String, String] = Map.empty

  case class Session(session: QldbSession) {
    def execute(stamt: String) = ???
    def execute(stamt: String, params: Seq[IonValue]) = ???
    def transaction[A](block: Transaction => A): A =
      session.execute(tx => block(new Transaction(tx)))
  }

  case class Transaction(tx: TransactionExecutor) {
    def execute(stamt: String) = ???
    def execute(stamt: String, params: Seq[IonValue]) = ???
  }

  //-- [ Utility Methods ] -----------------------------------------------------
  /**
   * Overwrite function if necessary.
   * As you add and change features in your app,
   * you need to modify your entity classes to reflect these adjust.
   */
  def migrate[A](data: A): A = data
}
