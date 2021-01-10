/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.container

import java.time.Duration
import scala.concurrent.Future

import ixias.model._
import ixias.play.api.auth.token.Token
import play.api.mvc.RequestHeader

/**
 * The container for session's tokens.
 */
trait Container[K <: @@[_, _]] {
  import Token._

  /** The type of entity id */
  type Id   = K

  /** The execution context */
  implicit val executionContext: scala.concurrent.ExecutionContext

  /**
   * It is the first callback function executed
   * when the session is started automatically or manually.
   */
  def open(uid: Id, expiry: Option[Duration])
    (implicit request: RequestHeader): Future[AuthenticityToken]

  /**
   * Sets the timeout setting.
   */
  def setTimeout(token: AuthenticityToken, expiry: Option[Duration])
    (implicit request: RequestHeader): Future[Unit]

  /**
   * The read callback must always return
   * a user identity or none if there is no data to read.
   */
  def read(token: AuthenticityToken)
    (implicit request: RequestHeader): Future[Option[Id]]

  /**
   * This callback is executed when a session is destroyed.
   */
  def destroy(token: AuthenticityToken)
    (implicit request: RequestHeader): Future[Unit]
}
