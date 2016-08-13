/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag

import shade.memcached.MemcachedCodecs
import ixias.model.{ Identity, Entity }
import ixias.persistence.model.DataSourceName

/**
 * The base repository which is implemented basic feature methods.
 */
abstract class ShadeRepository[K <: Identity[_], V <: Entity[K]](implicit ttag: ClassTag[V])
    extends ShadeProfile[K, V] with MemcachedCodecs
{
  // --[ Methods ]--------------------------------------------------------------
  val dsn: DataSourceName

  // --[ Methods ]--------------------------------------------------------------
  /** Fetches a value from the cache store. */
  def get(key: Id): Future[Option[V]] =
    DBAction(dsn) { db =>
      db.get[V](key.get.toString)
    } recoverWith {
      case _: java.io.InvalidClassException => Future.successful(None)
    }

  // --[ Methods ]--------------------------------------------------------------
  /** Sets a (key, value) in the cache store. */
  def store(value: V, expiry: Duration = Duration.Inf): Future[Id] =
    DBAction(dsn) { db =>
      for {
        _ <- db.set(value.id.get.toString, value, expiry)
      } yield (value.id)
    }

  /** Update existing value expiry in the cache store. */
  def updateExpiry(key: Id, expiry: Duration = Duration.Inf): Future[Unit] =
    DBAction(dsn) { db =>
      for {
        Some(v) <- db.get[V](key.get.toString)
        _       <- db.set(key.get.toString, v, expiry)
      } yield ()
    } recoverWith {
      case _: NoSuchElementException
         | _: java.io.InvalidClassException => Future.successful(Unit)
    }

  /** Deletes a key from the cache store. */
  def remove(key: Id): Future[Option[V]] =
    DBAction(dsn) { db =>
      for {
        old <- db.get[V](key.get.toString)
        _   <- db.delete(key.get.toString)
      } yield old
    } recoverWith {
      case _: java.io.InvalidClassException => Future.successful(None)
    }
}
