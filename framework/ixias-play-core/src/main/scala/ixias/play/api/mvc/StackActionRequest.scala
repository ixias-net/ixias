/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc.{ Request, WrappedRequest }
import scala.concurrent.Future
import scala.collection.concurrent.TrieMap
import ixias.play.api.mvc.Errors._
import play.api.http.HeaderNames

// Wrap an existing request. Useful to extend a request.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
import StackActionRequest._
case class StackActionRequest[A](
  underlying: Request[A],
  attributes: TrieMap[AttributeKey[_], Any] = TrieMap.empty
) extends WrappedRequest[A](underlying) {

  override lazy val host: String = {
    val AbsoluteUri = """(?is)^(https?)://([^/]+)(/.*|$)""".r
    uri match {
      case AbsoluteUri(proto, hostPort, rest) => hostPort
      case _ => headers.get(X_FORWARDED_HOST).orElse(headers.get(HeaderNames.HOST)).getOrElse("")
    }
  }

  /**
   * Retrieve an attribute by specific key.
   */
  def get[B](key: AttributeKey[B]): Option[B] =
    attributes.get(key).asInstanceOf[Option[B]]

  /**
   * Store an attribute under the specific key.
   */
  def set[B](key: AttributeKey[B], value: B): StackActionRequest[A] = {
    attributes.put(key, value)
    this
  }

  /**
   * Store an attributes
   */
  def ++=[B](tail: TrieMap[AttributeKey[_], Any]): StackActionRequest[A] = {
    attributes ++= tail
    this
  }
}

// The declaration for request's attribute.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object StackActionRequest {

  val X_FORWARDED_HOST  = "X-Forwarded-Host"

  /**
   * The attribute key of request.
   */
  trait AttributeKey[A] {
    def ->(value: A): Attribute[A] = Attribute(this, value)
  }

  /**
   * The attribute of request.
   */
  case class Attribute[A](key: AttributeKey[A], value: A) {
    def toTuple: (AttributeKey[A], A) = (key, value)
  }
}

