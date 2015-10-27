/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.controllers

import _root_.play.api.mvc._
import _root_.play.api.libs.concurrent.Execution

import scala.collection.concurrent.TrieMap
import scala.util.{ Failure, Success }
import scala.util.control.{ NonFatal, ControlThrowable }
import scala.concurrent.{ ExecutionContext, Future }

trait Stack {

  // --[ Request ]--------------------------------------------------------------
  /** The attribute key of request. */
  trait AttributeKey[A] {
    def ->(value: A): Attribute[A] = Attribute(this, value)
  }

  /** The attribute of request. */
  case class Attribute[A](key: AttributeKey[A], value: A) {
    def toTuple: (AttributeKey[A], A) = (key, value)
  }

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

  // --[ Action ]---------------------------------------------------------------
  /** Custom action builders */
  sealed case class StackActionBuilder(params: Attribute[_]*) extends ActionBuilder[StackRequest] {
    def invokeBlock[A](request: Request[A], block: (StackRequest[A]) => Future[Result]): Future[Result] = {
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
  }

  /** Proceed with the next advice or target method invocation */
  def proceed[A](request: StackRequest[A])
    (f: StackRequest[A] => Future[Result]): Future[Result] = f(request)

  // --[ Constructs an Action ] ------------------------------------------------
  /** Constructs an `Action` with default content, and no request parameter. */
  final def StackAction
    (f: StackRequest[AnyContent] => Result): Action[AnyContent] =
    StackActionBuilder().apply(f)

  /** Constructs an `Action` with default content. */
  final def StackAction(params: Attribute[_]*)
    (f: StackRequest[AnyContent] => Result): Action[AnyContent] =
    StackActionBuilder(params: _*).apply(f)

  /** Constructs an `Action` with default content. */
  final def StackAction[A](p: BodyParser[A], params: Attribute[_]*)
    (f: StackRequest[A] => Result): Action[A] =
    StackActionBuilder(params: _*).apply(p)(f)

  // --[ Constructs an Action with Asyncronus ] --------------------------------
  /** Constructs an `Action` that returns a future of a result,
    * with default content, and no request parameter. */
  final def AsyncStackAction
    (f: StackRequest[AnyContent] => Future[Result]): Action[AnyContent] =
    StackActionBuilder().async(f)

  /** Constructs an `Action` that returns a future of a result, with default content. */
  final def AsyncStackAction(params: Attribute[_]*)
    (f: StackRequest[AnyContent] => Future[Result]): Action[AnyContent] =
    StackActionBuilder(params: _*).async(f)

  /** Constructs an `Action` that returns a future of a result, with default content. */
  final def AsyncStackAction[A](p: BodyParser[A], params: Attribute[_]*)
    (f: StackRequest[A] => Future[Result]): Action[A] =
    StackActionBuilder(params: _*).async(p)(f)

  // --[ Callback methods ] ----------------------------------------------------
  /** This method will be called bu StackAction when invokeBlock succeed. */
  def cleanupOnSuccess[A](request: StackRequest[A]): Unit = ()
  def cleanupOnSuccess[A](request: StackRequest[A], result: Option[Result]): Unit =
    cleanupOnSuccess(request)

  /** This method will be called bu StackAction when invokeBlock failed. */
  def cleanupOnFailure[A](request: StackRequest[A], e: Throwable): Unit = ()

  // --[ ExecutionContext ] ----------------------------------------------------
  protected object ExecutionContextKey extends AttributeKey[ExecutionContext]
  protected def createStackActionExecutionContext(implicit request: StackRequest[_]): ExecutionContext =
    request.get(ExecutionContextKey).getOrElse(Execution.defaultContext)
}
