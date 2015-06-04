/**
 * Copyright 2015 Gianluca Amato <gamato@unich.it>
 *
 * This file is part of JANDOM: JVM-based Analyzer for Numerical DOMains
 * JANDOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JANDOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty ofa
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JANDOM.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unich.jandom.fixpoint

import org.scalatest.prop.PropertyChecks
import org.scalatest.FunSpec
import org.scalacheck.Gen

class BoxSuite extends FunSpec with PropertyChecks {
  describe("A functional box") {
    val f = { (x: Int, y: Int) => x + y }
    val box = Box(f)
    it("returns the same value as the function") {
      forAll { (x: Int, y: Int) =>
        assertResult(f(x, y))(box(x, y))
      }
    }
  }

  describe("A left box") {
    val box = Box.left[Int]
    it("returns the first element") {
      forAll { (x: Int, y: Int) =>
        assertResult(x)(box(x, y))
      }
    }
  }

  describe("A right box") {
    val box = Box.right[Int]
    it("returns the second element") {
      forAll { (x: Int, y: Int) =>
        assertResult(y)(box(x, y))
      }
    }
  }

  describe("A cascade box") {
    it("behaves as the first box for a given number of applications, as the second box for later applications") {
      val gen = Gen.choose(0, 10)
      forAll(gen) { (d: Int) =>
        forAll { (x: Int, y: Int) =>
          val box = Box.cascade(Box.left[Int], d, Box.right[Int])
          for (i <- 0 until d)
            assertResult(x)(box(x, y))
          for (i <- 0 until 5)
            assertResult(y)(box(x, y))
        }
      }
    }
  }
}
