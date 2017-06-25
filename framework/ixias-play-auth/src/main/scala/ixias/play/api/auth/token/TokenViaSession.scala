/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.token

import play.api.mvc.{ Result, RequestHeader }

case class TokenViaSession(sessionName: String) extends Token {
  import Token._

  /**
   * Put a specified security token to storage.
   */
  def put(result: Result, token: AuthenticityToken)(implicit request: RequestHeader): Result = {
    val signed = Token.signWithHMAC(token)
    result.withSession(request.session + (sessionName -> SignedToken.unwrap(signed)))
  }

  /**
   * Discard a security token in storage.
   */
  def discard(result: Result)(implicit request: RequestHeader): Result = result

  /**
   * Extract a security token from storage.
   */
  def extract(request: RequestHeader): Option[AuthenticityToken] =
    for {
      signed <- request.session.get(sessionName).map(SignedToken(_))
      token  <- Token.verifyHMAC(signed)
    } yield token
}
