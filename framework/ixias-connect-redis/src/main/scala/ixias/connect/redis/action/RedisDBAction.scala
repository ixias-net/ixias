/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.connect.redis.action

import scala.util.Failure
import scala.concurrent.Future

import ixias.connect.redis.RedisProfile
import ixias.persistence.model.DataSourceName
import ixias.persistence.action.BasicAction

/**
 * The provider for `RedisDBAction`
 */
trait RedisDBActionProvider { self: RedisProfile =>

  object RedisDBAction extends BasicAction[DataSourceName, Database] {

    /** Invoke DB action block. */
    def apply[A](dsn: DataSourceName)(block: Database => Future[A]): Future[A] =
      invokeBlock(dsn, block)

    /** Run block process */
    def invokeBlock[A](dsn: DataSourceName, block: Database => Future[A]): Future[A] =
      (for {
        con   <- backend.getDatabase(dsn)
        value <- block(con)
      } yield value) andThen {
        case Failure(ex) => logger.error(
          "The redis action failed. dsn=%s".format(dsn.toString), ex)
      }
  }
}
