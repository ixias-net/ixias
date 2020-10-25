/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.security

import javax.crypto
import java.util.{ Random, Base64 }
import java.security.SecureRandom
import java.nio.{ ByteBuffer, IntBuffer }
import java.nio.charset.StandardCharsets.UTF_8
import scala.util.Try

/**
 * PBKDF2 (Password-Based Key Derivation Function 2) is a key derivation function
 * that is part of RSA Laboratories' Public-Key Cryptography Standards (PKCS) series,
 * specifically PKCS #5 v2.0,
 */
sealed case class PBKDF2Data(
  val algo: String,
  val salt: Array[Byte],
  val hash: Array[Byte],
  val iterations: Int
)

object PBKDF2 {
  val HASH_LENGTH           = 32
  val HASH_SALT_LENGTH      = 32
  val HASH_ITERATIONS_MIN   = 256
  val HASH_ITERATIONS_MAX   = 1024
  val HASH_ALGO             = "HmacSHA512"
  val HASH_ALGO_JAVA_TO_LIB = Map(
    "HmacSHA1"   -> "sha1",
    "HmacSHA256" -> "sha256",
    "HmacSHA512" -> "sha512"
  )

  /** Compares a given hash with a cleantext password. Also extracts the salt from the hash. */
  def compare(input: String, hash: String): Boolean =
    internals.decode(hash).map { case PBKDF2Data(algo, salt, hash, iterations) =>
      internals.compare(hash, internals.hash(input, salt, iterations, hash.length, algo))
    }.getOrElse(false)

  /** Uses PBKDF2 and random salt generation to create a hash based on some input.
    *
    * @param input      The string to be hashed.
    * @param salt       (optional) The salt.
    * @param iterations (optional) The number of iterations to be used.
    * @param length     (optional) The length the key will be cropped to fit.
    * @param algo       (optional) The hashing algorithm. */
  def hash(input: String, salt: Array[Byte] = internals.salt, iterations: Int = internals.iterations, length: Int = HASH_LENGTH, algo: String = HASH_ALGO): String =
    internals.encode(PBKDF2Data(algo, salt, internals.hash(input, salt, iterations, length, algo), iterations))

  /** Extracts the salt from a hash/salt-combination */
  def extractSalt(hash: String): Option[Array[Byte]] =
    internals.decode(hash).map{ case PBKDF2Data(_, p, _, _) => p }

  /** Extracts the saltlength from a hash/salt-combination */
  def extractSaltLength(hash: String): Option[Int] =
    internals.decode(hash).map{ case PBKDF2Data(_, p, _, _) => p.length }

  /** Extracts the hash from a hash/salt-combination */
  def extractHash(hash: String): Option[Array[Byte]] =
    internals.decode(hash).map{ case PBKDF2Data(_, _, p, _) => p }

  /** Extracts the number of iterations from a hash/salt-combination */
  def extractIterations(hash: String): Option[Int] =
    internals.decode(hash).map{ case PBKDF2Data(_, _, _, p) => p }

  /**
   * Implements internal methods.
   */
  private object internals {
    /** The random number of iterations from a hash/salt-combination */
    val iterations = (new Random()).nextInt(HASH_ITERATIONS_MAX - HASH_ITERATIONS_MIN) + HASH_ITERATIONS_MIN

    /** The random salt which is hashed together with the text as string */
    val salt: Array[Byte] = {
      val rand = new SecureRandom
      val b    = new Array[Byte](HASH_SALT_LENGTH)
      rand.nextBytes(b)
      b
    }

    /** Calaculate hash value. */
    def hash(input: String, salt: Array[Byte], iterations: Int, length: Int, algo: String): Array[Byte] = {
      // Initializes this Mac object with the given key.
      val mac = crypto.Mac.getInstance(algo)
      mac.init(new crypto.spec.SecretKeySpec(input.getBytes(UTF_8), "RAW"))

      // Calaculate hash value.
      val range = 1 to (length.toFloat / 20).ceil.toInt
      range.iterator.map(size => {
        var loop  = 1
        val hmac1 = mac.doFinal(salt ++ bytes(size))
        var hmac  = hmac1
        val buff  = IntBuffer.allocate(hmac1.length / 4).put(ByteBuffer.wrap(hmac1).asIntBuffer).array.clone
        while (loop < iterations) {
          hmac = mac.doFinal(hmac)
          xor(buff, hmac)
          loop += 1
        }
        val r = ByteBuffer.allocate(hmac1.length)
        r.asIntBuffer.put(buff)
        r.array
      }).flatten.take(length).toArray
    }

    /** Tests two byte arrays for value equality in constant time. */
    def compare(a1: Array[Byte], a2: Array[Byte]): Boolean = {
      a1.length == a2.length && a1.zip(a2).foldLeft(0) {
        case (r, (x1, x2)) => r | x1 ^ x2
      } == 0
    }

    /** Encode PBKDF2 data with the modular crypt format (MCF) */
    def encode(data :PBKDF2Data): String = {
      val itrs = data.iterations.toString
      val algo = HASH_ALGO_JAVA_TO_LIB.getOrElse(data.algo, data.algo)
      s"$$pbkdf2-$algo$$$itrs$$${b64Encoder(data.salt)}$$${b64Encoder(data.hash)}"
    }

    /** Decodes data encoded with the modular crypt format (MCF). */
    def decode(data: String): Option[PBKDF2Data] = {
      Try {
        val rx = "\\$pbkdf2-([^\\$]+)\\$(\\d+)\\$([^\\$]*)\\$([^\\$]*)".r
        data match {
          case rx(a, i, s, h) => Some(PBKDF2Data(
            HASH_ALGO_JAVA_TO_LIB.map(_.swap).getOrElse(a, a),
            b64Decoder(s),
            b64Decoder(h),
            i.toInt
          ))
          case _ => None
        }
      }.toOption.flatten
    }

    /** Decodes data encoded with MIME base64 */
    def b64Decoder(s: String): Array[Byte] =
      Base64.getDecoder.decode(s.replace(".", "+"))

    /** Encodes data with MIME base64 */
    def b64Encoder(b: Array[Byte]): String =
      Base64.getEncoder.withoutPadding.encodeToString(b).replace("+", ".")

    /** converto digital number to bytes data. */
    def bytes(i: Int) = ByteBuffer.allocate(4).putInt(i).array

    /** Performs a logical XOR of this bit set with the bit set argument. */
    def xor(buff: Array[Int], a2: Array[Byte]) {
      var pos = 0
      val b2  = ByteBuffer.wrap(a2).asIntBuffer
      val len = buff.array.size
      while (pos < len) {
        buff(pos) ^= b2.get(pos);
        pos += 1
      }
    }
  }
}
