/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.model

import com.amazon.ion.IonValue

/**
 *  Typedef for Not-Nothing
 */
sealed trait NotNothing[-T]
object       NotNothing {
  implicit object NotNothing                 extends NotNothing[Any]
  implicit object YoureSupposedToSupplyAType extends NotNothing[Nothing]
}

/**
 * Preparation information for generating `SqlStatement`
 */
case class SqlPrepareStatement(
  val tableName:  String,        // Table name
  val baseQuery:  String,        // Query definition with reserved words
  val bindParams: Seq[IonValue]  // Bind params for placeholder
) {

  //-- [ Constants ] -----------------------------------------------------------
  val T_DDL_INSERT     = """INSERT""".r
  val T_DDL_BIND_PHOLD = """\?""".r
  val T_TABLE_NAME     = """__TABLE_NAME__""".r

  //-- [ Methods ] -------------------------------------------------------------
  /**
   * Fixied `SqlStatement`
   */
  def affectedDocs = {
    implicit val mctag = reflect.classTag[AffectedDocument]
    SqlStatement.ForMultiResult(query, bindParams)
  }

  /**
   * Fixied `SqlStatement`
   */
  def result[A: NotNothing](implicit ctag: reflect.ClassTag[A]) =
    SqlStatement.ForMultiResult(query,  bindParams)

  /**
   * Get query to be execution.
   */
  def query = {
    // Whether it is specified reserved word to replace to table's name
    if (T_TABLE_NAME.findFirstIn(baseQuery).isEmpty) {
      throw new IllegalArgumentException(
        "The required reserved word `%s` is not specified in the baseQuery"
          .format(T_TABLE_NAME.regex)
      )
    }
    // Verification number of parameters.
    if (
      T_DDL_INSERT.findFirstIn(baseQuery).isEmpty &&
      T_DDL_BIND_PHOLD.findAllIn(baseQuery).matchData.size != bindParams.size
    ) {
      throw new IllegalArgumentException(
        "The number of parameters specified for the baseQuery placeholder does not match. "
          + "baseQuery = %s, params = %s".format(baseQuery, bindParams)
      )
    }
    // Build query
    T_TABLE_NAME.replaceAllIn(baseQuery, tableName)
  }
}
