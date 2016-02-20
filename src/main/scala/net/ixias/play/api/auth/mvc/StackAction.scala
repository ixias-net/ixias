/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.auth.mvc

import _root_.play.api.mvc._
import _root_.play.api.libs.concurrent.Execution

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ Future, ExecutionContext }
import scala.util.{ Success, Failure }
import scala.util.control.{ NonFatal, ControlThrowable }

/** Wrap an existing request. Useful to extend a request. */
case class StackRequest[A](
  underlying: Request[A],
  attributes: TrieMap[StackRequest.AttributeKey[_], Any]
) extends WrappedRequest[A](underlying) {

  /** Retrieve an attribute by specific key. */
  def get[B](key: StackRequest.AttributeKey[B]): Option[B] =
    attributes.get(key).asInstanceOf[Option[B]]

  /** Store an attribute under the specific key. */
  def set[B](key: StackRequest.AttributeKey[B], value: B): StackRequest[A] = {
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
import StackRequest._

/** Custom action builders */
class StackActionBuilder(val params: StackRequest.Attribute[_]*) extends ActionBuilder[StackRequest] {
  import StackRequest._

  /** Invoke the block. */
  def invokeBlock[A](request: Request[A], block: StackRequest[A] => Future[Result]): Future[Result] = {
    val attributes = new TrieMap[AttributeKey[_], Any] ++= params.map(_.toTuple)
    val requestExt = StackRequest(request, attributes)
    try {
      implicit val ctx = createStackActionExecutionContext(requestExt)
      proceed(requestExt)(block) andThen {
        case Success(p) => cleanupOnSuccess(requestExt, Some(p))
        case Failure(e) => cleanupOnFailure(requestExt, e)
      }
    } catch {
      case e: ControlThrowable => cleanupOnSuccess(requestExt, None); throw e
      case NonFatal(e)         => cleanupOnFailure(requestExt, e);    throw e
    }
  }

  /** Proceed with the next advice or target method invocation */
  def proceed[A](req: StackRequest[A])(f: StackRequest[A] => Future[Result]): Future[Result] = f(req)

  // --[ Callback methods ] ----------------------------------------------------
  /** This method will be called bu StackAction when invokeBlock succeed. */
  def cleanupOnSuccess[A](request: StackRequest[A]): Unit = ()
  def cleanupOnSuccess[A](request: StackRequest[A], result: Option[Result]): Unit = cleanupOnSuccess(request)

  /** This method will be called bu StackAction when invokeBlock failed. */
  def cleanupOnFailure[A](request: StackRequest[A], e: Throwable): Unit = ()

  // --[ ExecutionContext ] ----------------------------------------------------
  protected object ExecutionContextKey extends AttributeKey[ExecutionContext]
  protected def createStackActionExecutionContext(implicit req: StackRequest[_]): ExecutionContext =
    req.get(ExecutionContextKey).getOrElse(Execution.defaultContext)
}

/** The custom playframework action. */
object StackAction {

  type ActionBuilder         = StackActionBuilder
  type BlockFunction[A]      = StackRequest[A] => Result
  type AsyncBlockFunction[A] = StackRequest[A] => Future[Result]

  /** Constructs an `Action` with default content, and no request parameter. */
  final def apply(block: BlockFunction[AnyContent]): Action[AnyContent] =
    new ActionBuilder()(block)

  /** Constructs an `Action` with default content. */
  final def apply(params: Attribute[_]*)(block: BlockFunction[AnyContent]): Action[AnyContent] =
    new ActionBuilder(params: _*)(block)

  /** Constructs an `Action` with default content. */
  final def apply[A](p: BodyParser[A], params: Attribute[_]*)(block: BlockFunction[A]): Action[A] =
    new ActionBuilder(params: _*)(p)(block)

  /** Constructs an `Action` that returns a future of a result,
    * with default content, and no request parameter. */
  final def async(block: AsyncBlockFunction[AnyContent]): Action[AnyContent] =
    new ActionBuilder().async(block)

  /** Constructs an `Action` that returns a future of a result,
    * with default content. */
  final def async(params: Attribute[_]*)(block: AsyncBlockFunction[AnyContent]): Action[AnyContent] =
    new ActionBuilder(params: _*).async(block)

  /** Constructs an `Action` that returns a future of a result,
    * with default content. */
  final def async[A](p: BodyParser[A], params: Attribute[_]*)(block: AsyncBlockFunction[A]): Action[A] =
    new ActionBuilder(params: _*).async(p)(block)
}
