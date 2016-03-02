/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.util

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.JdbcProfile
import core.port.adapter.persistence.model.{ Table, Converter }
import core.port.adapter.persistence.action.{ SlickDBActionProvider, SlickRunDBActionProvider }

/**
 * The utility tool to manage database with using Slick library.
  */
trait SlickToolProvider[P <: JdbcProfile]
    extends SlickDBActionProvider[P] with SlickRunDBActionProvider[P]
{
  /** The configured driver. */
  protected implicit val driver: P

  /** Show create table SQL statements. */
  def showCreateTable[T <: Table[_, P]](table: T)(implicit conv: Converter[_, _]): Future[Unit] = {
    import driver.api._
    SlickDBAction(table) { case (_, slick) =>
      slick.asInstanceOf[T#BasicQuery]
        .schema.create.statements.foreach(println)
      Future.successful(Unit)
    }
  }

  /** Create table. */
  def createTable[T <: Table[_, P]](table: T)(implicit conv: Converter[_, _]): Future[Unit] = {
    import driver.api._
    SlickRunDBAction(table) { slick =>
      slick.asInstanceOf[T#BasicQuery].schema.create
    } recoverWith {
      case _: slick.SlickException => Future.successful(Unit)
    }
  }

  /** Drop table. */
  def dropTable[T <: Table[_, P]](table: T)(implicit conv: Converter[_, _]): Future[Unit] = {
    import driver.api._
    SlickRunDBAction(table) { slick =>
      slick.asInstanceOf[T#BasicQuery].schema.drop
    } recoverWith {
      case _: slick.SlickException => Future.successful(Unit)
    }
  }
}
