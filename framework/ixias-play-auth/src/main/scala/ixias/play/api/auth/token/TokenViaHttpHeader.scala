/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.token

import play.api.mvc.{ RequestHeader, Result }

case class TokenViaHttpHeader(
  protected val headerName: String
) extends Token {

  /**
   * Put a specified security token to HTTP-Headers.
   */
  def put(token: AuthenticityToken)(result: Result)(implicit request: RequestHeader): Result =
    result.withHeaders(headerName -> Token.signWithHMAC(token))

  /**
   * Extract a security token from HTTP-Headers.
   */
  def extract(request: RequestHeader): Option[AuthenticityToken] =
    request.headers.get(headerName).flatMap(Token.verifyHMAC)

  /**
   * Discard a security token.
   */
  def discard(result: Result)(implicit request: RequestHeader): Result = result
}
