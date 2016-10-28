package ixias.play.api.auth.token

import org.apache.commons.codec.binary.{Hex, StringUtils}
import org.apache.commons.codec.digest.DigestUtils
import org.keyczar.HmacKey

trait Signer {
  def sign(token: AuthenticityToken): SignedToken
  def verify(signedToken: SignedToken): Option[AuthenticityToken]
}

case class HMacSigner(seed: String) extends Signer {
  /*
  org.keyczar.Signer#signとverifyはStringを受けるインターフェースもあります。
  しかし、下記コードと違いBase64エンコードでStringにしているためsignatureの文字数が可変になるので、
  それを避け直接エンコード・デコードを行っています。
   */

  private val reader = MemoryKeyReader(new HmacKey(DigestUtils.sha256(seed)))
  private val signer = new org.keyczar.Signer(reader)

  def sign(token: AuthenticityToken): SignedToken = {
    val signature = signer.sign(StringUtils.getBytesUsAscii(token))
    new String(Hex.encodeHex(signature)) + token
  }

  def verify(signedToken: SignedToken): Option[AuthenticityToken] = {
    try {
      val (signature, token) = signedToken.splitAt(signer.digestSize * 2)
      signer.verify(StringUtils.getBytesUsAscii(token), Hex.decodeHex(signature.toCharArray)) match {
        case true => Some(token)
        case false => None
      }
    } catch { case _: Exception => None }
  }
}
