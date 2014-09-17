/**
 * Copyright 2014 Gianluca Amato
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

package it.unich.jandom.domains.numerical

import org.scalatest.FunSpec

import it.unich.jandom.domains.{EmptyExistsSuite, SeparatedTopAndBottomSuite}

/**
 * Test suite for the sum of interval and parallelotope domains.
 * @author Gianluca Amato <gamato@unich.it>
 */
class SumIntParallelotopeDomainSuite extends FunSpec  with NumericalDomainSuite with SeparatedTopAndBottomSuite with EmptyExistsSuite {
  lazy val dom = SumIntParallelotopeDomain()
}
