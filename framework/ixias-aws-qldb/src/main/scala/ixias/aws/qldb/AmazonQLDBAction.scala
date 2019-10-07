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
import software.amazon.qldb.{ QldbSession, Result => QLDBResult }

import ixias.persistence.action.BasicAction
import ixias.persistence.model.DataSourceName

trait AmazonQLDBActionProvider { self: AmazonQLDBProfile =>

  /**
   * The Request of Invocation.
   */
  sealed case class Request(
    val dsn: DataSourceName
  )

  // --[ Alias ]----------------------------------------------------------------
  val RunDBAction = DBAction.apply _

  // --[ Action ]---------------------------------------------------------------
  /**
   * The base action to execute query.
   */
  object DBAction extends BasicAction[Request, QldbSession] {

    /**
     * Execute self action.
     */
    def apply[A, B, T <: Table[_]](table: T)
      (action: QldbSession => Future[A])(implicit conv: A => B): Future[B] =
      invokeBlock(Request(table.dsn), tx => action(tx))
        .map(conv(_))
        .map(table.migrate(_))

    /**
     * Invoke the block.
     */
    def invokeBlock[A](req: Request, block: QldbSession => Future[A]): Future[A] =
      (for {
        session <- backend.getDatabase(req.dsn)
        value   <- block(session)
      } yield value) andThen {
        case scala.util.Failure(ex) => logger.error(
          "The database action failed. dsn=%s".format(req.dsn.toString), ex)
      }
  }

  // --[ Implicit Conv: For-Write ]---------------------------------------------
  /**
   * Implicit converter: model data -> IonValue row data.
   */
  implicit def convModelToIonValue[M](row: M): Seq[IonValue] =
    AmazonQLDBActionProvider
      .MAPPER_FOR_ION.writeValueAsIonValue(row)

  // --[ Implicit Conv: For-Read ]----------------------------------------------
  /**
   * Implicit converter: qldb Result -> IonValue rows.
   */
  implicit def convResultToIonValue(result: QLDBResult): Seq[IonValue] = {
    val rows = new java.util.ArrayList[IonValue]()
    result.iterator().forEachRemaining(row => rows.add(row))
    rows.asScala
  }

  /**
   * Implicit converter: qldb Result -> Model rows.
   */
  implicit def convResultToModel[M](result: QLDBResult)(implicit ctag: reflect.ClassTag[M]): Seq[M] =
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

