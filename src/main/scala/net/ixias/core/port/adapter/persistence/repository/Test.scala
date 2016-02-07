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
import com.typesafe.config.Config

import core.domain.model.Entity
import core.port.adapter.persistence.lifted._
import core.port.adapter.persistence.backend.{ DataConverter, SlickBackend }
import core.port.adapter.persistence.io.EntityIOActionContext

trait Test[P <: JdbcProfile] extends Profile { self =>

  type Backend  = SlickBackend[P]
  type Database = Backend#Database
  type Context  = EntityIOActionContext

  val driver: P
  val backend = new SlickBackend[P] {}


  def runInternal[R](backend: Backend, dsn: String, block: => DBIOAction[R, NoStream, Nothing])
    (implicit ctx: Context):  Future[T] {
    (for {
      db    <- Future.fromTry(backend.getDatabase(driver, dsn))
      value <- f(db)
    } yield value)
  }

  def runInternal[R](db: Database, block: => DBIOAction[R, NoStream, Nothing]): Future[R] = db.run(block)
}

