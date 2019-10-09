/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.backend

import scala.concurrent.Future
import software.amazon.qldb.QldbSession

import ixias.aws.qldb.AmazonQLDBProfile
import ixias.aws.qldb.model.Table
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
    def apply[A, B, T <: Table](table: T)
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

