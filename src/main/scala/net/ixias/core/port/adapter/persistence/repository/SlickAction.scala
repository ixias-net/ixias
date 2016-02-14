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
import slick.driver.JdbcProfile
import slick.lifted.TableQuery
import core.port.adapter.persistence.model.{ DataSourceName, Table, Converter }

/** Provides actions */
trait SlickAction[P <: JdbcProfile] { self: SlickProfile[_, _, P] =>

  /** The default using key of DSN map. */
  val DEFAULT_DSN_KEY = DataSourceName.RESERVED_NAME_MASTER

  /** The action request. */
  protected case class SlickActionRequest(
    val backend: Backend,
    val dsn:     DataSourceName,
    val table:   Table[Driver]
  ) extends ActionRequest[Backend]

  /** Run the supplied function with a database object by using pool database session. */
  object DBAction extends Action[SlickActionRequest, (Database, TableQuery[_])] {

    /** Invoke the block. */
    def invokeBlock[A](request: SlickActionRequest, block: ((Database, TableQuery[_])) => Future[A]): Future[A] =
      (for {
        db <- request.backend.getDatabase(request.dsn)
        v  <- block((db, request.table.query))
      } yield v) andThen {
        case Failure(ex) => logger.error(
          "The database action failed. dsn=" + request.dsn.toString, ex)
      }

    /** Returns a future of a result */
    def apply[A, B](table: Table[Driver], keyDSN: String = DEFAULT_DSN_KEY)(block: ((Database, TableQuery[_])) => Future[A])
      (implicit backend: Backend, conv: Converter[A, B]): Future[B] =
      for {
        dsn <- Future(table.dsn.get(keyDSN).get)
        v1  <- invokeBlock(SlickActionRequest(backend, dsn, table), block)
        v2  <- Future(conv.convert(v1))
      } yield(v2)
  }
}
