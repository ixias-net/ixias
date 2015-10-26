/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.controllers

import _root_.play.api.mvc._
import scala.collection.concurrent.TrieMap
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}
import scala.util.control.{NonFatal, ControlThrowable}

trait StackableController { self: Controller =>
  import StackableRequest._

  /** Proceed with the next advice or target method invocation */
  def proceed[A](request: StackableRequest[A])(f: StackableRequest[A] => Future[Result]): Future[Result] = f(request)

  /** Custom action builders */
  sealed case class StackAction(params: Attribute[_]*) extends ActionBuilder[StackableRequest] {
    def invokeBlock[A](request: Request[A], block: (StackableRequest[A]) => Future[Result]): Future[Result] = {
      val requestExt = new StackableRequest(request, new TrieMap[StackableRequest.AttributeKey[_], Any] ++= params.map(_.toTuple))
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

  // --[ ExecutionContext ] ----------------------------------------------------
  protected object ExecutionContextKey extends StackableRequest.AttributeKey[ExecutionContext]
  protected def createStackActionExecutionContext(implicit request: StackableRequest[_]): ExecutionContext =
    request.get(ExecutionContextKey).getOrElse(
      _root_.play.api.libs.concurrent.Execution.defaultContext)

  // --[ Constructs an Action ] ------------------------------------------------
  /** Constructs an `Action` with default content, and no request parameter. */
  final def ApplyStack
    (f: StackableRequest[AnyContent] => Result): Action[AnyContent] =
    StackAction().apply(f)

  /** Constructs an `Action` with default content. */
  final def ApplyStack(params: Attribute[_]*)
    (f: StackableRequest[AnyContent] => Result): Action[AnyContent] =
    StackAction(params: _*).apply(f)

  /** Constructs an `Action` with default content. */
  final def ApplyStack[A](p: BodyParser[A], params: Attribute[_]*)
    (f: StackableRequest[A] => Result): Action[A] =
    StackAction(params: _*).apply(p)(f)

  // --[ Constructs an Action with Asyncronus ] --------------------------------
  /** Constructs an `Action` that returns a future of a result, with default content, and no request parameter. */
  final def AsyncStack
    (f: StackableRequest[AnyContent] => Future[Result]): Action[AnyContent] =
    StackAction().async(f)

  /** Constructs an `Action` that returns a future of a result, with default content. */
  final def AsyncStack(params: Attribute[_]*)
    (f: StackableRequest[AnyContent] => Future[Result]): Action[AnyContent] =
    StackAction(params: _*).async(f)

  /** Constructs an `Action` that returns a future of a result, with default content. */
  final def AsyncStack[A](p: BodyParser[A], params: Attribute[_]*)
    (f: StackableRequest[A] => Future[Result]): Action[A] =
    StackAction(params: _*).async(p)(f)

  // --[ Callback methods ] ----------------------------------------------------
  /** This method will be called bu StackAction when invokeBlock succeed. */
  def cleanupOnSuccess[A](request: StackableRequest[A]): Unit = ()
  def cleanupOnSuccess[A](request: StackableRequest[A], result: Option[Result]): Unit = cleanupOnSuccess(request)

  /** This method will be called bu StackAction when invokeBlock failed. */
  def cleanupOnFailure[A](request: StackableRequest[A], e: Throwable): Unit = ()
}
