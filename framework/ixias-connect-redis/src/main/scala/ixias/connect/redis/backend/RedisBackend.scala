/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.connect.redis.backend

import scala.concurrent.Future
import ixias.persistence.model.DataSourceName
import ixias.persistence.backend.BasicBackend
import io.lettuce.core.{ RedisClient, RedisURI }
import io.lettuce.core.api.async.RedisAsyncCommands

/**
 * The backend to get a client for Redis
 */
class RedisBackend extends BasicBackend[RedisAsyncCommands[String, String]] with RedisConfig {

  /**
   * Get a client to manage Redis
   */
  def getDatabase(implicit dsn: DataSourceName): Future[RedisAsyncCommands[String, String]] =
    Future.fromTry {
      for {
        dbName <- getDatabaseName
        url    <- getHosts.flatMap(_.headOption match {
          case Some(host) => scala.util.Success {
            val toks    = host.split(",")
            val builder = RedisURI.Builder.redis(
              toks.lift(0).getOrElse("localhost"),
              toks.lift(1).map(_.toInt).getOrElse(RedisURI.DEFAULT_REDIS_PORT)
            ).withDatabase(dbName.toInt)
            getPassword.map(builder.withPassword(_: CharSequence))
            builder.build
          }
          case None => scala.util.Failure {
            new NoSuchElementException("Not found host setting")
          }
        })
      } yield {
        RedisClient.create(url).connect.async
      }
    }
}
