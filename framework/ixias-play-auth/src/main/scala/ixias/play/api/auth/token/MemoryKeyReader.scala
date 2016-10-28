/*
 * This file is part of the nextbeat services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.token

import org.keyczar.enums.{KeyPurpose, KeyStatus}
import org.keyczar.interfaces.KeyczarReader
import org.keyczar._

case class MemoryKeyReader(metadata: KeyMetadata, keys: Seq[KeyczarKey]) extends KeyczarReader {

  override def getMetadata: String = metadata.toString

  override def getKey(version: Int): String = keys(version).toString

  override def getKey: String = {
    val metadeta = KeyMetadata.read(getMetadata)
    getKey(metadeta.getPrimaryVersion.getVersionNumber)
  }
}

object MemoryKeyReader{
  def apply(key: HmacKey): MemoryKeyReader = {
    val metadata = new KeyMetadata("Imported HMAC", KeyPurpose.SIGN_AND_VERIFY, DefaultKeyType.HMAC_SHA1)
    val version =  new KeyVersion(0, KeyStatus.PRIMARY, false)
    metadata.addVersion(version)
    val keys = IndexedSeq(key)
    MemoryKeyReader(metadata = metadata, keys = keys)
  }

  def apply(key: AesKey): MemoryKeyReader = {
    val metadata = new KeyMetadata("Imported HMAC", KeyPurpose.DECRYPT_AND_ENCRYPT, DefaultKeyType.AES)
    val version: KeyVersion = new KeyVersion(0, KeyStatus.PRIMARY, false)
    metadata.addVersion(version)
    val keys = IndexedSeq(key)
    MemoryKeyReader(metadata = metadata, keys = keys)
  }
}