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


// Action builder
//~~~~~~~~~~~~~~~~
class StackActionBuilder(val params: Attribute[_]*)
    extends ActionBuilder[ActionRequest]
{
  def invokeBlock[A](request: Request[A], block: ActionRequest[A] => Future[Result]): Future[Result] = {
    val attributes = new TrieMap[AttributeKey[_], Any] ++= params.map(_.toTuple)
    val requestExt = ActionRequest(request, attributes)
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
  def proceed[A](req: ActionRequest[A])(f: ActionRequest[A] => Future[Result]): Future[Result] = f(req)

  // --[ Callback methods ] ----------------------------------------------------
  /** This method will be called bu StackAction when invokeBlock succeed. */
  def cleanupOnSuccess[A](request: ActionRequest[A]): Unit = ()
  def cleanupOnSuccess[A](request: ActionRequest[A], result: Option[Result]): Unit = cleanupOnSuccess(request)

  /** This method will be called bu StackAction when invokeBlock failed. */
  def cleanupOnFailure[A](request: ActionRequest[A], e: Throwable): Unit = ()

  // --[ ExecutionContext ] ----------------------------------------------------
  protected object ExecutionContextKey extends AttributeKey[ExecutionContext]
  protected def createStackActionExecutionContext(implicit req: ActionRequest[_]): ExecutionContext =
    req.get(ExecutionContextKey).getOrElse(Execution.defaultContext)
}

/** The custom playframework action. */
object StackAction {

  type ActionBuilder         = StackActionBuilder
  type BlockFunction[A]      = ActionRequest[A] => Result
  type AsyncBlockFunction[A] = ActionRequest[A] => Future[Result]

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

