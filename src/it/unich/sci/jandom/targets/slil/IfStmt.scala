/**
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
 *
 * (c) 2012 Gianluca Amato
 */

package it.unich.sci.jandom
package targets.slil

import domains.NumericalProperty
import targets.Parameters
import targets.linearcondition.LinearCond
import annotations.BlackBoard

/**
 * The class for an if/then/else statement
 * @param condition the guard of the statement
 * @param then_branch the statement to execute when the guard is true
 * @param else the statement to execute when the guard is false
 */

case class IfStmt(condition: LinearCond, then_branch: SLILStmt, else_branch: SLILStmt) extends SLILStmt {
  var savedThenAnnotationStart : NumericalProperty[_] = null
  var savedThenAnnotationEnd : NumericalProperty[_] = null
  var savedElseAnnotationStart : NumericalProperty[_] = null
  var savedElseAnnotationEnd : NumericalProperty[_] = null
  
  override def analyze[Property <: NumericalProperty[Property]] (input: Property, params: Parameters[Property,SLILProgram], ann: BlackBoard[SLILProgram]): Property = {
    val thenAnnotationStart = condition.analyze(input)
    val elseAnnotationStart = condition.opposite.analyze(input)
    val thenAnnotationEnd = then_branch.analyze(thenAnnotationStart,params, ann)
    val elseAnnotationEnd = else_branch.analyze(elseAnnotationStart,params, ann)
    savedThenAnnotationStart = thenAnnotationStart
    savedThenAnnotationEnd = thenAnnotationEnd
    savedElseAnnotationStart = elseAnnotationStart
    savedElseAnnotationEnd = elseAnnotationEnd
    return thenAnnotationEnd union elseAnnotationEnd
  }
  
  override def formatString(indent: Int, indentSize: Int) : String = {  
    val spaces = " "*indentSize*indent
    val s = spaces + "if (" + condition.toString + ") {\n" + 
        (if (savedThenAnnotationStart!=null) spaces + " "*indentSize+ savedThenAnnotationStart + "\n" else "") + 
        then_branch.formatString(indent+1,indentSize) + "\n" + 
        (if (savedThenAnnotationEnd!=null) spaces + " "*indentSize+savedThenAnnotationEnd + "\n" else "") + 
      spaces +"} else {\n" + 
        (if (savedElseAnnotationStart!=null) spaces + " "*indentSize + savedElseAnnotationStart + '\n' else "") + 
        else_branch.formatString(indent+1, indentSize) +  "\n" + 
        (if (savedElseAnnotationStart!=null) spaces + " "*indentSize+  savedElseAnnotationEnd + '\n' else "") +
      spaces + '}'
    return s  
  }  
}
