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
import slick.dbio.{ DBIOAction, NoStream }

import core.util.Logger
import core.port.adapter.persistence.model.{ DataSourceName, Table, Converter }
import core.port.adapter.persistence.backend.SlickBackend

/** Factory Object */
object SlickRunDBAction {

  /** The default using key of DSN map. */
  val DEFAULT_DSN_KEY = DataSourceName.RESERVED_NAME_MASTER

  /** Returns a future of a result */
  def apply[A, B, P <: JdbcProfile, T <: Table[_, P]]
    (table: T, keyDSN: String = DEFAULT_DSN_KEY)
    (action: T#Query => DBIOAction[A, NoStream, Nothing])
    (implicit driver: P, conv: Converter[A, B]): Future[B] =
  {
    for {
      dsn <- Future(table.dsn.get(keyDSN).get)
      v1  <- (new SlickDBAction[P, T]).invokeBlock(SlickDBActionRequest[P, T](dsn, table), {
        case (db, slick) => db.run(action(slick))
      })
      v2  <- Future(conv.convert(v1))
    } yield (v2)
  }
}
