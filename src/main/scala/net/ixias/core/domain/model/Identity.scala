/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.core.domain.model

trait Identity[+T] extends Serializable {

  /** The value of identity */
  val value: T

  override def hashCode: Int = 31 * this.value.##
  override def equals(that: Any) = that match {
    case that: Identity[_] => this.value == that.value
    case _ => false
  }
  override def toString = s"""Identity(${value})"""
}
