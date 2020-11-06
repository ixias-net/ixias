/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.connect.redis

import ixias.model._
import ixias.persistence.Repository
import ixias.persistence.model.DataSourceName
import ixias.security.RandomStringToken

import scala.concurrent.Future
import play.api.libs.json.{ Json, JsSuccess, JsError }

/**
 * The base repository which is implemented basic feature methods of redis.
 */
trait RedisRepository[K <: @@[_, _], M <: EntityModel[K]]
    extends Repository[K, M] with RedisProfile {
  import api._

  /**
   * Data source name
   */
  val dsn: DataSourceName

  /**
   * Json deserializer
   */
  implicit val jsonReads:  play.api.libs.json.Reads[M]

  /**
   * Json serializer
   */
  implicit val jsonWrites: play.api.libs.json.Writes[M]

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Fetches a value from the cache store.
   */
  def get(id: Id): Future[Option[EntityEmbeddedId]] =
    DBAction(dsn) { db =>
      db.get(id.toString).flatMap({
        case null  => Future.successful(None)
        case value => Json.parse(value).validate[M] match {
          case JsSuccess(data, _) => Future.successful(Some(data.toEmbeddedId))
          case err: JsError       => Future.failed(
            new RuntimeException(s"Errors: ${JsError.toJson(err)}")
          )
        }
      })
    }

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Sets a (key, value) in the cache store.
   */
  def add(data: EntityWithNoId): Future[Id] = {
    val id = RandomStringToken.next(32).asInstanceOf[K]
    DBAction(dsn) { db =>
      val jsval = Json.obj("" -> Json.toJson(data.v), "id" -> id.toString)
      db.set(id.toString, jsval.toString)
        .map(_ => id)
    }
  }

  /**
   * Sets a (key, value) in the cache store.
   */
  def update(data: EntityEmbeddedId): Future[Option[EntityEmbeddedId]] =
    DBAction(dsn) { db =>
      db.getset(data.id.toString, Json.toJson(data.v).toString).flatMap({
        case null  => Future.successful(None)
        case value => Json.parse(value).validate[M] match {
          case JsSuccess(data, _) => Future.successful(Some(data.toEmbeddedId))
          case _: JsError         => Future.successful(None)
        }
      })
    }

  /**
   * Deletes a key from the cache store.
   */
  def remove(id: Id): Future[Option[EntityEmbeddedId]] =
    for {
      old <- get(id)
      _   <- old match {
        case None    => Future.successful(())
        case Some(_) => DBAction(dsn) { db =>
          db.del(id.toString)
        }
      }
    } yield old
}

