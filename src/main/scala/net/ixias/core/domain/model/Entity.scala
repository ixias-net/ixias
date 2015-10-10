/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.core
package domain.model

import org.joda.time.DateTime

/** Used to manage surrogate identity and optimistic concurrency versioning */
trait Entity[ID <: Identity[_]] {

  /** The entity's identity. */
  val id: Option[ID]

  /** The current version of the object. Used for optimistic concurrency versioning. */
  val version: Long = -1L

  /** The version converted to a Option. */
  val versionOpt = if (version < 0) None else Some(version)

  /** The date and time when this entity was last updated. */
  val updatedAt: Option[DateTime]

  /** The date and time when this entity was added to the system. */
  val createdAt: Option[DateTime]

}
