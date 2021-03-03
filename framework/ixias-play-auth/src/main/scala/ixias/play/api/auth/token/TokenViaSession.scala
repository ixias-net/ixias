/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.token

import play.api.mvc.{ Result, RequestHeader, Cookie, DiscardingCookie }
import play.api.mvc.Cookie.SameSite
import java.time.Duration
import ixias.util.ConfigLoader

case class TokenViaSession(val name: String) extends Token {
  import Token._

  private implicit val sameSiteConfigLoader: ConfigLoader[Option[SameSite]] =
    ConfigLoader(_.getString).map(SameSite.parse)

  // The configuration
  def cookieName = config.get[String]           (s"session.${name}.cookieName")
  def maxAge     = config.get[Option[Duration]] (s"session.${name}.maxAge")
  def path       = config.get[String]           (s"session.${name}.path")
  def domain     = config.get[Option[String]]   (s"session.${name}.domain")
  def secure     = config.get[Boolean]          (s"session.${name}.secure")
  def httpOnly   = config.get[Boolean]          (s"session.${name}.httpOnly")
  def sameSite   = config.get[Option[SameSite]] (s"session.${name}.sameSite")

  /**
   * Put a specified security token to storage.
   */
  def put(token: AuthenticityToken)(result: Result)(implicit request: RequestHeader): Result = {
    val signed     = SignedToken.unwrap(Token.signWithHMAC(token))
    val maxAgeSecs = maxAge.map(_.getSeconds.toInt)
    val cookie     = Cookie(cookieName, signed, maxAgeSecs, path, domain, secure, httpOnly, sameSite)
    result.withCookies(cookie)
  }

  /**
   * Discard a security token in storage.
   */
  def discard(result: Result)(implicit request: RequestHeader): Result =
    result.discardingCookies(DiscardingCookie(cookieName))

  /**
   * Extract a security token from storage.
   */
  def extract(implicit request: RequestHeader): Option[AuthenticityToken] =
    for {
      signed <- request.cookies.get(cookieName).map(c => SignedToken(c.value))
      token  <- Token.verifyHMAC(signed)
    } yield token
}
