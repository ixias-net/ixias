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

// typedef for Document
//~~~~~~~~~~~~~~~~~~~~~~
object AffectedDocument {

  // --[ New Types ]------------------------------------------------------------
  type Id = String @@ AffectedDocument
  val  Id = the[Identity[Id]]
}
