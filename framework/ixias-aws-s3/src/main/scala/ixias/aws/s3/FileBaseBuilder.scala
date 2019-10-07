/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3

import ixias.model._
import ixias.util.Enum
import ixias.security.RandomStringToken
import ixias.aws.s3.model.File
import ixias.aws.s3.backend.DataSourceName

// To build file a object
//~~~~~~~~~~~~~~~~~~~~~~~~~
trait FileBaseBuilder[K <: @@[Long, _], M <: EntityModel[K]] {

  type Id    = K
  type Model = M

  // --[ Properties ]-----------------------------------------------------------
  /** division unit */
  val DIV_UNIT: DivUnit

  /** directory seperator as string */
  val DIRECTORY_SEPARATOR = """/"""

  // --[ Enum: silo division unit ]-------------------------------------------
  sealed abstract class DivUnit extends Enum
  object DivUnit extends Enum.Of[DivUnit] {
    case object IS_DIV_100   extends DivUnit
    case object IS_DIV_10000 extends DivUnit
  }

  // --[ Methods ]------------------------------------------------------------
  /**
   * Get a key-name of the file in the storage destination.
   */
  def apply(namespace: String, fprefix: String, base: Model, typedef: String = "unknown")
    (implicit dsn: DataSourceName): File#WithNoId =
    (for {
      fkey <- scala.util.Try(build(
        namespace,
        "%s-%s".format(fprefix, RandomStringToken.next(64)),
        base
      ))
      file <- File(fkey, typedef, None)
    } yield file) getOrElse {
      val message = "Failed to create file object. dsn, baseModel = %s".format(dsn, base)
      throw new IllegalStateException(message)
    }

  /**
   * Build division unit of silo
   */
  def silo(id: Id): String = {
    val tok = "%04d".format(id % 10000)
    val mid = tok.drop(2)
    val sid = tok.take(2)
    (DIV_UNIT match {
      case DivUnit.IS_DIV_100   => Seq(     sid, id.toString)
      case DivUnit.IS_DIV_10000 => Seq(mid, sid, id.toString)
    }).mkString(DIRECTORY_SEPARATOR)
  }

  // --[ Methods ]------------------------------------------------------------
  /**
   * Get a key-name of the file in the storage destination.
   */
  protected def build(namespace: String, fname: String, base: Model): String
}
