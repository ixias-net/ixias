/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.util

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.meta.MTable
import slick.driver.JdbcProfile

import ixias.persistence.model.{ Table, Converter }
import ixias.persistence.backend.{ SlickDBActionProvider, SlickRunDBActionProvider }

/**
 * The utility tool to manage database with using Slick library.
  */
trait SlickToolProvider[P <: JdbcProfile]
    extends SlickDBActionProvider[P] with SlickRunDBActionProvider[P]
{
  /** The configured driver. */
  protected implicit val driver: P

  /**
   * Show create table SQL statements.
   */
  def showCreateTable[T <: Table[_, P]](table: T)(implicit conv: Converter[_, _]): Future[Unit] = {
    import driver.api._
    SlickDBAction(table) { case (_, slick) =>
      slick.asInstanceOf[T#BasicQuery]
        .schema.create.statements.foreach(println)
      Future.successful(())
    }
  }

  /**
   * Create database table by specified table schema.
   */
  def createTable[T <: Table[_, P]](table: T)(implicit conv: Converter[_, _]): Future[Unit] = {
    import driver.api._
    SlickRunDBAction(table) { slick =>
      for {
        tables <- MTable.getTables
        _      <- tables.exists(_.name.name == slick.baseTableRow.tableName) match {
          case false => slick.asInstanceOf[T#BasicQuery].schema.create
          case true  => DBIO.successful(Unit)
        }
      } yield ()
    }
  }

  /**
   * Drop database table by specified table schema.
   */
  def dropTable[T <: Table[_, P]](table: T)(implicit conv: Converter[_, _]): Future[Unit] = {
    import driver.api._
    SlickRunDBAction(table) { slick =>
      for {
        tables <- MTable.getTables
        _      <- tables.exists(_.name.name == slick.baseTableRow.tableName) match {
          case true  => slick.asInstanceOf[T#BasicQuery].schema.drop
          case false => DBIO.successful(Unit)
        }
      } yield ()
    }
  }
}
