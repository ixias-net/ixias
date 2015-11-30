/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.play.api.auth.token

import play.api.mvc.{ RequestHeader, Result, Cookie, DiscardingCookie}

case class CookieToken(
  protected val cookieName:           String,
  protected val cookieMaxAge:         Option[Int]    = None,
  protected val cookiePathOption:     String         = "/",
  protected val cookieDomainOption:   Option[String] = None,
  protected val cookieSecureOption:   Boolean        = false,
  protected val cookieHttpOnlyOption: Boolean        = true
) extends Token {

  /** Put a specified security token to storage */
  def put(token: AuthenticityToken)(result: Result)(implicit request: RequestHeader): Result =
    result.withCookies(Cookie(
      cookieName,
      signWithHMAC(token),
      cookieMaxAge,
      cookiePathOption,
      cookieDomainOption,
      cookieSecureOption,
      cookieHttpOnlyOption
    ))

  /** Extract a security token from storage */
  def extract(request: RequestHeader): Option[AuthenticityToken] =
    request.cookies.get(cookieName).flatMap(c => verifyHMAC(c.value))

  /** Discard a security token in storage */
  def discard(result: Result)(implicit request: RequestHeader): Result =
    result.discardingCookies(DiscardingCookie(cookieName))

}

