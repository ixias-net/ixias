/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.container

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import ixias.model.Tagged
import ixias.play.api.auth.token._

/**
 * The container for session's tokens.
 */
trait Container[K <: Tagged[_, _]] {
  import Token._

  /** The type of entity id */
  type Id   = K

  /** The execution context */
  implicit val executionContext: scala.concurrent.ExecutionContext

  /**
   * It is the first callback function executed
   * when the session is started automatically or manually.
   */
  final def open(uid: Id, expiry: Duration): Future[AuthenticityToken] =
    for {
      newToken    <- Token.generate(this)(executionContext)
      storedToken <- open(uid, newToken, expiry)
    } yield storedToken

  /**
   * It is the first callback function executed
   * when the session is started automatically or manually.
   */
  def open(uid: Id, newToken: AuthenticityToken, expiry: Duration): Future[AuthenticityToken]

  /**
   * Sets the timeout setting.
   */
  def setTimeout(token: AuthenticityToken, expiry: Duration): Future[Unit]

  /**
   * The read callback must always return
   * a user identity or none if there is no data to read.
   */
  def read(token: AuthenticityToken): Future[Option[Id]]

  /**
   * This callback is executed when a session is destroyed.
   */
  def destroy(token: AuthenticityToken): Future[Unit]
}
