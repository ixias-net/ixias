/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core
package port.adapter.persistence.repository

import org.joda.time.DateTime
import org.specs2.mutable.Specification
import slick.driver.MySQLDriver
import net.ixias.core.domain.model.{ Identity, Entity }

// -----------------------------------------------------------------------------
case class Test(
  val id:        Option[Test.Id],
  val label:     Int,
  val updatedAt: Option[DateTime],
  val createdAt: Option[DateTime]
) extends Entity[Test.Id]

object Test {
  case class Id(val value: Long) extends Identity[Long]
}

object TestRepository extends SlickRepository[Test.Id, Test, MySQLDriver] {
  lazy val driver = MySQLDriver
  import api._
  def get(id: Id)(implicit ctx: Context): ValidationNel[Option[Entity]] = ???
  def update(entity: Entity)(implicit ctx: Context): Unit = ???
  def remove(id: Id)(implicit ctx: Context): ValidationNel[Option[Entity]] = ???

  // --[ Definisions ]----------------------------------------------------------
  def  withMasterDB[T](f: Database => T)(implicit ctx: Context): T = withDatabase("slick.db://master/hb_test")(f)
  def withReplicaDB[T](f: Database => T)(implicit ctx: Context): T = withDatabase("slick.db://slave/hb_test")(f)
  case class LocationTextRecord(
    id:           Long,
    locationType: Short,
    location:     Int,
    size:         Short,
    value:        String  = "",
    enabled:      Boolean = false,
    updatedAt:    Option[DateTime] = None,
    createdAt:    Option[DateTime] = None
  )

  class LocationTextTable(tag: Tag) extends Table[LocationTextRecord](tag, "seo_geo_seo_text") {
    def id            = column[Long]             ("id",            O.UInt64, O.PrimaryKey, O.AutoInc)
    def locationType  = column[Short]            ("location_type", O.Int8)
    def location      = column[Int]              ("location",      O.Int32)
    def size          = column[Short]            ("size",          O.Int8)
    def value         = column[String]           ("value",         O.TEXT)
    def enabled       = column[Boolean]          ("enabled",       O.Int8)
    def updatedAt     = column[Option[DateTime]] ("updated_at",    O.TsCurrent)
    def createdAt     = column[Option[DateTime]] ("created_at",    O.Ts)
    def * = (id, locationType, location, size, value, enabled, updatedAt, createdAt) <> (LocationTextRecord.tupled, LocationTextRecord.unapply)
  }

}

// -----------------------------------------------------------------------------
class RepositorySpec extends Specification {
  "Repository" should {
    "test" in {
      val exists = true
      exists must_== true
    }
  }
}
