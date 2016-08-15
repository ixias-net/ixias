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
import scala.language.implicitConversions

import slick.dbio.{ DBIOAction, NoStream }
import slick.driver.JdbcProfile
import ixias.util.Logger
import ixias.persistence.model.{ DataSourceName, Table, Converter }
import ixias.persistence.backend.SlickBackend

/**
 * The provider for `SlickRunDBAction`
 */
trait SlickRunDBActionProvider[P <: JdbcProfile] {
  object SlickRunDBAction {

    /** The type of slick driver */
    type Driver  = P

    /** The back-end type required by this profile */
    type Backend = SlickBackend[P]

    /** The default using key of DSN map. */
    val DEFAULT_DSN_KEY = DataSourceName.RESERVED_NAME_MASTER

    /** Returns a future of a result */
    def apply[A, B, T <: Table[_, Driver]]
      (table: T, keyDSN: String = DEFAULT_DSN_KEY)
      (action: T#Query => DBIOAction[A, NoStream, Nothing])
      (implicit driver: Driver, conv: Converter[A, B]): Future[B] =
    {
      for {
        dsn <- Future(table.dsn.get(keyDSN).get)
        v1  <- (new SlickDBAction[Driver, T]).invokeBlock(SlickDBActionRequest[Driver, T](dsn, table), {
          case (db, slick) => db.run(action(slick))
        })
        v2  <- Future(conv.convert(v1))
      } yield (v2)
    }
  }
}
