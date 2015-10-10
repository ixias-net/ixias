/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.core
package util

import scala.reflect._

/** The Enums based on sealed classes
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
abstract class EnumOf[+V: ClassTag] { self =>
  import runtime.universe._

  // --[ Properties ]-----------------------------------------------------------
  /** The list of values for Enumeration. */
  def values: List[V] = fields.getOrElse(getSelfFields).collect{ case v: V => v }
  lazy final val map1: Map[String, V] = values.map(v => v.toString -> v).toMap
  lazy final val map2: Map[String, V] = values.map(v => v.toString.toLowerCase -> v).toMap

  /** The myself instance fields. */
  protected lazy val fields: Option[List[Any]] = {
    val mirror = runtimeMirror(self.getClass.getClassLoader)
    val symbol = mirror.classSymbol(self.getClass)
    if (symbol.isModuleClass) {
      Some(sortedInnerModules(symbol).map(
        m => mirror.reflectModule(m.asModule).instance))
    } else {
      None
    }
  }

  // --[ Methods ]-------------------------------------------------------------=
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

  // --[ Methods ]-------------------------------------------------------------=
  private def getSelfFields: List[Any] = {
    val mirror = runtimeMirror(self.getClass.getClassLoader)
    val symbol = mirror.classSymbol(self.getClass)
    sortedInnerModules(symbol).map(m => mirror.reflect(self).reflectModule(m.asModule).instance)
  }

  private def sortedInnerModules(symbol: ClassSymbol): List[Symbol] =
    symbol.toType.members.sorted.filter(_.isModule)
}
