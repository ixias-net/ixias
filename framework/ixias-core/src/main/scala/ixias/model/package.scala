/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias

package object model {

  /** Tagged with `U` as representation type and added a tag. */
  object tag {
    def apply[U] = new shapeless.tag.Tagger[U]
  }
  type @@[+T, U] = shapeless.tag.@@[T, U]

  /**
   * Used as a term `the[T]` yields the unique implicit value of type `T` in the current
   * implicit scope, if any. It is a compile time error if there is no such value. Its
   * primary advantage over `Predef.implicitly` is that it will preserve any refinement that
   * the implicit definition has, resulting in more precisely typed, and hence often more
   * useful, values,
   */
  val the = shapeless.the

  /** The current time */
  def NOW = java.time.LocalDateTime.now()
}
