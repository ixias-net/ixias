/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.auth.mvc

import _root_.play.api.mvc._
import scala.concurrent.Future
import scala.util.{ Right, Left }

import play.api.auth.AuthProfile
import play.api.auth.mvc.StackRequest._

/** Provides the utility methods for authorization. */
class AuthorizedBuilder(params: Attribute[_]*)(implicit auth: AuthProfile) extends StackActionBuilder {

  /** Proceed with the next advice or target method invocation */
  override def proceed[A](req: StackRequest[A])(f: StackRequest[A] => Future[Result]): Future[Result] = {
    implicit val ctx = createStackActionExecutionContext(req)
    val authority = req.get(auth.AuthorityKey).map(_.asInstanceOf[auth.Authority])
    auth.authorized(authority)(req) flatMap {
      case Left(result) => Future.successful(result)
      case Right((user, updater)) => super.proceed(req.set(auth.UserKey, user))(f).map(updater)
    }
  }
}

/** The custom playframework action. */
object Authorized {

  type ActionBuilder         = AuthorizedBuilder
  type BlockFunction[A]      = StackRequest[A] => Result
  type AsyncBlockFunction[A] = StackRequest[A] => Future[Result]

  /** Constructs an `Action` with default content, and no request parameter. */
  final def apply(block: BlockFunction[AnyContent])
    (implicit auth: AuthProfile): Action[AnyContent] =
    new ActionBuilder().apply(block)

  /** Constructs an `Action` with default content. */
  final def apply(params: Attribute[_]*)(block: BlockFunction[AnyContent])
    (implicit auth: AuthProfile): Action[AnyContent] =
    new ActionBuilder(params: _*).apply(block)

  /** Constructs an `Action` with default content. */
  final def apply[A](p: BodyParser[A], params: Attribute[_]*)(block: BlockFunction[A])
    (implicit auth: AuthProfile): Action[A] =
    new ActionBuilder(params: _*).apply(p)(block)

  /** Constructs an `Action` that returns a future of a result,
    * with default content, and no request parameter. */
  final def async(block: AsyncBlockFunction[AnyContent])
    (implicit auth: AuthProfile): Action[AnyContent] =
    new ActionBuilder().async(block)

  /** Constructs an `Action` that returns a future of a result,
    * with default content. */
  final def async(params: Attribute[_]*)(block: AsyncBlockFunction[AnyContent])
    (implicit auth: AuthProfile): Action[AnyContent] =
    new ActionBuilder(params: _*).async(block)

  /** Constructs an `Action` that returns a future of a result,
    * with default content. */
  final def async[A](p: BodyParser[A], params: Attribute[_]*)(block: AsyncBlockFunction[A])
    (implicit auth: AuthProfile): Action[A] =
    new ActionBuilder(params: _*).async(p)(block)
}
