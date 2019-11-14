/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.model

import ixias.model._

// Affected document infomation
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
case class AffectedDocument(
  documentId: AffectedDocument.Id
)

object AffectedDocument {
  type Id = Document.Id[AffectedDocument]
  val  Id = the[Identity[Id]]
}

// typedef for Document
//~~~~~~~~~~~~~~~~~~~~~~
trait  Document
object Document {

  /**
   * Provide definition of Document-Id
   */
  type Id[T] = String @@ (Document, T)
}
