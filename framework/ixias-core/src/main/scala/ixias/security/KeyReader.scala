/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.security

import org.keyczar._
import org.keyczar.enums.{ KeyPurpose, KeyStatus }
import org.keyczar.interfaces.KeyczarReader

/**
 * These will read key files from disk,
 */
class KeyReader(
  metadata: KeyMetadata,
  keys:     Seq[KeyczarKey]
) extends KeyczarReader {

  /** Gets an input stream of a particular version of a key. */
  def getKey: String =
    getKey(KeyMetadata.read(getMetadata)
      .getPrimaryVersion.getVersionNumber)

  /** Gets an input stream of the primary key. */
  def getKey(version: Int): String =
    keys(version).toString

  /** Gets metadata for a set of keys */
  def getMetadata: String = metadata.toString
}


/**
 * Keyczar KeyReader that reads from a HMAC private key file
 */
object HmacKeyReader {

  /** Creates a HmacKeyReader. */
  def apply(key: HmacKey) = {
    val version  = new KeyVersion(0, KeyStatus.PRIMARY, false)
    val metadata = new KeyMetadata("Imported from HMAC", KeyPurpose.SIGN_AND_VERIFY, DefaultKeyType.HMAC_SHA1)
    metadata.addVersion(version)
    new KeyReader(metadata = metadata, keys = IndexedSeq(key))
  }
}

/**
 * Keyczar KeyReader that reads from a AES private key file
 */
object AesKeyReader {

  /** Creates a AesKeyReader. */
  def apply(key: AesKey) = {
    val version  = new KeyVersion(0, KeyStatus.PRIMARY, false)
    val metadata = new KeyMetadata("Imported from AES", KeyPurpose.DECRYPT_AND_ENCRYPT, DefaultKeyType.AES)
    metadata.addVersion(version)
    new KeyReader(metadata = metadata, keys = IndexedSeq(key))
  }
}
