/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.lifted

import scala.language.implicitConversions
import core.domain.model.Entity
import core.port.adapter.persistence.backend.DataConverter

trait ConvertOps {
  type E <: Entity[_]
  type R <: AnyRef

  // Transform to a record from a domain model object.
  implicit def transform(value: E)(implicit f: DataConverter[E, R]): R = f.convert(value)

  // Transform to a domain model object from a record .
  implicit def transform(value: R)(implicit f: DataConverter[E, R]): E = f.convert(value)
}
