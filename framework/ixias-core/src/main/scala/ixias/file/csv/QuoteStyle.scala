/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.file.csv

import ixias.util.Enum

  sealed abstract class QuoteStyle extends Enum
  object QuoteStyle extends Enum.Of[QuoteStyle] {
    case object NONE         extends QuoteStyle
    case object MINIMAL      extends QuoteStyle
    case object NONE_NUMERIC extends QuoteStyle
    case object ALL          extends QuoteStyle
  }
