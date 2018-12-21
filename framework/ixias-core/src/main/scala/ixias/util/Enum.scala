/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.util

import scala.reflect._

/**
 * The Enums based on sealed classes
 *
 * < Example >
 * sealed abstract class Color(red: Double, green: Double, blue: Double)
 * object Color extends EnumOf[Color] {
 *   case object Red   extends Color(1, 0, 0)
 *   case object Green extends Color(0, 1, 0)
 *   case object Blue  extends Color(0, 0, 1)
 *   case object White extends Color(0, 0, 0)
 *   case object Black extends Color(1, 1, 1)
 * }
 *
 */
trait Enum          extends Serializable
trait EnumStatus    extends Enum { val code: Short }
trait EnumBitFlags  extends Enum { val code: Long  }

/**
 * The operation of EnumStatus
 */
object EnumStatus {
  abstract class Of[T <: EnumStatus]
    (implicit ttag: ClassTag[T]) extends Enum.Of[T] {
    def apply(code: Short): T = this.find(_.code == code).get
  }
}

/**
 * The operation of EnumBitFlags
 */
object EnumBitFlags {
  abstract class Of[T <: EnumBitFlags]
    (implicit ttag: ClassTag[T]) extends Enum.Of[T] {

    /** Get bitset objects from numeric bitset. */
    def apply(bitset: Long): Seq[T] =
      this.filter(p => (p.code & bitset) == p.code)

    /** Calaculate bitset as numeric */
    def toBitset(bitset: Seq[T]): Long =
      bitset.foldLeft(0L)((code, cur) => code | cur.code)

    /** Check to whether has a bit flag. */
    def hasBitFlag(bitset: Seq[T], flag: T):    Boolean = (toBitset(bitset) & flag.code) == flag.code
    def hasBitFlag(bitset: Seq[T], code: Long): Boolean = (toBitset(bitset) & code) == code
    def hasBitFlag(bitset: Long,   flag: T):    Boolean = (bitset & flag.code) == flag.code
    def hasBitFlag(bitset: Long,   code: Long): Boolean = (bitset & code) == code

    /** Set a specified bit flag. */
    def setBitFlag(bitset: Seq[T], flag: T):    Seq[T] = apply(toBitset(bitset) | flag.code)
    def setBitFlag(bitset: Seq[T], code: Long): Seq[T] = apply(toBitset(bitset) | code)
    def setBitFlag(bitset: Long,   flag: T):    Long = bitset | flag.code
    def setBitFlag(bitset: Long,   code: Long): Long = bitset | code
  }
}

/**
 * The based operation of Enum
 */
object Enum {
  abstract class Of[T <: Enum](implicit ttag: ClassTag[T]) { self =>
    import runtime.universe._

    // --[ Methods ]-------------------------------------------------------------=
    /**
     * The list of values for Enumeration.
     */
    def values: List[T] = fields.getOrElse(getSelfFields).collect{ case v: T => v }
    lazy final val map1: Map[String, T] = values.map(v => v.toString -> v).toMap
    lazy final val map2: Map[String, T] = values.map(v => v.toString.toLowerCase -> v).toMap

    /**
     * The myself instance fields.
     */
    private lazy val fields: Option[List[Any]] = {
      val mirror = runtimeMirror(self.getClass.getClassLoader)
      val symbol = mirror.classSymbol(self.getClass)
      if (symbol.isModuleClass) {
        Some(sortedInnerModules(symbol).map(
          m => mirror.reflectModule(m.asModule).instance))
      } else {
        None
      }
    }

    private def getSelfFields: List[Any] = {
      val mirror = runtimeMirror(self.getClass.getClassLoader)
      val symbol = mirror.classSymbol(self.getClass)
      sortedInnerModules(symbol).map(m => mirror.reflect(self).reflectModule(m.asModule).instance)
    }

    private def sortedInnerModules(symbol: ClassSymbol): List[Symbol] =
      symbol.toType.members.sorted.filter(_.isModule)

    // --[ Methods ]-------------------------------------------------------------=
    /**
     * Finds the first element of the sequence satisfying a predicate, if any.
     */
    def find(f: T => Boolean): Option[T] = values.find(f)

    /**
     * Selects all elements of this Enum which satisfy a predicate.
     */
    def filter(f: T => Boolean): List[T] = values.filter(f)

    // --[ Methods ]-------------------------------------------------------------=
    /**
     * Retrieve the index number of the member passed in the values picked up by this enum
     */
    def indexOf[M >: T](member: M): Int = values.indexOf(member)

    /**
     * Optionally returns an enum's member for a given name.
     */
    def withNameOption(name: String): Option[T] = map1.get(name)

    /**
     * Optionally returns an enum's member for a given name, disregarding case
     */
    def withNameInsensitiveOption(name: String): Option[T] = map2.get(name.toLowerCase)

    /**
     * Retrieve an enum's member for a given name
     */
    def withName(name: String): T =
      withNameOption(name).getOrElse(
        throw new NoSuchElementException(s"$name is not a member of Enum $this"))

    /**
     * Retrieve an enum's member for a given name, disregarding case
     */
    def withNameInsensitive(name: String): T =
      withNameInsensitiveOption(name).getOrElse(
        throw new NoSuchElementException(s"$name is not a member of Enum $this"))
  }
}
