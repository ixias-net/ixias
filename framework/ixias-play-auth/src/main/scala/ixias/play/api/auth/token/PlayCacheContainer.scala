/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.token

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Play._
import play.api.cache.Cache
import ixias.model.Identity

/**
 * The datastore for user session with using `play.cache.Cache`.
 */
case class PlayCacheContainer[Id <: Identity[_]]() extends Container[Id]
{
  /** The suffix as string of the cache-key */
  protected val prefix = "sid:"

  /**
   * It is the first callback function executed
   * when the session is started automatically or manually.
   */
  def open(uid: Id, expiry: Duration): Future[AuthenticityToken] =
    Token.generate(this) map { token =>
      Cache.set(prefix + token, uid, expiry)
      token
    }

  /**
   * The read callback must always return
   * a user identity or none if there is no data to read.
   */
  def read(token: AuthenticityToken): Future[Option[Id]] =
    Future(Cache.get(prefix + token).map(_.asInstanceOf[Id]))

  /**
   * This callback is executed when a session is destroyed.
   */
  def destroy(token: AuthenticityToken): Future[Unit] =
    Future(Cache.remove(prefix + token))

  /**
   * Sets the timeout setting.
   */
  def setTimeout(token: AuthenticityToken, expiry: Duration): Future[Unit] =
    for {
      Some(uid) <- read(token)
    } yield Cache.set(prefix + token, uid, expiry)
}
