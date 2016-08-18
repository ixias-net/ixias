/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.action

import scala.util.Failure
import scala.concurrent.Future

import slick.driver.JdbcProfile
import slick.dbio.{ DBIOAction, NoStream }

import ixias.persistence.SlickProfile
import ixias.persistence.model.{ DataSourceName, Table, Converter }

/**
 * The action request.
 */
sealed case class SlickDBActionRequest[P <: JdbcProfile, T <: Table[_, P]](
  val dsn:   DataSourceName,
  val table: T
)

/**
 * The provider for `SlickDBAction`
 */
trait SlickDBActionProvider[P <: JdbcProfile] { self: SlickProfile[_, _, P] =>

  /** The default using key of DSN map. */
  val DEFAULT_DSN_KEY = DataSourceName.RESERVED_NAME_MASTER

  /** Declare Types for Database Acion */
  object SlickDBActionTypes {
    type SlickTable     <: Table[_, Driver]
    type ActionRequest  =  SlickDBActionRequest[P, SlickTable]
    type BlockArgument  = (Database, SlickTable#Query)
    type SlickAction[T] =  SlickTable#Query => DBIOAction[T, NoStream, Nothing]
  }
  import SlickDBActionTypes._

  /**
   * The feature of Database Acion
   */
  trait SlickDBAction extends BasicAction[ActionRequest, BlockArgument] {

    /** Invoke the block. */
    def invokeBlock[A](req: ActionRequest, block: BlockArgument => Future[A]): Future[A] =
      (for {
        db    <- backend.getDatabase(req.dsn)
        value <- block((db, req.table.query))
      } yield value) andThen {
        case Failure(ex) => logger.error(
          "The database action failed. dsn=".format(req.dsn.toString), ex)
      }
  }

  /**
   * The Database Acion
   */
  object SlickDBAction extends SlickDBAction {
    /** Returns a future of a result */
    def apply[A, B]
      (table: SlickTable, hostspec: String = DEFAULT_DSN_KEY)
      (block: BlockArgument => Future[A])
      (implicit driver: Driver, conv: Converter[A, B]): Future[B] =
    {
      for {
        dsn    <- Future(table.dsn.get(hostspec).get)
        value1 <- invokeBlock(new ActionRequest(dsn, table), block)
        value2 <- Future(conv.convert(value1))
      } yield (value2)
    }
  }

  /**
   * The Database Acion
   */
  object SlickRunDBAction extends SlickDBAction {
    /** Returns a future of a result */
    def apply[A, B]
      (table: SlickTable, hostspec: String = DEFAULT_DSN_KEY)
      (action: SlickAction[A])
      (implicit driver: Driver, conv: Converter[A, B]): Future[B] =
    {
      for {
        dsn    <- Future(table.dsn.get(hostspec).get)
        value1 <- invokeBlock(new ActionRequest(dsn, table), {
          case (db, slick) => db.run(action(slick))
        })
        value2 <- Future(conv.convert(value1))
      } yield (value2)
    }
  }
}
