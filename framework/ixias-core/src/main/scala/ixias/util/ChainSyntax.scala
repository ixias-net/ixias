/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.util

import scala.language.implicitConversions

/**
 * Provides tap/pipe functions for chaining methods
 * Officially supported since Scala 2.13.
 */
final class ChainSyntaxOps[A](private val self: A) extends AnyVal {

  /**
   * As a side effect, the value is passed to the function,
   * and the original value is returned after the function is executed.
   */
  def tap[U](f: A => U): A = {
    f(self)
    self
  }

  /**
   * Transform a value by applying the function
   */
  def pipe[B](f: A => B): B =
    f(self)
}

trait ChainSyntax {
  implicit final def toChainSyntaxOps[T](v: T): ChainSyntaxOps[T] =
    new ChainSyntaxOps(v)
}
