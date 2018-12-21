/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.security

import scala.util.Try
import com.typesafe.config.ConfigFactory

import org.keyczar.Signer
import org.keyczar.HmacKey
import org.keyczar.interfaces.KeyczarReader
import org.apache.commons.codec.binary.{Hex, StringUtils}
import org.apache.commons.codec.digest.DigestUtils

/**
 * TokenVerifiers verify session token signatures generated.
 */
case class TokenSigner(reader: KeyczarReader) {

  lazy val signer = new Signer(reader)

  /**
   * Signs the input and produces a signature.
   */
  def sign(token: String): String = {
    val signature = signer.sign(StringUtils.getBytesUsAscii(token))
    new String(Hex.encodeHex(signature)) + token
  }

  /**
   * Verify the session token signature on the given data
   */
  def verify(signedToken: String): Try[String] =
    Try {
      val (signature, token) = signedToken.splitAt(signer.digestSize * 2)
      signer.verify(
        StringUtils.getBytesUsAscii(token),
        Hex.decodeHex(signature.toCharArray)
      ) match {
        case true  => token
        case false => throw new java.security.SignatureException
      }
    }
}

// Companion object
//~~~~~~~~~~~~~~~~~~
object TokenSigner {

  /** Creates a TokenSigner. */
  def apply() = {
    val config = ConfigFactory.load()
    val secret = config.getString("session.token.secret")
    new TokenSigner(HmacKeyReader(new HmacKey(DigestUtils.sha256(secret))))
  }
}
