/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.model

import scala.language.implicitConversions
import com.amazon.ion.IonValue
import software.amazon.qldb.{ Result => QldbResult }
import ixias.aws.qldb.databind.DatabindModule

// Conversion processing methods
// to support mutual conversion between Amazon Ion and model.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
import ConvOps._
trait  ConvOps {

  //-- [ To Amazon Ion ] -------------------------------------------------------
  /**
   * Model -> Single IonValue value.
   */
  implicit def convToIonValue[A](v: A): IonValue =
    MAPPER_FOR_ION.writeValueAsIonValue(v)

  /**
   * Single IonValue value -> Model
   */
  implicit def convToModel[A](v: IonValue)(implicit ctag: reflect.ClassTag[A]): A =
    MAPPER_FOR_ION.readValue(v, ctag.runtimeClass).asInstanceOf[A]

  //-- [ From AmazonQldb Result ] ----------------------------------------------
  implicit def toQldbResultTransformer(v: QldbResult) =
    QldbResultTransformer(v)
}

// Companion object
//~~~~~~~~~~~~~~~~~~
object ConvOps {
  import com.fasterxml.jackson.annotation.JsonInclude
  import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
  import com.fasterxml.jackson.dataformat.ion.IonObjectMapper
  import com.fasterxml.jackson.databind.{ SerializationFeature, DeserializationFeature }
  import com.fasterxml.jackson.module.scala.DefaultScalaModule

  /** Mapper for AmazonIon object. */
  lazy val MAPPER_FOR_ION = {
    val mapper = new IonObjectMapper()
    mapper.registerModule(new JavaTimeModule)
      .registerModule(DefaultScalaModule)
      .registerModule(DatabindModule)
      .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    mapper
  }
}

// Transformer for QldbResult
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
case class QldbResultTransformer(self: QldbResult) extends AnyVal {
  import ConvOps.MAPPER_FOR_ION

  /** To Seq[Model] */
  def toModelSeq[A](implicit ctag: reflect.ClassTag[A]): Seq[A] = {
    val cls = ctag.runtimeClass
    toIonValueSeq.map(
      MAPPER_FOR_ION.readValue(_, cls).asInstanceOf[A]
    )
  }

  /**
   * AmazonQLDB Result -> IonValue rows.
   */
  def toIonValueSeq: Seq[IonValue] = {
    import collection.JavaConverters._
    val list = new java.util.ArrayList[IonValue]()
    self.iterator().forEachRemaining(v => list.add(v))
    Seq(list.asScala: _*)
  }
}
