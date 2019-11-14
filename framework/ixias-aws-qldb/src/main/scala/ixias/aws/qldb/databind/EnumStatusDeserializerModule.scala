/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.databind

import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.scala.JacksonModule
import ixias.util.EnumStatus

// Deserialize Objects of `EnumStatus`
// from JSON, using provided JsonParser.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
case class EnumStatusDeserializer(
  javaType: JavaType
) extends JsonDeserializer[EnumStatus] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): EnumStatus = {
    val clazz  = Class.forName(javaType.getRawClass.getName + "$")
    val module = clazz.getField("MODULE$").get(null)
    val method = clazz.getMethod("apply", classOf[Short])
    val enum   = method.invoke(module, p.getValueAsInt.toShort.asInstanceOf[AnyRef])
    enum.asInstanceOf[EnumStatus]
  }
}

// Resolver to serve deserializer
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object EnumStatusDeserializerResolver extends Deserializers.Base {
  private  val SYMBOL = classOf[EnumStatus]
  override def findBeanDeserializer(
    javaType: JavaType,
    config:   DeserializationConfig,
    beanDesc: BeanDescription
  ): JsonDeserializer[_] = {
    if (SYMBOL isAssignableFrom javaType.getRawClass)
      EnumStatusDeserializer(javaType)
    else null
  }
}

// Jackson Module Definition
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~
trait EnumStatusDeserializerModule extends JacksonModule {
  this += { _ addDeserializers EnumStatusDeserializerResolver }
}
