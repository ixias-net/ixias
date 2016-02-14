/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.MySQLDriver

import core.domain.model._
import core.port.adapter.persistence.model

object UserRepository extends { val driver = MySQLDriver }
    with SlickProfile[Long, User, MySQLDriver]
{
  import api._
  val UserTable = model.UserTable[MySQLDriver](driver)

  // --[ Read ]-----------------------------------------------------------------
  /** Optionally returns the value associated with a identity. */
  def get(id: Id): Future[Option[User]] = ???

  // --[ Write ]----------------------------------------------------------------
  /** Adds a new identity/entity-value pair to this repository.
    * If the map already contains a mapping for the identity,
    * it will be overridden by the new value */
  def store(entity: User): Future[Unit] = ???
  // DBAction(UserTable, "master") { db =>
  //   ???
  // }

  /** Removes a identity from this map,
    * returning the value associated previously with that identity as an option. */
  def remove(id: Id): Future[Option[User]] =
    DBAction(UserTable) { case (db, slick) =>
      db.run(for {
        old <- slick.filter(_.id === id.get).result.headOption
        _   <- slick.filter(_.id === id.get).delete
      } yield old)
    }
}
