/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.databind

import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.`type`.CollectionLikeType
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.Serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.module.scala.JacksonModule
import ixias.util.EnumBitFlags

// Serialize Objects of `EnumBitFlags`
// into JSON, using provided JsonGenerator.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
case class EnumBitFlagsSerializer(
  collectionType: CollectionLikeType
) extends JsonSerializer[Seq[EnumBitFlags]] {
  def serialize(value: Seq[EnumBitFlags], jgen: JsonGenerator, provider: SerializerProvider): Unit = {
    val elcls  = collectionType.getContentType.getRawClass
    val clazz  = Class.forName(elcls.getName + "$")
    val module = clazz.getField("MODULE$").get(null)
    val method = clazz.getMethod("toBitset", classOf[Seq[_]])
    val bitset = method.invoke(module, value.asInstanceOf[AnyRef])
    jgen.writeNumber(bitset.asInstanceOf[Long])
  }
}

// Resolver to serve serializer
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object EnumBitFlagsSerializerResolver extends Serializers.Base {
  private  val SYMBOL = classOf[Seq[EnumBitFlags]]
  override def findCollectionLikeSerializer(
    config:                SerializationConfig,
    collectionType:        CollectionLikeType,
    beanDescription:       BeanDescription,
    elementTypeSerializer: TypeSerializer,
    elementSerializer:     JsonSerializer[Object]
  ): JsonSerializer[_] = {
    SYMBOL isAssignableFrom collectionType.getRawClass match {
      case true  => EnumBitFlagsSerializer(collectionType)
      case false => null
    }
  }
}

// Jackson Module Definition
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~
trait EnumBitFlagsSerializerModule extends JacksonModule {
  this += { _ addSerializers EnumBitFlagsSerializerResolver }
}
