/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import ixias.model._
import slick.jdbc.JdbcProfile
import scala.language.implicitConversions

trait SlickRepUnsafeOps[P <: JdbcProfile] extends SlickRepOps[P]
{
  import driver.api._

  implicit def       toByteSlickRep[T](v:       Byte @@ T): slick.lifted.Rep[Byte]       = the[Identity[Byte       @@ T]].unwrap(v)
  implicit def      toShortSlickRep[T](v:      Short @@ T): slick.lifted.Rep[Short]      = the[Identity[Short      @@ T]].unwrap(v)
  implicit def        toIntSlickRep[T](v:        Int @@ T): slick.lifted.Rep[Int]        = the[Identity[Int        @@ T]].unwrap(v)
  implicit def       toLongSlickRep[T](v:       Long @@ T): slick.lifted.Rep[Long]       = the[Identity[Long       @@ T]].unwrap(v)
  implicit def toBigDecimalSlickRep[T](v: BigDecimal @@ T): slick.lifted.Rep[BigDecimal] = the[Identity[BigDecimal @@ T]].unwrap(v)
  implicit def     toStringSlickRep[T](v:     String @@ T): slick.lifted.Rep[String]     = the[Identity[String     @@ T]].unwrap(v)

  implicit def       toByteSlickRep[T](v: Option[      Byte @@ T]): slick.lifted.Rep[Option[Byte]]       = v.map(the[Identity[Byte       @@ T]].unwrap)
  implicit def      toShortSlickRep[T](v: Option[     Short @@ T]): slick.lifted.Rep[Option[Short]]      = v.map(the[Identity[Short      @@ T]].unwrap)
  implicit def        toIntSlickRep[T](v: Option[       Int @@ T]): slick.lifted.Rep[Option[Int]]        = v.map(the[Identity[Int        @@ T]].unwrap)
  implicit def       toLongSlickRep[T](v: Option[      Long @@ T]): slick.lifted.Rep[Option[Long]]       = v.map(the[Identity[Long       @@ T]].unwrap)
  implicit def toBigDecimalSlickRep[T](v: Option[BigDecimal @@ T]): slick.lifted.Rep[Option[BigDecimal]] = v.map(the[Identity[BigDecimal @@ T]].unwrap)
  implicit def     toStringSlickRep[T](v: Option[    String @@ T]): slick.lifted.Rep[Option[String]]     = v.map(the[Identity[String     @@ T]].unwrap)
}
