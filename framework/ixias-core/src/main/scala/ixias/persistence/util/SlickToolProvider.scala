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

import ixias.persistence.SlickProfile
import ixias.persistence.model.{ Table, Converter }

/**
 * The utility tool to manage database with using Slick library.
  */
trait SlickToolProvider[P <: JdbcProfile] extends SlickProfile[P]
{
  /** The configured driver. */
  protected implicit val driver: P

  import driver.api._
  import SlickDBActionTypes._

  /**
   * Show create table SQL statements.
   */
  def showCreateTable(table: SlickTable)(implicit conv: Converter[_, _]): Future[Unit] = {
    SlickDBAction(table) { case (_, slick) =>
      slick.asInstanceOf[SlickTable#BasicQuery]
        .schema.create.statements.foreach(println)
      Future.successful(())
    }
  }

  /**
   * Create database table by specified table schema.
   */
  def createTable(table: SlickTable)(implicit conv: Converter[_, _]): Future[Unit] = {
    SlickRunDBAction(table) { slick =>
      for {
        tables <- MTable.getTables
        _      <- tables.exists(_.name.name == slick.baseTableRow.tableName) match {
          case false => slick.asInstanceOf[SlickTable#BasicQuery].schema.create
          case true  => DBIO.successful(Unit)
        }
      } yield ()
    }
  }

  /**
   * Drop database table by specified table schema.
   */
  def dropTable(table: SlickTable)(implicit conv: Converter[_, _]): Future[Unit] = {
    import driver.api._
    SlickRunDBAction(table) { slick =>
      for {
        tables <- MTable.getTables
        _      <- tables.exists(_.name.name == slick.baseTableRow.tableName) match {
          case true  => slick.asInstanceOf[SlickTable#BasicQuery].schema.drop
          case false => DBIO.successful(Unit)
        }
      } yield ()
    }
  }
}
