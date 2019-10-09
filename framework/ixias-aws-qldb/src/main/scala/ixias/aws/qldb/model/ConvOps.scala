/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.model

import collection.JavaConverters._
import scala.language.implicitConversions

import com.amazon.ion.IonValue
import software.amazon.qldb.{ Result => QLDBResult }

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.dataformat.ion.IonObjectMapper

// Conversion processing methods
// to support mutual conversion between Amazon Ion and ixias model.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
import ConvOps._
trait  ConvOps {

  /**
   * AmazonQLDB Result -> Model rows.
   */
  implicit def convResultToModel[M](result: QLDBResult)(implicit ctag: reflect.ClassTag[M]): Seq[M] =
    convResultToIonValue(result).map(
      (row: IonValue) => MAPPER_FOR_ION.readValue(row, ctag.runtimeClass).asInstanceOf[M]
    )

  /**
   * AmazonQLDB Result -> IonValue rows.
   */
  implicit def convResultToIonValue(result: QLDBResult): Seq[IonValue] = {
    val rows = new java.util.ArrayList[IonValue]()
    result.iterator().forEachRemaining(row => rows.add(row))
    rows.asScala
  }

  /**
   * Model data -> IonValue row data.
   */
  implicit def convAnyToIonValue[A](v: A): IonValue =
    MAPPER_FOR_ION.writeValueAsIonValue(v)

}

// Companion object
//~~~~~~~~~~~~~~~~~~
object ConvOps {

  /**
   * Mapper for Ion object.
   */
  lazy val MAPPER_FOR_ION = {
    val mapper = new IonObjectMapper()
    mapper.registerModule(new JavaTimeModule)
      .registerModule(DefaultScalaModule)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    mapper
  }
}
