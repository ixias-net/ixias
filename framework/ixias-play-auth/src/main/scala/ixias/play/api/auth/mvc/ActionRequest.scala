/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

import scala.collection.concurrent.TrieMap
import play.api.mvc.{ Request, WrappedRequest }

/**
 * Wrap an existing request. Useful to extend a request.
 */
case class ActionRequest[A](
  underlying: Request[A],
  attributes: TrieMap[ActionRequest.AttributeKey[_], Any]
) extends WrappedRequest[A](underlying) {
  import ActionRequest.AttributeKey

  /**
   * Retrieve an attribute by specific key.
   */
  def get[B](key: AttributeKey[B]): Option[B] =
    attributes.get(key).asInstanceOf[Option[B]]

  /**
   * Store an attribute under the specific key.
   */
  def set[B](key: AttributeKey[B], value: B): ActionRequest[A] = {
    attributes.put(key, value)
    this
  }
}

object ActionRequest {

  /**
   * The attribute of request.
   */
  case class Attribute[A](key: AttributeKey[A], value: A) {
    def toTuple: (AttributeKey[A], A) = (key, value)
  }

  /**
   * The attribute key of request.
   */
  trait AttributeKey[A] {
    def ->(value: A): Attribute[A] = Attribute(this, value)
  }
}
