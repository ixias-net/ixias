/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.persistence

import java.time.LocalDateTime
import slick.jdbc.JdbcProfile
import ixias.aws.s3.model.File
import ixias.aws.s3.backend.{ DataSourceName => S3DSN, AmazonS3Config }
import ixias.persistence.model.Table

// Table definition.
//~~~~~~~~~~~~~~~~~~~~
case class FileTable[P <: JdbcProfile]()(implicit val driver: P, val s3dsn: S3DSN)
    extends Table[File, P] with AmazonS3Config { self =>
  import apiUnsafe._

  // --[ DNS ] -----------------------------------------------------------------
  lazy val dsn = Map(
    "master" -> DataSourceName("ixias.db.mysql://master/" + s3dsn.resource),
    "slave"  -> DataSourceName("ixias.db.mysql://slave/"  + s3dsn.resource)
  )

  // --[ Query ] ---------------------------------------------------------------
  class Query extends BasicQuery(new Table(_)) {
    def unique(fid: File.Id) =
      this.filter(_.id === fid)
  }
  lazy val query = new Query

  // --[ Table definition ] ----------------------------------------------------
  class Table(tag: Tag) extends BasicTable(tag, getMetaTableName) {

    // Columns
    /* @1 */ def id        = column[Option[Long]]  ("id",         O.UInt64, O.PrimaryKey, O.AutoInc)
    /* @2 */ def region    = column[String]        ("region",     O.Utf8Char32)
    /* @3 */ def bucket    = column[String]        ("bucket",     O.Utf8Char32)
    /* @4 */ def key       = column[String]        ("key",        O.Utf8Char255)
    /* @5 */ def typedef   = column[String]        ("typedef",    O.Utf8Char32)
    /* @6 */ def width     = column[Option[Int]]   ("width",      O.UInt16)
    /* @7 */ def height    = column[Option[Int]]   ("height",     O.UInt16)
    /* @8 */ def updatedAt = column[LocalDateTime] ("updated_at", O.TsCurrent)
    /* @9 */ def createdAt = column[LocalDateTime] ("created_at", O.Ts)

    // Indexes
    def ukey01 = index("key01", (bucket, key, region), unique = true)

    // All columns as a tuple
    import File._
    type TableElementTuple = (
      Option[Long], String, String, String, String,
      Option[Int], Option[Int], LocalDateTime, LocalDateTime
    )

    // The * projection of the table
    def * = (id, region, bucket, key, typedef, width, height, updatedAt, createdAt) <> (
      /* The bidirectional mappings : Tuple(table) => Model */
      (t: TableElementTuple) => {
        val imageSize = (t._6, t._7) match {
          case (Some(width), Some(height)) => Some(ImageSize(width, height))
          case _                           => None
        }
        File(Some(Id(t._1.get)), t._2, t._3, t._4, t._5, imageSize, None, t._8, t._9)
      },
      /* The bidirectional mappings : Model => Tuple(table) */
      (v: TableElementType)  => File.unapply(v).map { t => (
        v.id, t._2, t._3, t._4, t._5, t._6.map(_.width), t._6.map(_.height), LocalDateTime.now(), t._9
      ) }
    )
  }
}
