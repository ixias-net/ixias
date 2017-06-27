/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import ixias.model._
import slick.jdbc.JdbcProfile
import scala.language.implicitConversions

trait SlickRepOps[P <: JdbcProfile]
{
  val    driver: P
  import driver.api._
  implicit def       toByteSlickRep[T](v:       Byte @@ T): slick.lifted.Rep[Byte]       = the[Identity[Byte       @@ T]].unwrap(v)
  implicit def      toShortSlickRep[T](v:      Short @@ T): slick.lifted.Rep[Short]      = the[Identity[Short      @@ T]].unwrap(v)
  implicit def        toIntSlickRep[T](v:        Int @@ T): slick.lifted.Rep[Int]        = the[Identity[Int        @@ T]].unwrap(v)
  implicit def       toLongSlickRep[T](v:       Long @@ T): slick.lifted.Rep[Long]       = the[Identity[Long       @@ T]].unwrap(v)
  implicit def toBigDecimalSlickRep[T](v: BigDecimal @@ T): slick.lifted.Rep[BigDecimal] = the[Identity[BigDecimal @@ T]].unwrap(v)
  implicit def     toStringSlickRep[T](v:     String @@ T): slick.lifted.Rep[String]     = the[Identity[String     @@ T]].unwrap(v)
}
