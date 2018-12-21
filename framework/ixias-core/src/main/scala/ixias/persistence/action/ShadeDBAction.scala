/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.action

import scala.util.Failure
import scala.concurrent.Future

import ixias.persistence.ShadeProfile
import ixias.persistence.model.DataSourceName

/**
 * The provider for `ShadeDBAction`
 */
trait ShadeDBActionProvider { self: ShadeProfile =>

  object ShadeDBAction extends BasicAction[DataSourceName, Database] {

    /** Invoke DB action block. */
    def apply[A](dsn: DataSourceName)(block: Database => Future[A]): Future[A] =
      invokeBlock(dsn, block)

    /** Run block process */
    def invokeBlock[A](dsn: DataSourceName, block: Database => Future[A]): Future[A] =
      (for {
        db    <- backend.getDatabase(dsn)
        value <- block(db)
      } yield value) andThen {
        case Failure(ex) => logger.error(
          "The database action failed. dsn=%s".format(dsn.toString), ex)
      }
  }
}
