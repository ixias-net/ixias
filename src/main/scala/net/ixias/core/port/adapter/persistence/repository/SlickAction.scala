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
import core.port.adapter.persistence.model.{ DataSourceName, Table, Converter }

/** Provides actions */
trait SlickAction[P <: JdbcProfile] { self: SlickProfile[_, _, P] =>

  /** Run the supplied function with a database object by using pool database session. */
  object DBAction extends Action[Backend, Database] {

    /** Returns a future of a result */
    def apply[A, B](table: Table[_, Driver], keyDSN: String)(block: Database => Future[A])
      (implicit backend: Backend, conv: Converter[A, B]): Future[B] =
      for {
        dsn <- Future(table.dsn.get(keyDSN).get)
        v1  <- invokeBlock(backend, dsn, block)
        v2  <- Future(conv.convert(v1))
      } yield(v2)


    /** Invoke the block. */
    def invokeBlock[A](backend: Backend, dsn: DataSourceName, block: Database => Future[A]): Future[A] =
      (for {
        db <- backend.getDatabase(dsn)
        v  <- block(db)
      } yield v) andThen {
        case Failure(ex) => logger.error("The database action failed. dsn=" + dsn.toString, ex)
      }
  }
}
