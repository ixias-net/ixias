/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.connect.redis

import scala.util.Failure
import scala.concurrent.{ Future, ExecutionContext }
import scala.language.implicitConversions
import java.util.concurrent.ExecutionException

import ixias.persistence.Profile
import ixias.connect.redis.backend.RedisBackend
import ixias.connect.redis.action.RedisDBActionProvider

import io.lettuce.core.RedisFuture
import io.lettuce.core.api.async.RedisAsyncCommands

/**
 * The profile for persistence with using the redis client library.
 */
trait RedisProfile extends Profile with RedisDBActionProvider {

  // --[ Typedefs ]-------------------------------------------------------------
  /**
   * The type of database objects
   */
  type Database          = RedisAsyncCommands[String, String]

  /**
   * The back-end type required by this profile
   */
  type Backend           = RedisBackend

  // --[ Alias ]----------------------------------------------------------------
  val DataSourceName     = ixias.persistence.model.DataSourceName

  // --[ Properties ]-----------------------------------------------------------
  /**
   * The back-end implementation for this profile
   */
  protected val backend  = new RedisBackend

  /**
   * Database Action Helpers
   */
  protected val DBAction = RedisDBAction

  /**
   * The API for using the utility methods with a single import statement.
   * This provides the repository's implicits, the Database connections,
   * and commonly types and objects.
   */
  trait API extends super.API {

    /**
     * Transform `RedisFuture` to Scala Future
     */
    implicit def toScalaFuture[T](redisF: RedisFuture[T])(implicit ex: ExecutionContext): Future[T] =
      Future(redisF.get()).transform {
        case Failure(ex: ExecutionException) => Failure(ex.getCause)
        case v => v
      }
  }

  val api: API = new API {}
}

