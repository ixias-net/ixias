/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import slick.driver.JdbcProfile
import core.domain.model.Entity
import core.port.adapter.persistence.lifted._
import core.port.adapter.persistence.backend.SlickBackend

/**
 * The profile for persistence with using the Slick library.
 */
trait SlickProfile[K, E <: Entity[K], P <: JdbcProfile]
    extends Profile[K, E] with ExtensionMethodConversions { self =>

  /** The back-end type required by this profile */
  type Backend = SlickBackend[P]

  /** The configured driver. */
  val driver: P

  /** The back-end implementation for this profile */
  protected implicit lazy val backend = new SlickBackend[P] { val driver = self.driver }

  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  trait API extends super.API with driver.API
      with SlickColumnOptionOps
      with SlickColumnTypeOps[P] {
    lazy val driver = self.driver
  }
  val api: API = new API {}

  /** Run an Action synchronously and return the result as a `Future`. */
  /*
  def runWithDatabase[R, T](dsn: String)(action: => DBIOAction[R, NoStream, Nothing])
    (implicit ctx: Context, codec: R => T): Future[T] =
    (for {
      db    <- Future.fromTry(backend.getDatabase(dsn))
      value <- db.run(action).map(codec)
    } yield value) andThen {
      case Failure(ex) => actionLogger.error("The database action failed. dsn=" + dsn, ex)
    }
  */
}
