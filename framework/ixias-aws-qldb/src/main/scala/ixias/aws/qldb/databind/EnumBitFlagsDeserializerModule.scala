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
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer
import com.fasterxml.jackson.databind.`type`.CollectionLikeType
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.scala.JacksonModule
import ixias.util.EnumBitFlags

// Deserialize Objects of `EnumBitFlags`
// from JSON, using provided JsonParser.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
case class EnumBitFlagsDeserializer(
  collectionType: CollectionLikeType
) extends JsonDeserializer[Seq[EnumBitFlags]] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): Seq[EnumBitFlags] = {
    val elcls  = collectionType.getContentType.getRawClass
    val clazz  = Class.forName(elcls.getName + "$")
    val module = clazz.getField("MODULE$").get(null)
    val method = clazz.getMethod("apply", classOf[Long])
    val enum   = method.invoke(module, p.getValueAsLong.asInstanceOf[AnyRef])
    enum.asInstanceOf[Seq[EnumBitFlags]]
  }
}

// Resolver to serve deserializer
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object EnumBitFlagsDeserializerResolver extends Deserializers.Base {
  private  val SYMBOL  = classOf[Seq[_]]
  private  val CONTENT = classOf[EnumBitFlags]
  override def findCollectionLikeDeserializer(
    collectionType:          CollectionLikeType,
    config:                  DeserializationConfig,
    beanDesc:                BeanDescription,
    elementTypeDeserializer: TypeDeserializer,
    elementDeserializer:     JsonDeserializer[_]
  ): JsonDeserializer[_] = (
    (SYMBOL  isAssignableFrom collectionType.getRawClass) &&
    (CONTENT isAssignableFrom collectionType.getContentType.getRawClass)
  ) match {
    case true  => EnumBitFlagsDeserializer(collectionType)
    case false => null
  }
}

// Jackson Module Definition
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~
trait EnumBitFlagsDeserializerModule extends JacksonModule {
  this += { _ addDeserializers EnumBitFlagsDeserializerResolver }
}
