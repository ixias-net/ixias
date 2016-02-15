/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.lifted

import scala.language.implicitConversions
import core.port.adapter.persistence.model.Converter

trait ConverterOps {
  implicit def convert[A, B](o: A)(implicit conv: Converter[A, B]): B = conv.convert(o)
}
