/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import slick.dbio.{ DBIOAction, NoStream }
import slick.driver.JdbcProfile
import core.port.adapter.persistence.model.{ DataSourceName, Table, Converter }

/** Provides actions */
trait SlickAction[P <: JdbcProfile] { self: SlickProfile[_, _, P] =>

  /** The default using key of DSN map. */
  val DEFAULT_DSN_KEY = DataSourceName.RESERVED_NAME_MASTER

  /** The action request. */
  protected case class SlickActionRequest[T <: Table[_, Driver]](
    val backend: Backend,
    val dsn:     DataSourceName,
    val table:   T
  ) extends ActionRequest[Backend]


  /** Run the supplied function with a database object by using pool database session. */
  class DBAction[T <: Table[_, Driver]] extends Action[SlickActionRequest[T], (Database, T#Query)] {

    /** Invoke the block. */
    def invokeBlock[A](request: SlickActionRequest[T], block: ((Database, T#Query)) => Future[A]): Future[A] =
      (for {
        db <- request.backend.getDatabase(request.dsn)
        v  <- block((db, request.table.query))
      } yield v) andThen {
        case Failure(ex) => logger.error(
          "The database action failed. dsn=" + request.dsn.toString, ex)
      }
  }
  object DBAction {
    /** Returns a future of a result */
    def apply[T <: Table[_, Driver], A, B](table: T, keyDSN: String = DEFAULT_DSN_KEY)
      (block: ((Database, T#Query)) => Future[A])(implicit backend: Backend, conv: Converter[A, B]): Future[B] =
      for {
        dsn <- Future(table.dsn.get(keyDSN).get)
        v1  <- (new DBAction[T]).invokeBlock(SlickActionRequest(backend, dsn, table), block)
        v2  <- Future(conv.convert(v1))
      } yield (v2)
  }


  /** Run a database action by using pool database session. */
  object RunDBAction {
    /** Returns a future of a result */
    def apply[T <: Table[_, Driver], A, B](table: T, keyDSN: String = DEFAULT_DSN_KEY)
      (block: T#Query => DBIOAction[A, NoStream, Nothing])(implicit backend: Backend, conv: Converter[A, B]): Future[B] =
      ???
  }
}
