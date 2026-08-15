/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import slick.lifted.Rep
import slick.jdbc.JdbcProfile
import slick.ast.Library.SqlOperator
import scala.language.implicitConversions

trait SlickRepOps[P <: JdbcProfile] {
  val driver: P
  implicit def bitwiseColumnExtensionMethods[B1](c: Rep[B1]): BaseBitwiseColumnExtensionMethods[B1] =
    new BaseBitwiseColumnExtensionMethods[B1](c)
}

// Bitwise operators
//~~~~~~~~~~~~~~~~~~~
trait BitwiseColumnExtensionMethods[B1, P1] extends Any with slick.lifted.ExtensionMethods[B1, P1] {
  def & [P2, R](e: Rep[P2])(implicit om: o#arg[B1, P2]#to[B1, R]) =
    om.column(new SqlOperator("&"), n, e.toNode)
}
final class BaseBitwiseColumnExtensionMethods[P1](val c: Rep[P1]) extends AnyVal
    with BitwiseColumnExtensionMethods[P1, P1]
    with slick.lifted.BaseExtensionMethods[P1]

