/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.action

import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.JdbcProfile

import core.util.Logger
import core.port.adapter.persistence.model.{ DataSourceName, Table, Converter }
import core.port.adapter.persistence.backend.SlickBackend

/** The action request. */
sealed case class SlickDBActionRequest[P <: JdbcProfile, T <: Table[_, P]](
  val dsn:   DataSourceName,
  val table: T
)

/** Run the supplied function with a database object
  * by using pool database session. */
class SlickDBAction[P <: JdbcProfile, T <: Table[_, P]](implicit driver: P)
    extends Action[SlickDBActionRequest[P, T], (SlickBackend[P]#Database, T#Query)]
{
  type Request       =  SlickDBActionRequest[P, T]
  type BlockArgument = (SlickBackend[P]#Database, T#Query)

  /** The logger for profile */
  protected lazy val logger  = Logger()

  /** The back-end implementation for this profile */
  protected lazy val backend = SlickBackend()

  /** Invoke the block. */
  def invokeBlock[A](request: Request, block: BlockArgument => Future[A]): Future[A] =
    (for {
      db <- backend.getDatabase(request.dsn)
      v  <- block((db, request.table.query))
    } yield v) andThen {
      case Failure(ex) => logger.error(
        "The database action failed. dsn=" + request.dsn.toString, ex)
    }
}

/** Factory Object */
object SlickDBAction {

  /** The default using key of DSN map. */
  val DEFAULT_DSN_KEY = DataSourceName.RESERVED_NAME_MASTER

  /** Returns a future of a result */
  def apply[A, B, P <: JdbcProfile, T <: Table[_, P]]
    (table: T, keyDSN: String = DEFAULT_DSN_KEY)
    (block: ((SlickBackend[P]#Database, T#Query)) => Future[A])
    (implicit driver: P, conv: Converter[A, B]): Future[B] =
  {
    for {
      dsn <- Future(table.dsn.get(keyDSN).get)
      v1  <- (new SlickDBAction[P, T]).invokeBlock(SlickDBActionRequest[P, T](dsn, table), block)
      v2  <- Future(conv.convert(v1))
    } yield (v2)
    ???
  }
}
