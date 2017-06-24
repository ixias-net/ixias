/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.token

import play.api.mvc.{ Result, RequestHeader }

case class TokenViaSession(sessionName: String) extends Token {

  /**
   * Put a specified security token to storage.
   */
  def put(result: Result, token: AuthenticityToken)(implicit request: RequestHeader): Result =
    result.withSession(request.session + (sessionName -> token))

  /**
   * Discard a security token in storage.
   */
  def discard(result: Result)(implicit request: RequestHeader): Result = result

  /**
   * Extract a security token from storage.
   */
  def extract(request: RequestHeader): Option[AuthenticityToken] =
    request.session.get(sessionName).flatMap(Token.verifyHMAC)
}
