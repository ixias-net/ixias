/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.domain.model

import org.joda.time.DateTime

trait Entity[K] {

  /** The entity's identity. */
  val id: Identity[K]

  /** The current version of the object. Used for optimistic concurrency versioning. */
  val version: Option[Long] = None

  /** The date and time when this entity was last updated. */
  val updatedAt: DateTime

  /** The date and time when this entity was added to the system. */
  val createdAt: DateTime

}
