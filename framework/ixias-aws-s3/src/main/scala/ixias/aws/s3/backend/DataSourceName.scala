/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.backend

/**
 * The DSN(Data-Source-Name) structure.
 */
sealed class DataSourceName(
  val path:     String,
  val resource: String
)

// Conpanion object.
//~~~~~~~~~~~~~~~~~~~~~~~~~
object DataSourceName {

  /** The synatx format for DSN */
  val SYNTAX_DATA_SOURCE_NAME = """^(ixias.aws.s3)://(\w+?)$""".r

  /** Build a `DataSourceName` object. */
  def apply(dsn: String) = dsn match {
    case SYNTAX_DATA_SOURCE_NAME(p1, p2) => new DataSourceName(p1, p2)
    case _ => throw new Exception(s"""Dose not match the DSN format. ($dsn)""")
  }
}
