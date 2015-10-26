/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.controllers

import _root_.play.api.mvc.{ Request, WrappedRequest }
import scala.collection.concurrent.TrieMap

/* Wrap an existing request. Useful to extend a request. */
case class StackableRequest[A](
  underlying: Request[A],
  attributes: TrieMap[StackableRequest.AttributeKey[_], Any]
) extends WrappedRequest[A](underlying) {
  def get[B](key: StackableRequest.AttributeKey[B]): Option[B] = attributes.get(key).asInstanceOf[Option[B]]
  def set[B](key: StackableRequest.AttributeKey[B], value: B): StackableRequest[A] = {
    attributes.put(key, value)
    this
  }
}

// Companion object
//~~~~~~~~~~~~~~~~~~
object StackableRequest {
  trait AttributeKey[A] {
    def ->(value: A): Attribute[A] = Attribute(this, value)
  }
  case class Attribute[A](key: AttributeKey[A], value: A) {
    def toTuple: (AttributeKey[A], A) = (key, value)
  }
}

