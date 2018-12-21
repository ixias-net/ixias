/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.backend

/**
 * The DSN(Data-Source-Name) structure.
 */
sealed class DataSourceName(
  val path:     String,
  val resource: String,
  val name:     Option[String]
)

// Conpanion object.
//~~~~~~~~~~~~~~~~~~~~~~~~~
object DataSourceName {

  /** The synatx format for DSN */
  val SYNTAX_DATA_SOURCE_NAME1 = """^(ixias.aws.s3)://(\w+?)$""".r
  val SYNTAX_DATA_SOURCE_NAME2 = """^(ixias.aws.s3)://(\w+?)/(\w+)$""".r

  /** Build a `DataSourceName` object. */
  def apply(dsn: String) = dsn match {
    case SYNTAX_DATA_SOURCE_NAME1(p1, p2)     => new DataSourceName(p1, p2, None)
    case SYNTAX_DATA_SOURCE_NAME2(p1, p2, p3) => new DataSourceName(p1, p2, Some(p3))
    case _ => throw new Exception(s"""Dose not match the DSN format. ($dsn)""")
  }
}
