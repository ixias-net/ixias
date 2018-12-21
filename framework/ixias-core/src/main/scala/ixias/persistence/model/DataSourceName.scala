/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.model

/**
 * The DSN(Data-Source-Name) structure.
 */
case class DataSourceName(
  val path:     String,
  val hostspec: String,
  val database: String
) {
  /** Compares two DSN structurally */
  override final def equals(other: Any): Boolean = other match {
    case that: DataSourceName => {
      (that _equal this) &&
      (this.path     == that.path)     &&
      (this.hostspec == that.hostspec) &&
      (this.database == that.database)
    }
    case _ => false
  }
  private def _equal(other: Any) = other.isInstanceOf[DataSourceName]

  /** Returns the hash code for this `DataSourceName`. */
  override final def hashCode: Int =
    31 ^ 3 * path.## + 31 ^ 2 * hostspec.## + 31 ^ 1 * database.##

  /** The String representation of the `DataSourceName` companion object. */
  override final def toString: String = "%s://%s/%s".format(path, hostspec, database)
}

/**
 * Conpanion object.
 */
object DataSourceName {

  /** The synatx format for DSN */
  val SYNTAX_DATA_SOURCE_NAME = """^([.\w]+)://(\w+?)/(\w+)$""".r

  val RESERVED_NAME_MASTER    = "master"
  val RESERVED_NAME_SLAVE     = "slave"

  /** Build a `DataSourceName` object. */
  def apply(dsn: String): DataSourceName = dsn match {
    case SYNTAX_DATA_SOURCE_NAME(p1, p2, p3) => DataSourceName(p1, p2, p3)
    case _ => throw new Exception(s"""Dose not match the DSN format. ($dsn)""")
  }
}
