/*
 * This file is part of the IxiaS services.
 *
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
)

// The companion object
//~~~~~~~~~~~~~~~~~~~~~~
object FileResource {
  def apply(fid:  File.Id)                          = new FileResource(fid,     None)
  def apply(file: Entity.EmbeddedId[File.Id, File]) = new FileResource(file.id, Some(file.v))
}

