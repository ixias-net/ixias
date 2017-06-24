/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.scalate.mvc

import play.api.PlayException

final class JadeCompilationException(
  override val sourceName: String,
  override val input:      String,
  override val line:       Integer,
  override val position:   Integer,
  message: String,
  cause:   Throwable
) extends PlayException.ExceptionSource("scalate compilation exception", message, cause)
