/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.play.api.auth.datastore

import play.api.mvc.RequestHeader
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import net.ixias.play.api.auth.token._
import net.ixias.core.domain.model.Identity

/* Wrap an existing container. Useful to extend a container. */
case class WrappedContainer[Id <: Identity[_]](container: Container[Id]) {

  /** It is the first callback function executed
    * when the session is started automatically or manually */
  def open(uid: Id, expiry: Int): Future[AuthenticityToken] = open(uid, (expiry.toLong, SECONDS))
  def open(uid: Id, expiry: Duration): Future[AuthenticityToken] =
    Future.successful(container.open(uid, expiry))

  /** The read callback must always return
    * a user identity or none if there is no data to read */
  def read(token: AuthenticityToken): Future[Option[Id]] =
    Future.successful(container.read(token))

  /** This callback is executed when a session is destroyed */
  def destroy(token: AuthenticityToken): Future[Unit] =
    Future.successful(container.destroy(token))

  /** Sets the timeout setting. */
  def setTimeout(token: AuthenticityToken, expiry: Int): Future[Unit] = setTimeout(token, (expiry.toLong, SECONDS))
  def setTimeout(token: AuthenticityToken, expiry: Duration): Future[Unit] =
    Future.successful(container.setTimeout(token, expiry))

}
