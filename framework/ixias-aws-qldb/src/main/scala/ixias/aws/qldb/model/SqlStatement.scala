/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.model

import scala.util.Try
import collection.JavaConverters._
import com.amazon.ion.IonValue
import software.amazon.qldb.{ QldbSession, TransactionExecutor }

/**
 * Sql prepare statement
 */
trait SqlStatement {

  type Result          =  ResultContainer#Result
  type ResultContainer <: SqlResultContainer

  val query:  String
  val params: Seq[IonValue]

  /**
   * Store in container
   */
  def toResult(data: software.amazon.qldb.Result): ResultContainer

  /**
   * Execute query
   */
  def execute(session: QldbSession): Try[Result] =
    Try {
      toResult(session.execute(query, params.asJava)).value
    }

  /**
   * Execute query with transaction
   */
  def execute(tx: TransactionExecutor): Try[Result] =
    Try {
      toResult(tx.execute(query, params.asJava)).value
    }
}

/**
 * Companion object
 */
object SqlStatement {

  /**
   * Design that returns a single record result with `Option` type
   */
  case class ForSingleResult[M](
    query:  String,
    params: Seq[IonValue]
  )(implicit val ctag: reflect.ClassTag[M]) extends SqlStatement {

    /**
     * Store in container
     */
    def toResult(data: software.amazon.qldb.Result): ResultContainer =
      ResultContainer(data)

    /**
     * Result container definition
     */
    case class ResultContainer(data: software.amazon.qldb.Result) extends SqlResultContainer with ConvOps {
      type Model  = M
      type Result = Option[Model]
      lazy val value = data.toModelSeq.headOption
    }
  }

  /**
   * Design that returns a single record result with `Seq` type
   */
  case class ForMultiResult[M](
    query:  String,
    params: Seq[IonValue]
  )(implicit val ctag: reflect.ClassTag[M]) extends SqlStatement {

    /**
     * Set flags to get only a head of the result data
     */
    def headOption = ForSingleResult(query, params)

    /**
     * Store in container
     */
    def toResult(data: software.amazon.qldb.Result): ResultContainer =
      ResultContainer(data)

    /**
     * Result container definition
     */
    case class ResultContainer(data: software.amazon.qldb.Result) extends SqlResultContainer with ConvOps {
      type Model  = M
      type Result = Seq[Model]
      lazy val value = data.toModelSeq
    }
  }
}

