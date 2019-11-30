/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.token

import ixias.model._
import ixias.security.TokenSigner
import ixias.util.Configuration
import play.api.mvc.{ RequestHeader, Result }

// The security token
//~~~~~~~~~~~~~~~~~~~~
trait Token {
  import Token._

  /** The configuration */
  protected val config = Configuration()

  /** Put a specified security token to storage */
  def put(token: AuthenticityToken)(result: Result)(implicit request: RequestHeader): Result

  /** Discard a security token in storage */
  def discard(result: Result)(implicit request: RequestHeader): Result

  /** Extract a security token from storage */
  def extract(implicit request: RequestHeader): Option[AuthenticityToken]
}

// Companion object
//~~~~~~~~~~~~~~~~~~
object Token {

  sealed    trait  Tag
  protected object Tag {
    trait SignedToken       extends Token
    trait AuthenticityToken extends Token
  }
  type SignedToken       = String @@ Tag.SignedToken
  type AuthenticityToken = String @@ Tag.AuthenticityToken
  val  SignedToken       = the[Identity[SignedToken]]
  val  AuthenticityToken = the[Identity[AuthenticityToken]]

  /** The object that provides some cryptographic operations */
  protected lazy val signer = TokenSigner()

  /** Verifies a given HMAC on a piece of data */
  final def verifyHMAC(signedToken: SignedToken): Option[AuthenticityToken] =
    signer.verify(SignedToken.unwrap(signedToken)).toOption
      .map(AuthenticityToken(_))

  /** Signs the given String with HMAC-SHA1 using the secret token.*/
  final def signWithHMAC(token: AuthenticityToken): SignedToken =
    SignedToken(signer.sign(AuthenticityToken.unwrap(token)))
}
