/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.auth.data

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import play.api.auth.token.AuthenticityToken
import core.domain.model.Identity

trait Container[Id <: Identity[_]] {

  /** It is the first callback function executed
    * when the session is started automatically or manually */
  def open(uid: Id, expiry: Duration): Future[AuthenticityToken]

  /** The read callback must always return
    * a user identity or none if there is no data to read */
  def read(token: AuthenticityToken): Future[Option[Id]]

  /** This callback is executed when a session is destroyed */
  def destroy(token: AuthenticityToken): Future[Unit]

  /** Sets the timeout setting. */
  def setTimeout(token: AuthenticityToken, expiry: Duration): Future[Unit]

}
