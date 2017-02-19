/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.file.csv

import ixias.util.EnumOf

  sealed abstract class QuoteStyle extends Serializable
  object QuoteStyle extends EnumOf[QuoteStyle] {
    case object NONE         extends QuoteStyle
    case object MINIMAL      extends QuoteStyle
    case object NONE_NUMERIC extends QuoteStyle
    case object ALL          extends QuoteStyle
  }
