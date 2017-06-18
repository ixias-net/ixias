/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias

package object model {

  /**
   * Tag a type `T` with `Tag`.
   * The resulting type is used to discriminate between type class instances.
   */
  private[model] type Tagged[A, T] = { type Tag = T; type Self = A }
  type @@[T, Tag] = Tagged[T, Tag]
}
