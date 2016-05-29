
/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.play.api.auth.mvc

import scala.collection.concurrent.TrieMap
import play.api.mvc.{ Request, WrappedRequest }
import StackRequest._

/** Wrap an existing request. Useful to extend a request. */
case class StackRequest[A](
  underlying: Request[A],
  attributes: TrieMap[AttributeKey[_], Any]
) extends WrappedRequest[A](underlying) {

  /** Retrieve an attribute by specific key. */
  def get[B](key: AttributeKey[B]): Option[B] =
    attributes.get(key).asInstanceOf[Option[B]]

  /** Store an attribute under the specific key. */
  def set[B](key: AttributeKey[B], value: B): StackRequest[A] = {
    attributes.put(key, value)
    this
  }
}

/** Declare attribute key and value pair of StackRequest. */
object StackRequest {
  /** The attribute of request. */
  case class Attribute[A](key: AttributeKey[A], value: A) {
    def toTuple: (AttributeKey[A], A) = (key, value)
  }
  /** The attribute key of request. */
  trait AttributeKey[A] {
    def ->(value: A): Attribute[A] = Attribute(this, value)
  }
}
