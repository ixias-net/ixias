/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.databind

import com.fasterxml.jackson.module.scala.JacksonModule

object DatabindModule extends JacksonModule
    with EnumBitFlagsSerializerModule
    with EnumBitFlagsDeserializerModule
    with EnumStatusSerializerModule
    with EnumStatusDeserializerModule
{
  override def getModuleName = "DatabindModule"
}
