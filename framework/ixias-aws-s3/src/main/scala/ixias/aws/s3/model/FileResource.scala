/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.model

import ixias.model.Entity

// The type of file resource.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~
case class FileResource(
  fid:     File.Id,
  content: Option[File]
) {
  def join(file: Option[File.EmbeddedId]): FileResource =
    this.copy(content = file.collect {
      case e if e.id == this.fid => e.v
    })

  def join(candidates: Seq[File.EmbeddedId]): FileResource =
    this.copy(content = candidates.collectFirst {
      case e if e.id == this.fid => e.v
    })
}

// The companion object
//~~~~~~~~~~~~~~~~~~~~~~
object FileResource {
  def apply(fid:  File.Id)                          = new FileResource(fid,     None)
  def apply(file: Entity.EmbeddedId[File.Id, File]) = new FileResource(file.id, Some(file.v))
}

