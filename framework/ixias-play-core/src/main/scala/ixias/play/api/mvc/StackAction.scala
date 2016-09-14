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
import scala.language.higherKinds

import play.api.mvc._
import play.api.Application
import play.api.inject.Injector


// Wrap an existing request. Useful to extend a request.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
import StackActionRequest._
case class StackActionRequest[A](
  underlying: Request[A],
  attributes: TrieMap[AttributeKey[_], Any] = TrieMap.empty
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

// Statck Action Function
//~~~~~~~~~~~~~~~~~~~~~~~~~
/**
 * A builder for generic Actions that generalizes over the type of requests.
 */
trait StackActionFunction extends ActionFunction[StackActionRequest, StackActionRequest] {

  /**
   * The key of attribute for containing the current running application.
   */
  case object ApplicationKey extends AttributeKey[Application]

  /**
   * Invokes a block process with the current running application.
   */
  def withApplication(request: StackActionRequest[_])(block: Application => Future[Result]): Future[Result] = {
    implicit val ctx = executionContext
    for {
      app    <- Future(request.get(ApplicationKey).map(_.asInstanceOf[Application]).get)
      result <- block(app)
    } yield result
  }
}

/**
 * Provides helpers for creating `Action` values.
 */
trait StackActionBuilder extends StackActionFunction {
  self =>

  // --[ Methods ] -------------------------------------------------------------
  /**
   * Constructs an `Action` with default content, and no request parameter.
   */
  final def apply
    (block: => Result)
    (implicit app: Application): Action[AnyContent] =
    apply(BodyParsers.parse.ignore(AnyContentAsEmpty: AnyContent))(_ => block)

  /**
   * Constructs an `Action` with default content.
   */
  final def apply
    (params: Attribute[_]*)
    (block: => Result)
    (implicit app: Application): Action[AnyContent] =
    apply(BodyParsers.parse.ignore(AnyContentAsEmpty: AnyContent), params: _*)(_ => block)

  /**
   * Constructs an `Action` with default content
   */
  final def apply
    (block: StackActionRequest[AnyContent] => Result)
    (implicit app: Application): Action[AnyContent] =
    apply(BodyParsers.parse.default)(block)

  /**
   * Constructs an `Action` with default content.
   */
  final def apply
    (params: Attribute[_]*)
    (block: StackActionRequest[AnyContent] => Result)
    (implicit app: Application): Action[AnyContent] =
    apply(BodyParsers.parse.default, params: _*)(block)

  /**
   * Constructs an `Action`.
   */
  final def apply[A]
    (bodyParser: BodyParser[A], params: Attribute[_]*)
    (block: StackActionRequest[A] => Result)
    (implicit app: Application): Action[A] =
    async(bodyParser, params: _*) { request: StackActionRequest[A] =>
      Future.successful(block(request))
    }

  // --[ Methods ] -------------------------------------------------------------
  /**
   * Constructs an `Action` that returns a future of a result,
   * with default content, and no request parameter.
   */
  final def async
    (block: => Future[Result])
    (implicit app: Application): Action[AnyContent] =
    async(BodyParsers.parse.ignore(AnyContentAsEmpty: AnyContent))(_ => block)

  /**
   * Constructs an `Action` that returns a future of a result,
   * with default content, and no request parameter.
   */
  final def async
    (params: Attribute[_]*)
    (block: => Future[Result])
    (implicit app: Application): Action[AnyContent] =
    async(BodyParsers.parse.ignore(AnyContentAsEmpty: AnyContent), params: _*)(_ => block)

  /**
   * Constructs an `Action` that returns a future of a result, with default content
   */
  final def async
    (block: StackActionRequest[AnyContent] => Future[Result])
    (implicit app: Application): Action[AnyContent] =
    async(BodyParsers.parse.default)(block)

  /**
   * Constructs an `Action` that returns a future of a result, with default content.
   */
  final def async
    (params: Attribute[_]*)
    (block: StackActionRequest[AnyContent] => Future[Result])
    (implicit app: Application): Action[AnyContent] =
    async(BodyParsers.parse.default, params: _*)(block)

  /**
   * Constructs an `Action` that returns a future of a result, with default content.
   */
  final def async[A]
    (bodyParser: BodyParser[A], params: Attribute[_]*)
    (block: StackActionRequest[A] => Future[Result])
    (implicit app: Application): Action[A] = composeAction(new Action[A] {
      def parser = composeParser(bodyParser)
      def apply(request: Request[A]) = try {
        invokeBlock(StackActionRequest(request, (
          new TrieMap[AttributeKey[_], Any]
            ++= params.map(_.toTuple)
            +=  (ApplicationKey -> app).toTuple
        )), block)
      } catch {
        // NotImplementedError is not caught by NonFatal, wrap it
        case e: NotImplementedError => throw new RuntimeException(e)
        // LinkageError is similarly harmless in Play Framework, since automatic reloading could easily trigger it
        case e: LinkageError => throw new RuntimeException(e)
      }
      override def executionContext = StackActionBuilder.this.executionContext
    })


  /**
   * Compose the parser.  This allows the action builder to potentially intercept requests before they are parsed.
   *
   * @param bodyParser The body parser to compose
   * @return The composed body parser
   */
  protected def composeParser[A](bodyParser: BodyParser[A]): BodyParser[A] = bodyParser

  /**
   * Compose the action with other actions.  This allows mixing in of various actions together.
   *
   * @param action The action to compose
   * @return The composed action
   */
  protected def composeAction[A](action: Action[A]): Action[A] = action

  override def andThen[Q[_]](other: ActionFunction[StackActionRequest, Q]): ActionBuilder[Q] = new ActionBuilder[Q] {
    def invokeBlock[A](request: Request[A], block: Q[A] => Future[Result]) =
      self.invokeBlock[A](StackActionRequest(request), other.invokeBlock[A](_, block))
    override protected def composeParser[A](bodyParser: BodyParser[A]): BodyParser[A] = self.composeParser(bodyParser)
    override protected def composeAction[A](action: Action[A]): Action[A] = self.composeAction(action)
  }
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
