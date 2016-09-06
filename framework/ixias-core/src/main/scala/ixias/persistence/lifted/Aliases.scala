/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

/**
 * Aliases for lifted embedding features. This trait can be mixed into aliasing
 * objects which simplify the use of the lifted embedding.
 */
trait Aliases
{
  type SomeId[+A] = ixias.model.SomeId[A]
  type Cursor     = ixias.persistence.model.Cursor

  val  SomeId     = ixias.model.SomeId
  val  NoneId     = ixias.model.NoneId
  val  Identity   = ixias.model.Identity
  val  Cursor     = ixias.persistence.model.Cursor
}
