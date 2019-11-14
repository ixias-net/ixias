/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.model

import ixias.model._

/**
 * Definition to execute a query from the target table
 */
abstract class TableQuery[K <: Document.Id[_], M <: EntityModel[K]]
  (val tableName: String)(implicit ctag: reflect.ClassTag[M]) extends ConvOps {

  // -- [ Basic queries ] ---------------------------------
  lazy val get = (id: M#Id) =>
    sql("SELECT id, t.* FROM __TABLE_NAME__ AS t BY id WHERE id = ?", id)
      .result[M].headOption

  lazy val add = (data: M#WithNoId) =>
    sql("INSERT INTO __TABLE_NAME__ VALUE ?", data.v)
      .affectedDocs.headOption

  lazy val delete = (id: M#Id) =>
    sql("DELETE FROM __TABLE_NAME__ BY id WHERE id = ?", id)
      .affectedDocs.headOption

  lazy val update = (data: M#EmbeddedId) =>
    sql("UPDATE __TABLE_NAME__ AS t BY id SET t = ? WHERE t != ? AND id = ?", data.v, data.v, data.id)
      .affectedDocs.headOption

  // -- [ Methods to create statement object ] ---------------------------------
  def sql
    (stmt: String)
      = SqlPrepareStatement(tableName, stmt, Seq.empty)
  def sql[P1]
    (stmt: String, p1: P1)
      = SqlPrepareStatement(tableName, stmt, Seq(p1))
  def sql[P1, P2]
    (stmt: String, p1: P1, p2: P2)
      = SqlPrepareStatement(tableName, stmt, Seq(p1, p2))
  def sql[P1, P2, P3]
    (stmt: String, p1: P1, p2: P2, p3: P3)
      = SqlPrepareStatement(tableName, stmt, Seq(p1, p2, p3))
  def sql[P1, P2, P3, P4]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4)
      = SqlPrepareStatement(tableName, stmt, Seq(p1, p2, p3, p4))
  def sql[P1, P2, P3, P4, P5]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5)
      = SqlPrepareStatement(tableName, stmt, Seq(p1, p2, p3, p4, p5))
  def sql[P1, P2, P3, P4, P5, P6]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6)
      = SqlPrepareStatement(tableName, stmt, Seq(p1, p2, p3, p4, p5, p6))
  def sql[P1, P2, P3, P4, P5, P6, P7]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7)
      = SqlPrepareStatement(tableName, stmt, Seq(p1, p2, p3, p4, p5, p6, p7))
  def sql[P1, P2, P3, P4, P5, P6, P7, P8]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8)
      = SqlPrepareStatement(tableName, stmt, Seq(p1, p2, p3, p4, p5, p6, p7, p8))
  def sql[P1, P2, P3, P4, P5, P6, P7, P8, P9]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9)
      = SqlPrepareStatement(tableName, stmt, Seq(p1, p2, p3, p4, p5, p6, p7, p8, p9))
}
