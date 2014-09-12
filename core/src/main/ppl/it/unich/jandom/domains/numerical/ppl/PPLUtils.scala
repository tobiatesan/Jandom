/**
 * Copyright 2013 Gianluca Amato
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

package it.unich.jandom.domains.numerical.ppl

import it.unich.jandom.domains.numerical.LinearForm

import parma_polyhedra_library.Coefficient
import parma_polyhedra_library.Constraint_System
import parma_polyhedra_library.Linear_Expression
import parma_polyhedra_library.Linear_Expression_Coefficient
import parma_polyhedra_library.Linear_Expression_Variable
import parma_polyhedra_library.Partial_Function
import parma_polyhedra_library.Variable
import parma_polyhedra_library.Variable_Stringifier

/**
 * This is a collection of methods used by the PPL-based numerical domains.
 * @author Gianluca Amato <gamato@unich.it>
 */
private[jandom] object PPLUtils {
  /**
   * Converts a `LinearForm` into a pair made of a `Linear_Expression` object and a
   * `Coefficient` object, which is the denumerator to be used in linear assignments.
   * @param coeff the homogeneous coefficients.
   * @param known the in-homogeneous coefficient.
   */
  def toPPLLinearExpression(lf: LinearForm[Double]): (Linear_Expression, Coefficient) = {
    if (lf.toPPL != null)
      lf.toPPL.asInstanceOf[(Linear_Expression, Coefficient)]
    else {
      val coeffs = lf.coeffs map { BigDecimal.exact(_) }
      val maxScale = (coeffs map { _.scale }).max
      val denumerator = BigDecimal(10) pow maxScale
      val newcoeffs = coeffs map { (x: BigDecimal) => (x * denumerator).toBigIntExact.get.bigInteger }
      var le: Linear_Expression = new Linear_Expression_Coefficient(new Coefficient(newcoeffs(0)))
      for (i <- 0 until lf.dimension) {
        le = le.sum((new Linear_Expression_Variable(new Variable(i)).times(new Coefficient(newcoeffs(i + 1)))))
      }
      val result = (le, new Coefficient(denumerator.toBigIntExact.get.bigInteger))
      lf.toPPL = result
      result
    }
  }

  /**
   * Generates a string representation of a constraint system.
   * @param cs a constraint system
   * @param vars the variables to use for the string form
   */
  def constraintsToString(cs: Constraint_System, vars: Seq[String]): String = {
    import collection.JavaConversions._

    val vs = new Variable_Stringifier {
      def stringify(x: Long) = vars(x.toInt)
    }
    Variable.setStringifier(vs)
    val result = for (c <- cs) yield c.toString
    Variable.setStringifier(null)
    result.mkString("[ ", " , ", " ]")
  }

  /**
   * Converts a sequence into a partial function.
   * @param rho the original sequence. If `rho(i)=j` and `j>0`, the resulting partial
   * function maps `i` to `j`. If `j=0`, then `i` is not in the domain of the resulting
   * function.
   */
  def sequenceToPartialFunction(rho: Seq[Int]): Partial_Function = {
    val pf = new Partial_Function
    for ((newi, i) <- rho.zipWithIndex; if newi >= 0) {
      pf.insert(i, newi)
    }
    pf
  }
}