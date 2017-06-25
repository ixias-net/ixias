/*
 * This file is part of the IxiaS services.
 *
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
trait EnumStatus[T] extends Enum { type Code = T; val code: T }
trait EnumBitFlags  extends EnumStatus[Long]

/**
 * The operation of EnumStatus
 */
abstract class EnumStatusOf[V <: EnumStatus[_]]
  (implicit ttag: ClassTag[V]) extends EnumOf[V] {
  def apply(code: V#Code): V = this.find(_.code == code).get
}

/**
 * The operation of EnumBitFlags
 */
abstract class EnumBitFlagsOf[V <: EnumBitFlags]
  (implicit ttag: ClassTag[V]) extends EnumOf[V] {

  /** Get bitset objects from numeric bitset. */
  def apply(bitset: V#Code): Seq[V] =
    this.filter(p => (p.code & bitset) == p.code)

  /** Calaculate bitset as numeric */
  def toBitset(bitset: Seq[V]): V#Code =
    bitset.foldLeft(0L)((code, cur) => code | cur.code)

  /** Check to whether has a bit flag. */
  def hasBitFlag(bitset: Seq[V], flag: V):      Boolean = (toBitset(bitset) & flag.code) == flag.code
  def hasBitFlag(bitset: Seq[V], code: V#Code): Boolean = (toBitset(bitset) & code) == code
  def hasBitFlag(bitset: V#Code, flag: V):      Boolean = (bitset & flag.code) == flag.code
  def hasBitFlag(bitset: V#Code, code: V#Code): Boolean = (bitset & code) == code

  /** Set a specified bit flag. */
  def setBitFlag(bitset: Seq[V], flag: V):      Seq[V] = apply(toBitset(bitset) | flag.code)
  def setBitFlag(bitset: Seq[V], code: V#Code): Seq[V] = apply(toBitset(bitset) | code)
  def setBitFlag(bitset: V#Code, flag: V):      V#Code = bitset | flag.code
  def setBitFlag(bitset: V#Code, code: V#Code): V#Code = bitset | code
}

/**
 * The based operation of Enum
 */
abstract class EnumOf[V <: Enum](implicit ttag: ClassTag[V]) { self =>
  import runtime.universe._

  // --[ Methods ]-------------------------------------------------------------=
  /**
   * The list of values for Enumeration.
   */
  def values: List[V] = fields.getOrElse(getSelfFields).collect{ case v: V => v }
  lazy final val map1: Map[String, V] = values.map(v => v.toString -> v).toMap
  lazy final val map2: Map[String, V] = values.map(v => v.toString.toLowerCase -> v).toMap

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
  def find(f: V => Boolean): Option[V] = values.find(f)

  /**
   * Selects all elements of this Enum which satisfy a predicate.
   */
  def filter(f: V => Boolean): List[V] = values.filter(f)

  // --[ Methods ]-------------------------------------------------------------=
  /**
   * Retrieve the index number of the member passed in the values picked up by this enum
   */
  def indexOf[M >: V](member: M): Int = values.indexOf(member)

  /**
   * Optionally returns an enum's member for a given name.
   */
  def withNameOption(name: String): Option[V] = map1.get(name)

  /**
   * Optionally returns an enum's member for a given name, disregarding case
   */
  def withNameInsensitiveOption(name: String): Option[V] = map2.get(name.toLowerCase)

  /**
   * Retrieve an enum's member for a given name
   */
  def withName(name: String): V =
    withNameOption(name).getOrElse(
      throw new NoSuchElementException(s"$name is not a member of Enum $this"))

  /**
   * Retrieve an enum's member for a given name, disregarding case
   */
  def withNameInsensitive(name: String): V =
    withNameInsensitiveOption(name).getOrElse(
      throw new NoSuchElementException(s"$name is not a member of Enum $this"))

}
