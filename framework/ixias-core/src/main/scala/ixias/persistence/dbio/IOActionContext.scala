/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.dbio

/**
 * The base trait for the context object passed topersistent actions
 * by the execution engine.
 */
trait IOActionContext {

  private[this] var stickiness = 0

  /**
   * Pin the context. Multiple calls to `pin` may be nested.
   * The same number of calls to `unpin` is required in order to mark context.
   */
  final def pin(): Unit = stickiness += 1

  /**
   * Unpin the context.
   */
  final def unpin(): Unit = stickiness -= 1

  /**
   * Check if the context is pinned.
   */
  final def isPinned: Boolean = stickiness > 0
}
