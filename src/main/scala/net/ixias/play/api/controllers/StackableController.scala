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

  case class StackAction(params: Attribute[_]*) extends ActionBuilder[StackableRequest] {
    def invokeBlock[A](req: Request[A], block: (StackableRequest[A]) => Future[Result]): Future[Result] = ???
  }

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
  def cleanupOnFailure[A](request: StackableRequest[A]): Unit = ()
}
