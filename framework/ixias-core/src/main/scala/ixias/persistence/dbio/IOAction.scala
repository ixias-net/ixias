/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.dbio

import scala.concurrent.Future

/**
 * A Persistence I/O Action that can be executed on a database.
 */
trait IOAction {

  /** Construct a success validation value. */
  protected def successful[T](value: T): Future[T] =
    Future.successful(value)

  /** Construct a failure validation value. */
  protected def failed[T](exception: Throwable): Future[T] =
    Future.failed(exception)
}
