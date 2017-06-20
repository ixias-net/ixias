/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.model

object id {

  /** The status of id value */
  trait  Status
  object Status {
    trait NoId  extends Status
    trait HasId extends Status
  }

  /**
   * Creates a value of the Id given a value of its representation type.
   */
  def apply[R, T](r: R): IdType2[R, T, Status.HasId] =
    r.asInstanceOf[Any with IdType2[R, T, Status.HasId]]

  /**
   * Creates a empty value of the Id given a value of its representation type.
   */
  def empty[R, T]: IdType2[R, T, Status.NoId] =
    null.asInstanceOf[Any with IdType2[R, T, Status.NoId]]

  /**
   * Returns a value of its representation type.
   */
  def unwrap[R, T](t: IdType2[R, T, _]): R =
    t.asInstanceOf[R]

  /**
   * IdType with `R` as representation type and added a tag.
   *
   * Values of the Id will not add any additional boxing beyond what's required for
   * values of the representation type to conform to Any. In practice this means that value
   * types will receive their standard Scala AnyVal boxing and reference types will be unboxed.
   */
  sealed trait IdTypeTag[R, T]
  type IdType[R, T]               = { type Tag = IdTypeTag[R, T]; type State <: Status }
  type IdType2[R, T, S <: Status] = { type Tag = IdTypeTag[R, T]; type State = S       }
  type @@[T, Tag] = IdType[T, Tag]

  /**
   * Variants of `apply`, `unwrap` that require specifying the Id
   */
  def of[R, T]: IdTypeOf[R, T] = new IdTypeOf[R, T]
  final class  IdTypeOf[R, T] {
    def  empty:       IdType2[R, T, Status.NoId]  = id.empty
    def  apply(r: R): IdType2[R, T, Status.HasId] = id.apply(r)
    def unwrap(t: IdType2[R, T, Status.HasId]): R = id.unwrap(t)
  }
}
