/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend.memcached

import java.io.{
  Serializable, InputStream, ObjectStreamClass,
  ByteArrayInputStream, ByteArrayOutputStream,
  ObjectInputStream,    ObjectOutputStream
}
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import scala.reflect.ClassTag
import scala.util.control.NonFatal

/**
 * Converts a value to/from the byte array stored in memcached.
 *
 * The encodings below are the ones `io.monix:shade` used, byte for byte, so that
 * entries written by the previous implementation stay readable.
 */
trait Codec[T] {
  def serialize(value: T): Array[Byte]
  def deserialize(data: Array[Byte]): T
}

/**
 * The codecs for the values which are serializable by `java.io.Serializable`.
 *
 * These live in a separate trait so that the codecs declared in
 * `MemcachedCodecs` take precedence over this generic fallback.
 * (implicits declared in a sub-trait win over the inherited ones)
 */
trait GenericCodecs {

  /**
   * Encodes the value with the standard java serialization, with no envelope of
   * our own around it.
   *
   * `deserialize` intentionally lets `java.io.InvalidClassException` escape so
   * that the caller can distinguish "the cached value has an incompatible
   * layout" from any other failure, and evict the entry instead.
   */
  implicit def AnyRefBinaryCodec[S <: Serializable](implicit ctag: ClassTag[S]): Codec[S] = new Codec[S] {
    def serialize(value: S): Array[Byte] = {
      val buffer = new ByteArrayOutputStream()
      val out    = new ObjectOutputStream(buffer)
      try { out.writeObject(value); out.flush() } finally out.close()
      buffer.toByteArray
    }
    def deserialize(data: Array[Byte]): S = {
      val in = new ClassTagObjectInputStream(ctag, new ByteArrayInputStream(data))
      try in.readObject().asInstanceOf[S] finally in.close()
    }
  }
}

/**
 * Resolves classes through the class loader of the expected type first.
 *
 * The default `ObjectInputStream` picks the latest user-defined class loader on
 * the stack, which under Play's dev/test mode is not the one holding the
 * application classes.
 */
private[memcached] class ClassTagObjectInputStream(ctag: ClassTag[_], in: InputStream)
    extends ObjectInputStream(in) {

  override protected def resolveClass(desc: ObjectStreamClass): Class[_] =
    try ctag.runtimeClass.getClassLoader.loadClass(desc.getName) catch {
      case NonFatal(_) =>
        try super.resolveClass(desc) catch {
          case NonFatal(_) =>
            Thread.currentThread().getContextClassLoader.loadClass(desc.getName)
        }
    }
}

/**
 * The codecs which are available by default on a memcached repository.
 */
trait MemcachedCodecs extends GenericCodecs {

  implicit object StringBinaryCodec extends Codec[String] {
    def serialize(value: String):    Array[Byte] = value.getBytes(StandardCharsets.UTF_8)
    def deserialize(data: Array[Byte]): String   = new String(data, StandardCharsets.UTF_8)
  }

  implicit object ArrayByteBinaryCodec extends Codec[Array[Byte]] {
    def serialize(value: Array[Byte]):    Array[Byte] = value
    def deserialize(data: Array[Byte]): Array[Byte]   = data
  }

  implicit object IntBinaryCodec extends Codec[Int] {
    def serialize(value: Int):    Array[Byte] = ByteBuffer.allocate(4).putInt(value).array()
    def deserialize(data: Array[Byte]): Int   = ByteBuffer.wrap(data).getInt
  }

  implicit object LongBinaryCodec extends Codec[Long] {
    def serialize(value: Long):    Array[Byte] = ByteBuffer.allocate(8).putLong(value).array()
    def deserialize(data: Array[Byte]): Long   = ByteBuffer.wrap(data).getLong
  }

  implicit object ShortBinaryCodec extends Codec[Short] {
    def serialize(value: Short):    Array[Byte] = ByteBuffer.allocate(2).putShort(value).array()
    def deserialize(data: Array[Byte]): Short   = ByteBuffer.wrap(data).getShort
  }

  implicit object CharBinaryCodec extends Codec[Char] {
    def serialize(value: Char):    Array[Byte] = ByteBuffer.allocate(2).putChar(value).array()
    def deserialize(data: Array[Byte]): Char   = ByteBuffer.wrap(data).getChar
  }

  implicit object DoubleBinaryCodec extends Codec[Double] {
    def serialize(value: Double): Array[Byte] =
      LongBinaryCodec.serialize(java.lang.Double.doubleToLongBits(value))
    def deserialize(data: Array[Byte]): Double =
      java.lang.Double.longBitsToDouble(LongBinaryCodec.deserialize(data))
  }

  implicit object FloatBinaryCodec extends Codec[Float] {
    def serialize(value: Float): Array[Byte] =
      IntBinaryCodec.serialize(java.lang.Float.floatToIntBits(value))
    def deserialize(data: Array[Byte]): Float =
      java.lang.Float.intBitsToFloat(IntBinaryCodec.deserialize(data))
  }

  implicit object BooleanBinaryCodec extends Codec[Boolean] {
    def serialize(value: Boolean):    Array[Byte] = Array((if (value) 1 else 0).toByte)
    def deserialize(data: Array[Byte]): Boolean   = data.headOption.contains(1.toByte)
  }
}

object MemcachedCodecs extends MemcachedCodecs
