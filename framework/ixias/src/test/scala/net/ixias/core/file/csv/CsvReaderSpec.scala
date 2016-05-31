/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.file.csv

import scala.io.Source
import org.specs2.mutable.Specification

class CsvReaderSpec extends Specification {
  "CsvReader" should {
    "apply" in {
      val format = CsvDefaultFormat
      val source = Source.fromURL(getClass.getResource("/test.csv"))
      val csv = new CsvReader(source, format)
      csv.columns(0) must_== Seq("1", "ほげほげ\nふがふが", "100", "あああ")
      csv.columns(1) must_== Seq("2", "ふが", "ほげ", "2", "aaaaaa")
    }
  }
}
