/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import core.domain.model.Entity

/**
 * Represents a type class that needs to be implemented
 * for conversion to work.
 */
trait DataConverter[E <: Entity[_], R] {
  def convert(value: E): R
  def convert(value: R): E
}
