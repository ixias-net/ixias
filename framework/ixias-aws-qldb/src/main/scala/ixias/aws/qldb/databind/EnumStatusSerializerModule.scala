/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.databind

import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.module.scala.JacksonModule
import ixias.util.EnumStatus

// Serialize Objects of `EnumStatus`
// into JSON, using provided JsonGenerator.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object EnumStatusSerializer extends JsonSerializer[EnumStatus] {
  def serialize(value: EnumStatus, jgen: JsonGenerator, provider: SerializerProvider): Unit =
    jgen.writeNumber(value.code)
}

// Resolver to serve serializer
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object EnumStatusSerializerResolver extends Serializers.Base {
  private  val SYMBOL = classOf[EnumStatus]
  override def findSerializer(
    config:   SerializationConfig,
    javaType: JavaType,
    beanDesc: BeanDescription
  ): JsonSerializer[_] =
    SYMBOL isAssignableFrom javaType.getRawClass match {
      case true  => EnumStatusSerializer
      case false => null
    }
}

// Jackson Module Definition
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~
trait EnumStatusSerializerModule extends JacksonModule {
  this += { _ addSerializers EnumStatusSerializerResolver }
}
