/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence

import scala.reflect.ClassTag
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import shade.memcached.MemcachedCodecs

import ixias.model.{ Identity, Entity }
import ixias.persistence.model.DataSourceName
import ixias.persistence.backend.ShadeBackend
import ixias.persistence.action.ShadeDBActionProvider

/**
 * The profile for persistence with using the Shade library.
 */
private[persistence] trait ShadeProfile extends Profile with ShadeDBActionProvider {

  // --[ Typedefs ]-------------------------------------------------------------
  /** The back-end type required by this profile */
  type Backend = ShadeBackend

  // --[ Alias ]----------------------------------------------------------------
  val DataSourceName = ixias.persistence.model.DataSourceName

  // --[ Properties ]-----------------------------------------------------------
  /** The back-end implementation for this profile */
  protected lazy val backend = ShadeBackend()

  /** Database Action Helpers */
  protected val DBAction = ShadeDBAction

  /**
   * The API for using the utility methods with a single import statement.
   * This provides the repository's implicits, the Database connections,
   * and commonly types and objects.
   */
  trait API extends super.API
  val api: API = new API {}
}

/**
 * The base repository which is implemented basic feature methods of memcached.
 */
abstract class ShadeRepository[K <: Identity[_], E <: Entity[K]](implicit ttag: ClassTag[E])
    extends Repository[K, E] with ShadeProfile with MemcachedCodecs
{
  import api._

  // --[ Methods ]--------------------------------------------------------------
  val dsn: DataSourceName

  // --[ Methods ]--------------------------------------------------------------
  /** Fetches a value from the cache store. */
  def get(key: Id): Future[Option[E]] =
    DBAction(dsn) { db =>
      db.get[E](key.get.toString)
    } recoverWith {
      case _: java.io.InvalidClassException => Future.successful(None)
    }

  // --[ Methods ]--------------------------------------------------------------
  /** Sets a (key, value) in the cache store. */
  def store(value: E): Future[Id] = store(value, Duration.Inf)
  def store(value: E, expiry: Duration): Future[Id] =
    DBAction(dsn) { db =>
      for {
        _ <- db.set(value.id.get.toString, value, expiry)
      } yield (value.id)
    }

  /** Update existing value expiry in the cache store. */
  def updateExpiry(key: Id, expiry: Duration = Duration.Inf): Future[Unit] =
    DBAction(dsn) { db =>
      for {
        Some(v) <- db.get[E](key.get.toString)
        _       <- db.set(key.get.toString, v, expiry)
      } yield ()
    } recoverWith {
      case _: NoSuchElementException
         | _: java.io.InvalidClassException => Future.successful(Unit)
    }

  /** Deletes a key from the cache store. */
  def remove(key: Id): Future[Option[E]] =
    DBAction(dsn) { db =>
      for {
        old <- db.get[E](key.get.toString)
        _   <- db.delete(key.get.toString)
      } yield old
    } recoverWith {
      case _: java.io.InvalidClassException => Future.successful(None)
    }
}
