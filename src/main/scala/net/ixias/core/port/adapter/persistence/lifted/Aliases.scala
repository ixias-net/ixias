/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.lifted

/** Aliases for lifted embedding features. This trait can be mixed into aliasing
  * objects which simplify the use of the lifted embedding. */
trait Aliases {
  type SomeId[+A] = core.domain.model.SomeId[A]
  val  SomeId     = core.domain.model.SomeId
  val  NoneId     = core.domain.model.NoneId
  val  Identity   = core.domain.model.Identity
}
