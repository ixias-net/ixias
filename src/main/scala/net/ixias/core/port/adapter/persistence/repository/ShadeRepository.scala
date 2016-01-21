/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import scala.util.Try
import scala.reflect.ClassTag
import scala.concurrent.duration.Duration
import shade.memcached.MemcachedCodecs
import com.typesafe.config.Config
import core.domain.model.{ Identity, Entity }
import core.port.adapter.persistence.lifted._
import core.port.adapter.persistence.backend.ShadeBackend
import core.port.adapter.persistence.io.EntityIOActionContext

/**
 * The base repository for persistence with using the Shade library.
 */
trait ShadeRepository[K, V <: Entity[K]] extends Repository[K, V] with ShadeProfile

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
      db.awaitGet[V](key.get.toString)
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
      db.awaitGet[V](key.get.toString).map { value =>
        db.awaitDelete(key.get.toString)
        value
      }
    }
}

/**
 * The profile for persistence with using the Shade library.
 */
trait ShadeProfile extends Profile with ShadeActionComponent { self =>

  type This >: this.type <: ShadeProfile
  /** The back-end type required by this profile */
  type Backend  = ShadeBackend
  /** The type of database objects. */
  type Database = Backend#Database
  /** The type of the context used for running repository Actions */
  type Context =  EntityIOActionContext

  /** The back-end implementation for this profile */
  val backend = new ShadeBackend {}

  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  trait API extends super.API
  val api: API = new API {}

  /** Run the supplied function with a default action context. */
  def withActionContext[T](f: Context => T): Try[T] =
    Try { f(createPersistenceActionContext()) }

  /** Run the supplied function with a database object by using pool database session. */
  def withDatabase[T](dsn:String)(f: Database => T)(implicit ctx: Context): Try[T] =
    Try { f(backend.getDatabase(dsn)) }
}

trait ShadeActionComponent extends ActionComponent { profile: ShadeProfile =>
  /** Create the default IOActionContext for this repository. */
  def createPersistenceActionContext(cfg: Config): Context =
     EntityIOActionContext(config = cfg)
}

