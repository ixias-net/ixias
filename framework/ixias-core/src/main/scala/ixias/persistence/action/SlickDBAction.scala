/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.action

import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.JdbcProfile
import ixias.util.Logger
import ixias.persistence.model.{ DataSourceName, Table, Converter }
import ixias.persistence.backend.SlickBackend

/**
 * The action request.
 */
sealed case class SlickDBActionRequest[P <: JdbcProfile, T <: Table[_, P]](
  val dsn:   DataSourceName,
  val table: T
)

/**
 * Run the supplied function with a database object
 * by using pool database session.
 */
sealed class SlickDBAction[P <: JdbcProfile, T <: Table[_, P]](implicit driver: P)
     extends BasicAction[SlickDBActionRequest[P, T], (SlickBackend[P]#Database, T#Query)]
{
  type Request       =  SlickDBActionRequest[P, T]
  type BlockArgument = (SlickBackend[P]#Database, T#Query)

  /** The logger for profile */
  protected lazy val logger  = Logger.apply

  /** The back-end implementation for this profile */
  protected lazy val backend = SlickBackend[P]

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

/**
 * The provider for `SlickDBAction`
 */
trait SlickDBActionProvider[P <: JdbcProfile]
{
  object SlickDBAction {
    /** The type of slick driver */
    type Driver  = P

    /** The back-end type required by this profile */
    type Backend = SlickBackend[P]

    /** The type of database objects. */
    type Database = Backend#Database

    /** The default using key of DSN map. */
    val DEFAULT_DSN_KEY = DataSourceName.RESERVED_NAME_MASTER

    /** Returns a future of a result */
    def apply[A, B, T <: Table[_, Driver]]
      (table: T, keyDSN: String = DEFAULT_DSN_KEY)
      (block: ((Database, T#Query)) => Future[A])
      (implicit driver: Driver, conv: Converter[A, B]): Future[B] =
    {
      for {
        dsn <- Future(table.dsn.get(keyDSN).get)
        v1  <- (new SlickDBAction[Driver, T]).invokeBlock(SlickDBActionRequest[Driver, T](dsn, table), block)
        v2  <- Future(conv.convert(v1))
      } yield (v2)
    }
  }
}
