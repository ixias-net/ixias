/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence

import scala.reflect.ClassTag
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import shade.memcached.{ Memcached, MemcachedCodecs }

import ixias.model.{ @@, Entity, EntityModel }
import ixias.persistence.model.DataSourceName
import ixias.persistence.backend.ShadeBackend
import ixias.persistence.action.ShadeDBActionProvider
import ixias.security.RandomStringToken

/**
 * The profile for persistence with using the Shade library.
 */
private[persistence]
trait ShadeProfile extends Profile with ShadeDBActionProvider {

  // --[ Typedefs ]-------------------------------------------------------------
  /** The type of database objects. */
  type Database = Memcached

  /** The back-end type required by this profile */
  type Backend  = ShadeBackend

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
abstract class ShadeRepository[K <: @@[_, _], M <: EntityModel[K]](implicit ttag: ClassTag[M])
    extends Repository[K, M] with ShadeProfile with MemcachedCodecs {
  import api._

  // --[ Methods ]--------------------------------------------------------------
  val dsn: DataSourceName

  // --[ Methods ]--------------------------------------------------------------
  /** Fetches a value from the cache store. */
  def get(id: Id): Future[Option[EntityEmbeddedId]] = {
    DBAction(dsn) { db =>
      db.get[M](id.toString)
        .map(_.map(Entity.EmbeddedId[K, M]))
    } recoverWith {
      case _: java.io.InvalidClassException => Future.successful(None)
    }
  }

  // --[ Methods ]--------------------------------------------------------------
  /** Sets a (key, value) in the cache store. */
  def add(data: EntityWithNoId): Future[Id] = add(data, Duration.Inf)
  def add(data: EntityWithNoId, expiry: Duration): Future[Id] =
    DBAction(dsn) { db =>
      val id = RandomStringToken.next(32).asInstanceOf[K]
      db.add(id.toString, data.v, expiry)
        .map(_ => id)
    }

  /** Sets a (key, value) in the cache store. */
  def update(data: EntityEmbeddedId): Future[Option[EntityEmbeddedId]] = update(data, Duration.Inf)
  def update(data: EntityEmbeddedId, expiry: Duration): Future[Option[EntityEmbeddedId]] =
    DBAction(dsn) { db =>
      for {
        old <- db.get[M](data.id.toString)
        _   <- db.set(data.id.toString, data.v, expiry)
      } yield old.map(Entity.EmbeddedId[K, M])
    }

  /** Update existing value expiry in the cache store. */
  def updateExpiry(id: Id, expiry: Duration = Duration.Inf): Future[Unit] =
    DBAction(dsn) { db =>
      for {
        Some(v) <- db.get[M](id.toString)
        _       <- db.set(id.toString, v, expiry)
      } yield ()
    } recoverWith {
      case _: NoSuchElementException
         | _: java.io.InvalidClassException => Future.successful(Unit)
    }

  /** Deletes a key from the cache store. */
  def remove(id: Id): Future[Option[EntityEmbeddedId]] =
    DBAction(dsn) { db =>
      for {
        old <- db.get[M](id.toString)
        _   <- db.delete(id.toString)
      } yield old.map(Entity.EmbeddedId[K, M])
    } recoverWith {
      case _: java.io.InvalidClassException => Future.successful(None)
    }
}
