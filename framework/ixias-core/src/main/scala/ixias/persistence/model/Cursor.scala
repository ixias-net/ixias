/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.model

/**
 * A database cursor is a control structure that enables
 * traversal over the records in a database.
 */
case class Cursor(
  val offset: Long         = 0L,        // Select all elements except the first ones.
  val limit:  Option[Long] = Some(10L)  // Select the first elements.
) extends CursorLike

/**
 * Companion object
 */
object Cursor {

  /** Create a new cursor object with a limit parameter */
  def apply(n: Long)         = new Cursor(limit = Some(n))
  def apply(n: Option[Long]) = new Cursor(limit = n)
}

/**
 * The cursor's future implementation.
 */
trait CursorLike { self: Cursor =>

  /** Updates position by specified value. */
  def set(pos: Long): Cursor = {
    this.copy(offset = pos)
  }

  /** Sets position of the next element. */
  def next: Cursor = {
    limit.fold(this)(v => this.copy(offset = offset + v))
  }

  /** Rewind the position. */
  def rewind: Cursor = {
    this.copy(offset = 0)
  }
}


