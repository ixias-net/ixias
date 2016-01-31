/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import scala.util.Try
import scala.util.control.NonFatal
import scala.reflect.ClassTag
import scala.concurrent.duration.Duration

import shade.memcached.MemcachedCodecs
import core.domain.model.Entity

/**
 * The base repository which is implemented basic feature methods.
 */
abstract class ShadeBasicRepository[K, V <: Entity[K]]
  (implicit ttag: ClassTag[V]) extends ShadeRepository[K, V] with MemcachedCodecs {

  /** Gets a cache client resource. */
  def withDatabase[T](f: Database => T)(implicit ctx: Context): Try[T]

  /** Gets exprity time. */
  def exprityTime(key: Id): Duration = Duration.Inf

  /** Fetches a value from the cache store. */
  def get(key: Id)(implicit ctx: Context): Try[Option[V]] =
    withDatabase { db =>
      try db.awaitGet[V](key.get.toString)
      catch {
        case _: java.io.InvalidClassException => None
      }
    }

  /** Adds a value for a given key, if the key doesn't already exist in the cache store. */
  def add(value: V)(implicit ctx: Context): Try[Id] =
    withDatabase { db =>
      db.awaitAdd(value.id.get.toString, value, exprityTime(value.id))
      value.id
    }

  /** Sets a (key, value) in the cache store. */
  def update(value: V)(implicit ctx: Context): Try[Unit] =
    withDatabase { db =>
      db.awaitSet(value.id.get.toString, value, exprityTime(value.id))
    }

  /** Update existing value exprity in the cache store. */
  def exprity(key: Id)(implicit ctx: Context): Try[Unit] =
    withDatabase { db =>
      db.awaitGet[V](key.get.toString).map { value =>
        db.awaitSet(key.get.toString, value, exprityTime(key))
      }
    }

  /** Deletes a key from the cache store. */
  def remove(key: Id)(implicit ctx: Context): Try[Option[V]] =
    withDatabase { db =>
      try db.awaitGet[V](key.get.toString)
      catch {
        case _: java.io.InvalidClassException => None
      }
      finally {
        db.awaitDelete(key.get.toString)
      }
    }
}
