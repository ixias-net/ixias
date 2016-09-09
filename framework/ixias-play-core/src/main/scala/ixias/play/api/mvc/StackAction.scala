/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import scala.reflect.ClassTag
import scala.util.{ Success, Failure }
import scala.util.control.{ NonFatal, ControlThrowable }
import scala.concurrent.{ Future, ExecutionContext }
import scala.collection.concurrent.TrieMap

import play.api.mvc._
import play.api.Application
import play.api.inject.Injector


// Wrap an existing request. Useful to extend a request.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
import StackActionRequest._
case class StackActionRequest[A](
  underlying: Request[A],
  attributes: TrieMap[AttributeKey[_], Any]
) extends WrappedRequest[A](underlying) {

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
}

// The declaration for request's attribute.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object StackActionRequest {

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


// Statck Action
//~~~~~~~~~~~~~~~~
object StackAction {

  /** The key of attribute for containing the current running application. */
  case object ApplicationKey extends AttributeKey[Application]

  // --[ Methods ] -------------------------------------------------------------
  /**
   * Constructs an `Action`.
   */
  final def apply[A](p: BodyParser[A], params: Attribute[_]*)
    (block: StackActionRequest[A] => Result)
    (implicit app: Application): Action[A] =
    createActionBuilder(params: _*).apply(p)(block)

  /**
   * Constructs an `Action` with default content.
   */
  final def apply(params: Attribute[_]*)
    (block: StackActionRequest[AnyContent] => Result)
    (implicit app: Application): Action[AnyContent] =
    createActionBuilder(params: _*).apply(block)

  /**
   * Constructs an `Action` with default content, and no request parameter.
   */
  final def apply(block: StackActionRequest[AnyContent] => Result)
    (implicit app: Application): Action[AnyContent] =
    createActionBuilder().apply(block)

  // --[ Methods ] -------------------------------------------------------------
  /**
   * Constructs an `Action` that returns a future of a result, with default content.
   */
  final def async[A](p: BodyParser[A], params: Attribute[_]*)
    (block: StackActionRequest[A] => Future[Result])
    (implicit app: Application): Action[A] =
    createActionBuilder(params: _*).async(p)(block)

  /**
   * Constructs an `Action` that returns a future of a result, with default content.
   */
  final def async(params: Attribute[_]*)
    (block: StackActionRequest[AnyContent] => Future[Result])
    (implicit app: Application): Action[AnyContent] =
    createActionBuilder(params: _*).async(block)

  /**
   * Constructs an `Action` that returns a future of a result,
   * with default content, and no request parameter.
   */
  final def async(block: StackActionRequest[AnyContent] => Future[Result])
    (implicit app: Application): Action[AnyContent] =
    createActionBuilder().async(block)

  // --[ Methods ] -------------------------------------------------------------
  /**
   * Create a new Action Builder
   */
  def createActionBuilder(params: Attribute[_]*)(implicit app: Application) =
    new ActionBuilder[StackActionRequest] {
      final def invokeBlock[A](request: Request[A], block: StackActionRequest[A] => Future[Result]): Future[Result] = {
        block(StackActionRequest(request, (
          new TrieMap[AttributeKey[_], Any]
            ++= params.map(_.toTuple)
            +=  (ApplicationKey -> app).toTuple
        )))
      }
    }
}

/**
 * A builder for generic Actions that generalizes over the type of requests.
 */
trait StackActionFunction extends ActionFunction[StackActionRequest, StackActionRequest] {

  /**
   * Instantiate a authprofile object.
   */
  protected def getInjector(request: StackActionRequest[_]): Option[Injector] =
    request.get(StackAction.ApplicationKey)
      .map(_.asInstanceOf[Application].injector)
}

/**
 * A simple kind of ActionFunction which, given a request, may
 * either immediately produce a Result (for example, an error), or call
 * its Action block with a parameter.
 * The critical (abstract) function is refine.
 */
trait StackActionRefiner extends StackActionFunction
    with ActionRefiner[StackActionRequest, StackActionRequest]

/**
 * A simple kind of ActionRefiner which, given a request,
 * unconditionally transforms it to a new parameter type to be passed to
 * its Action block.  The critical (abstract) function is transform.
 */
trait StackActionTransformer extends StackActionFunction
    with ActionTransformer[StackActionRequest, StackActionRequest]

/**
 * A simple kind of ActionRefiner which, given a request, may
 * either immediately produce a Result (for example, an error), or
 * continue its Action block with the same request.
 * The critical (abstract) function is filter.
 */
trait StackActionFilter extends StackActionFunction
    with ActionRefiner[StackActionRequest, StackActionRequest]
