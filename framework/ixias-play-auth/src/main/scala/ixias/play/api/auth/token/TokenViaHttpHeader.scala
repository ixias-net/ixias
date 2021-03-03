/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.token

import play.api.mvc.{ RequestHeader, Result }

case class TokenViaHttpHeader(val name: String) extends Token {
  import Token._

  // The configuration
  def headerName = config.get[String](s"session.${name}.headerName")

  /**
   * Put a specified security token to HTTP-Headers.
   */
  def put(token: AuthenticityToken)(result: Result)(implicit request: RequestHeader): Result = {
    val signed = Token.signWithHMAC(token)
    result.withHeaders(headerName -> SignedToken.unwrap(signed))
  }

  /**
   * Discard a security token.
   */
  def discard(result: Result)(implicit request: RequestHeader): Result = result

  /**
   * Extract a security token from HTTP-Headers.
   */
  def extract(implicit request: RequestHeader): Option[AuthenticityToken] =
    for {
      signed <- request.headers.get(headerName).map(SignedToken(_))
      token  <- Token.verifyHMAC(signed)
    } yield token
}
