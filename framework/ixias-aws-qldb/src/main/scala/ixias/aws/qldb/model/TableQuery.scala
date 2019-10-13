/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.model

/**
 * Definition to execute a query from the target table
 */
abstract class TableQuery(val tableName: String) extends ConvOps {

  /** Methods to create statement object */
  def sql
    (stmt: String)
      = SqlStatement(tableName, stmt, Seq.empty)
  def sql[P1]
    (stmt: String, p1: P1)
      = SqlStatement(tableName, stmt, Seq(p1))
  def sql[P1, P2]
    (stmt: String, p1: P1, p2: P2)
      = SqlStatement(tableName, stmt, Seq(p1, p2))
  def sql[P1, P2, P3]
    (stmt: String, p1: P1, p2: P2, p3: P3)
      = SqlStatement(tableName, stmt, Seq(p1, p2, p3))
  def sql[P1, P2, P3, P4]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4)
      = SqlStatement(tableName, stmt, Seq(p1, p2, p3, p4))
  def sql[P1, P2, P3, P4, P5]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5)
      = SqlStatement(tableName, stmt, Seq(p1, p2, p3, p4, p5))
  def sql[P1, P2, P3, P4, P5, P6]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6)
      = SqlStatement(tableName, stmt, Seq(p1, p2, p3, p4, p5, p6))
  def sql[P1, P2, P3, P4, P5, P6, P7]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7)
      = SqlStatement(tableName, stmt, Seq(p1, p2, p3, p4, p5, p6, p7))
  def sql[P1, P2, P3, P4, P5, P6, P7, P8]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8)
      = SqlStatement(tableName, stmt, Seq(p1, p2, p3, p4, p5, p6, p7, p8))
  def sql[P1, P2, P3, P4, P5, P6, P7, P8, P9]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9)
      = SqlStatement(tableName, stmt, Seq(p1, p2, p3, p4, p5, p6, p7, p8, p9))
}
