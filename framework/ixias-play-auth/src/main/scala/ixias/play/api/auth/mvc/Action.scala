/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

import play.api.mvc._
import play.api.libs.concurrent.Execution

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ Future, ExecutionContext }
import scala.util.{ Success, Failure }
import scala.util.control.{ NonFatal, ControlThrowable }

// Statck Action
//~~~~~~~~~~~~~~~~
abstract class StackAction(val params: Attribute[_]*) extends ActionBuilder[ActionRequest]
{
  /**
   * Invoke the block.
   * This is the main method that an ActionBuilder has to implement.
   */
  final def invokeBlock[A](request: Request[A], block: ActionRequest[A] => Future[Result]): Future[Result] = {
    val attr      = new TrieMap[AttributeKey[_], Any] ++= params.map(_.toTuple)
    val actionReq = ActionRequest(request, attr)
    try {
      implicit val ctx = getExecutionContext(actionReq)
      proceed(actionReq)(block) andThen {
        case Success(p) => cleanupOnSuccess(actionReq, Some(p))
        case Failure(e) => cleanupOnFailure(actionReq, e)
      }
    } catch {
      case e: ControlThrowable => cleanupOnSuccess(actionReq, None); throw e
      case NonFatal(e)         => cleanupOnFailure(actionReq, e);    throw e
    }
  }

  /** Proceed with the next advice or target method invocation */
  def proceed[A](req: ActionRequest[A])(f: ActionRequest[A] => Future[Result]): Future[Result] = f(req)

  // --[ Callback methods ] ----------------------------------------------------
  /** This method will be called bu StackAction when invokeBlock succeed. */
  def cleanupOnSuccess[A](request: ActionRequest[A]): Unit = ()
  def cleanupOnSuccess[A](request: ActionRequest[A], result: Option[Result]): Unit = cleanupOnSuccess(request)

  /** This method will be called bu StackAction when invokeBlock failed. */
  def cleanupOnFailure[A](request: ActionRequest[A], e: Throwable): Unit = ()

  // --[ ExecutionContext ] ----------------------------------------------------
  protected object ExecutionContextKey extends AttributeKey[ExecutionContext]
  protected def getExecutionContext(implicit req: ActionRequest[_]): ExecutionContext =
    req.get(ExecutionContextKey).getOrElse(Execution.defaultContext)
}

// Statck Action Builder
//~~~~~~~~~~~~~~~~~~~~~~~
trait StackActionBuilder[T <: StackAction] {

  /**
   * Build a custom action object.
   */
  def build(params: Attribute[_]*): T

  /**
   * Constructs an `Action` with default content, and no request parameter.
   */
  final def apply(block: ActionRequest[AnyContent] => Result):
      Action[AnyContent] = build()(block)

  /**
   * Constructs an `Action` with default content.
   */
  final def apply(params: Attribute[_]*)
    (block: ActionRequest[AnyContent] => Result):
      Action[AnyContent] = build(params: _*)(block)

  /**
   * Constructs an `Action` with default content.
   */
  final def apply[A](p: BodyParser[A], params: Attribute[_]*)
    (block: ActionRequest[A] => Result):
      Action[A] = build(params: _*)(p)(block)

  /**
   * Constructs an `Action` that returns a future of a result,
   * with default content, and no request parameter.
   */
  final def async(block: ActionRequest[AnyContent] => Future[Result]):
      Action[AnyContent] = build().async(block)

  /**
   * Constructs an `Action` that returns a future of a result,
   * with default content.
   */
  final def async(params: Attribute[_]*)
    (block: ActionRequest[AnyContent] => Future[Result]):
      Action[AnyContent] = build(params: _*).async(block)

  /**
   * Constructs an `Action` that returns a future of a result,
   * with default content.
   */
  final def async[A](p: BodyParser[A], params: Attribute[_]*)
    (block: ActionRequest[A] => Future[Result]):
      Action[A] = build(params: _*).async(p)(block)
}

// Statck Action Builder for Authenticate
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
trait StackAuthActionBuilder[T <: StackAction] {

  /**
   * Build a custom action object.
   */
  def build(params: Attribute[_]*)(implicit auth: AuthProfile): T

  /**
   * Constructs an `Action` with default content, and no request parameter.
   */
  final def apply(block: ActionRequest[AnyContent] => Result)(implicit auth: AuthProfile):
      Action[AnyContent] = build()(auth)(block)

  /**
   * Constructs an `Action` with default content.
   */
  final def apply(params: Attribute[_]*)
    (block: ActionRequest[AnyContent] => Result)(implicit auth: AuthProfile):
      Action[AnyContent] = build(params: _*)(auth)(block)

  /**
   * Constructs an `Action` with default content.
   */
  final def apply[A](p: BodyParser[A], params: Attribute[_]*)
    (block: ActionRequest[A] => Result)(implicit auth: AuthProfile):
      Action[A] = build(params: _*)(auth)(p)(block)

  /**
   * Constructs an `Action` that returns a future of a result,
   * with default content, and no request parameter.
   */
  final def async(block: ActionRequest[AnyContent] => Future[Result])(implicit auth: AuthProfile):
      Action[AnyContent] = build()(auth).async(block)

  /**
   * Constructs an `Action` that returns a future of a result,
   * with default content.
   */
  final def async(params: Attribute[_]*)
    (block: ActionRequest[AnyContent] => Future[Result])(implicit auth: AuthProfile):
      Action[AnyContent] = build(params: _*)(auth).async(block)

  /**
   * Constructs an `Action` that returns a future of a result,
   * with default content.
   */
  final def async[A](p: BodyParser[A], params: Attribute[_]*)
    (block: ActionRequest[A] => Future[Result])(implicit auth: AuthProfile):
      Action[A] = build(params: _*)(auth).async(p)(block)
}
