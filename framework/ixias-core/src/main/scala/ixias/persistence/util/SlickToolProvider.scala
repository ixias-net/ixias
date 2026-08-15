/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.util

import scala.concurrent.Future
import slick.jdbc.JdbcProfile
import slick.jdbc.meta.MTable

import ixias.persistence.SlickProfile
import ixias.persistence.model.Table

/**
 * The utility tool to manage database with using Slick library.
  */
trait SlickToolProvider[P <: JdbcProfile] extends SlickProfile[P] {

  /**
   * Show create table SQL statements.
   */
  def showCreateTable[T <: Table[_, P]](table: T): Future[Unit] =
    SlickDBAction(table) { case (_, slick) =>
      import driver.api._
      slick.asInstanceOf[T#BasicQuery]
        .schema.create.statements.foreach(println)
      Future.successful(())
    }

  /**
   * Create database table by specified table schema.
   */
  def createTable[T <: Table[_, P]](table: T): Future[Unit] =
    SlickRunDBAction(table) { slick =>
      import driver.api._
      for {
        tables <- MTable.getTables
        _      <- tables.exists(_.name.name == slick.baseTableRow.tableName) match {
          case false => slick.asInstanceOf[T#BasicQuery].schema.create
          case true  => DBIO.successful(())
        }
      } yield ()
    }

  /**
   * Drop database table by specified table schema.
   */
    def dropTable[T <: Table[_, P]](table: T): Future[Unit] =
      SlickRunDBAction(table) { slick =>
        import driver.api._
        for {
          tables <- MTable.getTables
          _      <- tables.exists(_.name.name == slick.baseTableRow.tableName) match {
            case true  => slick.asInstanceOf[T#BasicQuery].schema.drop
            case false => DBIO.successful(())
        }
      } yield ()
    }
}
