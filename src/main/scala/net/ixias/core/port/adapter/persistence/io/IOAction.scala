/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.io

import scalaz._
import scalaz.Scalaz._

/**
 * A Persistence I/O Action that can be executed on a database.
 */
trait IOAction {

  /** The type of the context used for running IOActions. */
  type Context >: Null <: IOActionContext

  /** Contains an error messsage when a command fails validation. */
  type Error = String

  /** Used to validate commands received by the system that act on the adapter. */
  type ValidationNel[A] = scalaz.ValidationNel[Error, A]

  /** Construct a success validation value. */
  def success[A](v: A): ValidationNel[A] = v.successNel

  /** Construct a failure validation value. */
  def failed[A](message: String): ValidationNel[A] = message.failureNel
}
