/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import scala.reflect.ClassTag
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import shade.memcached.MemcachedCodecs
import core.domain.model.Entity

/**
 * The base repository which is implemented basic feature methods.
 */
abstract class ShadeBasicRepository[K, V <: Entity[K]]
  (implicit ttag: ClassTag[V]) extends ShadeRepository[K, V] with MemcachedCodecs {

  // --[ Methods ]--------------------------------------------------------------
  /** Gets a cache client resource. */
  def withDatabase[T](f: Database => Future[T])(implicit ctx: Context): Future[T]

  /** Gets expiry time. */
  def expiry(key: Id): Duration = Duration.Inf

  // --[ Methods ]--------------------------------------------------------------
  /** Fetches a value from the cache store. */
  def get(key: Id)(implicit ctx: Context): Future[Option[V]] =
    withDatabase { db =>
      (for {
        v <- db.get[V](key.get.toString)
      } yield(v)) recoverWith {
        case _: java.io.InvalidClassException => Future.successful(None)
      }
    }

  // --[ Methods ]--------------------------------------------------------------
  /** Sets a (key, value) in the cache store. */
  def store(value: V)(implicit ctx: Context): Future[Option[V]] =
    withDatabase { db =>
      for {
        old <- db.get[V](value.id.get.toString) recoverWith {
          case _: java.io.InvalidClassException => Future.successful(None) }
        _   <- db.set(value.id.get.toString, value, expiry(value.id))
      } yield old
    }

  /** Update existing value expiry in the cache store. */
  def updateExpiry(key: Id)(implicit ctx: Context): Future[Unit] =
    withDatabase { db =>
      (for {
        Some(v) <- db.get[V](key.get.toString)
        _       <- db.set(key.get.toString, v, expiry(key))
      } yield ()) recoverWith {
        case _: NoSuchElementException
           | _: java.io.InvalidClassException => Future.successful(Unit)
      }
    }

  /** Deletes a key from the cache store. */
  def remove(key: Id)(implicit ctx: Context): Future[Option[V]] =
    withDatabase { db =>
      for {
        old <- db.get[V](key.get.toString) recoverWith {
          case _: java.io.InvalidClassException => Future.successful(None) }
        _   <- db.delete(key.get.toString)
      } yield old
    }
}
