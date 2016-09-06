/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.model

/**
 * Adatabase cursor is a control structure that enables
 * traversal over the records in a database.
 */
case class Cursor(
  var offset: Long         = 0L,        // Select all elements except the first ones.
  var limit:  Option[Long] = Some(10L)  // Select the first elements.
) extends CursorLike

/**
 * Companion object
 */
object Cursor {

  /** Create a new cursor object with a limit parameter */
  def apply(n: Long) = new Cursor(limit = Some(n))
}

/**
 * The cursor's future implementation.
 */
trait CursorLike { self: Cursor =>

  /** Update position by specified value. */
  def set(pos: Long): Cursor = {
    offset = pos
    this
  }

  /** Sets position of the next element. */
  def next: Cursor = {
    limit.map(offset += _)
    this
  }

  /** Rewind the position. */
  def rewind: Cursor = {
    offset = 0
    this
  }
}


