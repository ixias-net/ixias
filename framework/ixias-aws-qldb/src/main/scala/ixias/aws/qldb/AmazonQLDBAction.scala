/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb

import scala.concurrent.Future
import scala.language.implicitConversions
import collection.JavaConverters._

import com.amazon.ion.IonValue
import software.amazon.qldb.{ Result, TransactionExecutor }

import ixias.persistence.action.BasicAction
import ixias.persistence.model.DataSourceName

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
  private object DBAction extends BasicAction[Request, TransactionExecutor] {
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

  /**
   * The Database Acion
   */
  object RunDBAction {
    def apply[A, B, T <: Table[_]](table: T)
      (action: TransactionExecutor => Future[A])(implicit conv: A => B) = {
      val req = Request(table.dsn)
      DBAction.invokeBlock(req, tx => action(tx).map(conv(_)))
    }
  }

  /**
   * Implicit converter: qldb Result -> IonValue rows.
   */
  implicit def convResultToIonValue(result: Result): Seq[IonValue] = {
    val rows = new java.util.ArrayList[IonValue]()
    result.iterator().forEachRemaining(row => rows.add(row))
    rows.asScala
  }

  /**
   * Implicit converter: qldb Result -> Model rows.
   */
  implicit def convResultToModel[M](result: Result)(implicit ctag: reflect.ClassTag[M]): Seq[M] =
    convResultToIonValue(result).map(
      (row: IonValue) => AmazonQLDBActionProvider
        .MAPPER_FOR_ION.readValue(row, ctag.runtimeClass)
        .asInstanceOf[M]
    )
}

// Companion object
//~~~~~~~~~~~~~~~~~~
object AmazonQLDBActionProvider {
  import com.fasterxml.jackson.dataformat.ion.IonObjectMapper
  import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
  import com.fasterxml.jackson.databind.SerializationFeature
  import com.fasterxml.jackson.module.scala.DefaultScalaModule

  /**
   * Mapper for Ion object.
   */
  lazy val MAPPER_FOR_ION = {
    val mapper = new IonObjectMapper()
    mapper.registerModule(new JavaTimeModule)
      .registerModule(DefaultScalaModule)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    mapper
  }
}

