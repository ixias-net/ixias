/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias

package object model {

  /**
   * Tagged with `R` as representation type and added a tag.
   *
   * Values of the Id will not add any additional boxing beyond what's required for
   * values of the representation type to conform to Any. In practice this means that value
   * types will receive their standard Scala AnyVal boxing and reference types will be unboxed.
   */
  type     @@[R, T] = Tagged[R, T]
  type Tagged[R, T] = { type Self = R; type Tag = T }

  /** The current time */
  def NOW = java.time.LocalDateTime.now()
}
