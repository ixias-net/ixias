/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.model

object Tag {

  /**
   * Creates a value of the Id given a value of its representation type.
   */
  @inline def apply[@specialized R, T](r: R): R @@ T =
    r.asInstanceOf[R @@ T]

  /**
   * Returns a value of its representation type.
   */
  def unwrap[R, T](t: R @@ T): R =
    t.asInstanceOf[R]

}

/**
 * Variants of `apply`, `unwrap` that require specifying the Id
 */
final class TagOf[T] {
  def empty[R]:       R @@ T  = Tag.apply(null.asInstanceOf[R])
  def apply[R](r: R): R @@ T  = Tag.apply(r)
  def unwrap[R](t: R @@ T): R = Tag.unwrap(t)
}
object TagOf {
  def apply[T]: TagOf[T] = new TagOf[T]
}
