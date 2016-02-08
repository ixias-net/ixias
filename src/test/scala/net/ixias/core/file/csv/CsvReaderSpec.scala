/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.file.csv

import org.specs2.mutable.Specification

class CsvReaderSpec extends Specification {
  "CsvReader" should {
    "apply" in {
      val exists = 1
      val format = new CsvFormat{}
      val csv = CsvReader("/Users/sp1rytus/git-dev/ixias/lib.core/src/test/resources/seo_txt_city_and_town_1.tsv")(format)
      exists must_== 1
    }
  }
}
