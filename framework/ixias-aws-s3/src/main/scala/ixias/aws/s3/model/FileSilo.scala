/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.model

import ixias.model._
import ixias.util.Enum

// Silo configuration for partitioning data
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
trait FileSilo[K <: @@[_, _]] {

  type Id = K

  /** directory seperator as string */
  val DIRECTORY_SEPARATOR = """/"""

  /**
   * Build division unit of silo
   */
  def silo(id: Id): String
}

// For String type Id
//~~~~~~~~~~~~~~~~~~~~
trait FileSiloAsString[K <: @@[String, _]] extends FileSilo[K] {

  /**
   * Build division unit of silo
   */
  def silo(id: Id): String = id
}

// For Long type Id
//~~~~~~~~~~~~~~~~~~~
trait FileSiloAsLong[K <: @@[Long, _]] extends FileSilo[K] {

  // --[ Properties ]-----------------------------------------------------------
  /** division unit */
  val divUnit: DivUnit

  // --[ Enum: silo division unit ]-------------------------------------------
  sealed abstract class DivUnit extends Enum
  object DivUnit extends Enum.Of[DivUnit] {
    case object IS_DIV_NONE  extends DivUnit
    case object IS_DIV_100   extends DivUnit
    case object IS_DIV_10000 extends DivUnit
  }

  // --[ Methods ]------------------------------------------------------------
  /**
   * Build division unit of silo
   */
  def silo(id: Id): String = {
    val tok = "%04d".format(id % 10000)
    val mid = tok.drop(2)
    val sid = tok.take(2)
    (divUnit match {
      case DivUnit.IS_DIV_NONE  => Seq(          id.toString)
      case DivUnit.IS_DIV_100   => Seq(     sid, id.toString)
      case DivUnit.IS_DIV_10000 => Seq(mid, sid, id.toString)
    }).mkString(DIRECTORY_SEPARATOR)
  }
}
