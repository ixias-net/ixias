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
 * Query statement definition
 */
class SqlStatement(
  val query:  String,
  val params: Seq[IonValue]
)

// Companion object
//~~~~~~~~~~~~~~~~~~~
object SqlStatement {

  val T_BIND_PHOLD = """\?""".r
  val T_TABLE_NAME = """__TABLE_NAME__""".r

  //-- [ Methods ] -------------------------------------------------------------
  /**
   * Create `SqlStatement` object, and validate parameter's at same time.
   */
  def apply(tableName: String, query: String, params: Seq[IonValue]): SqlStatement = {
    //- Whether it is specified reserved word to replace to table's name
    if (T_TABLE_NAME.findFirstIn(query).isEmpty) {
      throw new IllegalArgumentException(
        "The required reserved word `%s` is not specified in the query"
          .format(T_TABLE_NAME.regex)
      )
    }
    //- Verification number of parameters.
    if (T_BIND_PHOLD.findAllIn(query).matchData.size != params.size) {
      throw new IllegalArgumentException(
        "The number of parameters specified for the query placeholder does not match. "
          + "query = %s, params = %s".format(query, params)
      )
    }
    new SqlStatement(T_TABLE_NAME.replaceAllIn(query, tableName), params)
  }
}
