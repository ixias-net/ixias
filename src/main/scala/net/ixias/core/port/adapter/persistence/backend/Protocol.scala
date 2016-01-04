/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import core.util.EnumOf

/** The protocol types used for running IOAction. */
sealed abstract class Protocol
object Protocol extends EnumOf[Protocol] {
  case object TCP     extends Protocol
  case object UDP     extends Protocol
  case object Unix    extends Protocol
  case object Unknown extends Protocol
}
