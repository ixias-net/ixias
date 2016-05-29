/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.play.api.auth.mvc

import play.api.mvc._
import play.api.libs.concurrent.Execution

import scala.util.{ Success, Failure }
import scala.util.control.{ NonFatal, ControlThrowable }
import scala.concurrent.{ Future, ExecutionContext }
import scala.collection.concurrent.TrieMap

/**
 * Custom action builders
 */
trait StackActionBuilder extends ActionBuilder[StackRequest] {
  import StackRequest._

  // --[ Typedefs ] ------------------------------------------------------------
  /**
   * The attribute key for execution context.
   */
  object ExecutionContextKey extends AttributeKey[ExecutionContext]

  // --[ methods ] -------------------------------------------------------------
  /**
   * Invoke the block.
   * This is the main method that an ActionBuilder has to implement.
   */
  final def invokeBlock[A](request: StackRequest[A], block: StackRequest[A] => Future[Result]): Future[Result] = {
    try {
      implicit val ctx = getExecutionContext(request)
      proceed(request)(block) andThen {
        case Success(v)  => cleanupOnSuccess(request, Some(v))
        case Failure(ex) => cleanupOnFailure(request, ex)
      }
    } catch {
      case ex: ControlThrowable => cleanupOnSuccess(request, None); throw ex
      case NonFatal(ex)         => cleanupOnFailure(request, ex);    throw ex
    }
  }

  /**
   * Proceed with the next advice or target method invocation
   */
  def proceed[A](request: StackRequest[A])(block: StackRequest[A] => Future[Result]): Future[Result]

  // --[ methods ] -------------------------------------------------------------
  /**
   * This method will be called bu StackAction when invokeBlock succeed.
   */
  def cleanupOnSuccess[A](request: StackRequest[A], result: Option[Result]): Unit = ()

  /**
   * This method will be called bu StackAction when invokeBlock failed.
   */
  def cleanupOnFailure[A](request: StackRequest[A], e: Throwable): Unit = ()

  /**
   * An ExecutionContext that executes work on the given ExecutionContext.
   */
  protected def getExecutionContext(implicit request: StackRequest[_]): ExecutionContext =
    request.get(ExecutionContextKey).getOrElse(Execution.defaultContext)
}
