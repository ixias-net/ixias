/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.dbio

import scala.util.{ Try, Success, Failure }

/**
 * A Persistence I/O Action that can be executed on a database.
 */
trait IOAction {

  /** Construct a success validation value. */
  def success[T](value: T): Try[T] = Success(value)

  /** Construct a failure validation value. */
  def failed[T](exception: Throwable): Try[T] = Failure(exception)
}
