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

package it.unich.sci.jandom.domains.numerical

/**
 * This is the box abstract domain over doubles.
 *
 * It is actually a proof of concept on how to implement an abstract domain entirely in Scala. It
 * is not safe to use, since it does not implement rounding correctly. Real domains should be implemented
 * within $PPL or $APRON.
 * @author Gianluca Amato <amato@sci.unich.it>
 *
 * @constructor Creates a `BoxDouble` with given lower and upper bounds.
 * @param low the lower bounds for the box.
 * @param high the upper bounds for the box.
 * @note `low` and `high` should be normalized according to the method `normalized`
 * @throws IllegalArgumentException if parameters are not correct.
 * @define ROUNDING this is not implemented correctly since it does not control rounding errors.
 */

final class BoxDouble(private[domains] val low: Array[Double], private[domains] val high: Array[Double]) extends NumericalProperty[BoxDouble] {
  require(normalized, "The parameters low:" + low.mkString(",") + " and high: " + high.mkString(",") + " are not normalized")

  /**
   * This checks whether the box is normalized. This should always be the case. A box is normalized when
   * the lower and higher bounds are of the same length, and either
   *   1. there are no lower bounds equal to +Inf,
   *      there are no upper bounds equal to -Inf, the lower bounds are smaller of the corresponding upper bounds or
   *   2. all the lower bound are +Inf and all the upper bounds are -Inf.
   * @return whether the box is normalized.
   */
  private def normalized: Boolean =
    low.length == high.length &&
      (
        low.forall { (x) => !(x.isPosInfinity) } &&
        high.forall { (x) => !(x.isNegInfinity) } &&
        (low, high).zipped.forall(_ <= _)
        ||
        low.forall { _.isPosInfinity } &&
        high.forall { _.isNegInfinity })

  /**
   * Returns the sum of `x` and `y`, rounded towards +Inf.
   * @todo $ROUNDING
   */
  private def add_hi(x: Double, y: Double): Double = x + y

  /**
   * Returns the sum of `x` and `y`, rounded towards -Inf.
   * @todo $ROUNDING
   */
  private def add_lo(x: Double, y: Double): Double = x + y

  /**
   * Returns the product of `x` and `y`, rounded towards +Inf. Moreover,
   * if `x` is `0`, the product is `0` independently from the value of `y`.
   * @todo $ROUNDING
   */
  private def mul_hi(x: Double, y: Double): Double = if (x == 0) 0 else x * y

  /**
   * Returns the product of `x` and `y`, rounded towards -Inf. Moreover,
   * if `x` is `0`, the product is `0` independently from the value of `y`.
   * @todo $ROUNDING
   */
  private def mul_lo(x: Double, y: Double): Double = if (x == 0) 0 else x * y

  /**
   * Return the dot product of `x` and `y`, rounded towards `-Inf`. If element `x(i)` is
   * zero, then `x(i)*y(i)` is `0` independently from the value of `y(i)`.
   * If `remove` is a valid index in `x` and `y`, the factor `x(remove) * y(remove)` is
   * removed from the dot product.
   * @todo $ROUNDING
   */
  private def dotprod_lo(x: Seq[Double], y: Seq[Double], remove: Int = -1): Double = {
    var sum: Double = 0
    for (i <- x.indices) if (i != remove) sum = add_lo(sum, mul_lo(x(i), y(i)))
    return sum
  }

  /**
   * Return the dot product of `x` and `y`, rounded towards `+Inf`. If element `x(i)` is
   * zero, then `x(i)*y(i)` is `0` independently from the value of `y(i)`.
   * If `remove` is a valid index in `x` and `y`, the factor `x(remove) * y(remove)` is
   * removed from the dot product.
   * @todo $ROUNDING
   */
  private def dotprod_hi(x: Seq[Double], y: Seq[Double], remove: Int = -1): Double = {
    var sum: Double = 0
    for (i <- x.indices) if (i != remove) sum = add_hi(sum, mul_hi(x(i), y(i)))
    return sum
  }

  /**
   * @inheritdoc
   * @note @inheritdoc
   * @throws $ILLEGAL
   */
  def union(that: BoxDouble): BoxDouble = {
    require(dimension == that.dimension)
    val newlow = (this.low, that.low).zipped.map(_ min _)
    val newhigh = (this.high, that.high).zipped.map(_ max _)
    new BoxDouble(newlow, newhigh)
  }

  /**
   * @inheritdoc
   * @note @inheritdoc
   * @throws $ILLEGAL
   */
  def intersection(that: BoxDouble): BoxDouble = {
    require(dimension == that.dimension)
    val newlow = (this.low, that.low).zipped.map(_ max _)
    val newhigh = (this.high, that.high).zipped.map(_ min _)
    new BoxDouble(newlow, newhigh)
  }

  /**
   * This is the standard widening on boxes based on [[http://www.di.ens.fr/~cousot/COUSOTpapers/ISOP76.shtml CC76]].
   * @note @inheritdoc
   * @throws $ILLEGAL
   */
  def widening(that: BoxDouble) = {
    require(dimension == that.dimension)
    val newlow = (low, that.low).zipped.map((l1, l2) => if (l1 == Double.PositiveInfinity) l2 else if (l1 <= l2) l1 else Double.NegativeInfinity)
    val newhigh = (high, that.high).zipped.map((l1, l2) => if (l1 == Double.NegativeInfinity) l2 else if (l1 >= l2) l1 else Double.PositiveInfinity)
    new BoxDouble(newlow, newhigh)
  }

  /**
   * This is the standard narrowing on boxes based on [[http://www.di.ens.fr/~cousot/COUSOTpapers/ISOP76.shtml CC76]].
   * @note @inheritdoc
   * @throws $ILLEGAL
   */
  def narrowing(that: BoxDouble) = {
    require(dimension == that.dimension)
    val newlow = (low, that.low).zipped.map((l1, l2) => if (l1 == Double.NegativeInfinity) l2 else l1 min l2)
    val newhigh = (high, that.high).zipped.map((l1, l2) => if (l1 == Double.PositiveInfinity) l2 else l1 max l2)
    new BoxDouble(newlow, newhigh)
  }

  /**
   * Compute the minimum and maximum value of a linear form in a box.
   * @todo should be generalized to linear forms over arbitrary types.
   * @return a tuple with two components: the first component is the least value, the second component is the greatest value
   * of the linear form over the box.
   */
  def linearEvaluation(lf: LinearForm[Double]): (Double, Double) = {
    require(lf.dimension <= dimension)
    var newlow: Double = lf.known
    var newhigh: Double = lf.known
    val coeffs = lf.homcoeffs
    for (i <- 0 until coeffs.length) {
      if (coeffs(i) < 0) {
        newlow = add_lo(newlow, mul_lo(coeffs(i), high(i)))
        newhigh = add_hi(newhigh, mul_hi(coeffs(i), low(i)))
      } else {
        newlow = add_lo(newlow, mul_lo(coeffs(i), low(i)))
        newhigh = add_hi(newhigh, mul_hi(coeffs(i), high(i)))
      }
    }
    (newlow, newhigh)
  }

  def minimize(lf: LinearForm[Double]) = linearEvaluation(lf)._1

  def maximize(lf: LinearForm[Double]) = linearEvaluation(lf)._2

  def frequency(lf: LinearForm[Double]) = {
    val (min, max) = linearEvaluation(lf)
    if (min == max) Some(min) else None
  }

  /**
   * Compute the corner of the box which minimizes a linear form.
   * todo should be generalized to linear forms over arbitrary types.
   * @param coeff the homogeneous coefficients.
   * @return the coordinates of the point which minimizes the linear form.
   */
  private def linearArgmin(lf: LinearForm[Double]): Seq[Double] = {
    require(lf.dimension <= dimension)
    (lf.homcoeffs.zipWithIndex) map { case (c, i) => if (c > 0) low(i) else high(i) }
  }

  /**
   * Compute the corner of the box which maximizes a linear form.
   * @todo should be generalized to linear forms over arbitrary types.
   * @param coeff the homogeneous coefficients
   * @return the coordinates of the point which maximizes the linear form
   */
  private def linearArgmax(lf: LinearForm[Double]): Seq[Double] = {
    require(lf.dimension <= dimension)
    (lf.homcoeffs.zipWithIndex) map { case (c, i) => if (c < 0) low(i) else high(i) }
  }

  /**
   * @inheritdoc
   * @note @inheritdoc
   * @throws $ILLEGAL
   */
  def nonDeterministicAssignment(n: Int): BoxDouble = {
    require(n < low.length && n >= 0)
    new BoxDouble(low.updated(n, Double.NegativeInfinity), high.updated(n, Double.PositiveInfinity))
  }

  /**
   * @inheritdoc
   * @note @inheritdoc
   * @todo @inheritdoc
   * @throws $ILLEGAL
   */
  def linearAssignment(n: Int, lf: LinearForm[Double]): BoxDouble = {
    require(n < low.length && n >= 0 && lf.dimension <= dimension)
    if (isEmpty) return this
    val interval = linearEvaluation(lf)
    new BoxDouble(low.updated(n, interval._1), high.updated(n, interval._2))
  }

  /**
   * @inheritdoc
   * @note @inheritdoc
   * @todo @inheritdoc
   * @throws $ILLEGAL
   */
  def linearInequality( lf: LinearForm[Double]): BoxDouble = {
    require(lf.dimension <= dimension)

    /* if the box is empty the result is empty */
    if (isEmpty) return this

    /* check if result is empty */
    val lfArgmin = linearArgmin(lf)
    val lfMin = linearEvaluation(lf)._1
    if (lfMin > 0) return BoxDouble.bottom(dimension)

    val newlow = low.clone
    val newhigh = high.clone

    val coeffs = lf.homcoeffs
    val known = lf.known

    val infinities = (0 until coeffs.length) filter { i => lfArgmin(i).isInfinity && coeffs(i) != 0 }
    infinities.size match {
      case 0 =>
        for (i <- 0 until coeffs.length) {
          if (coeffs(i) < 0) newlow(i) = low(i) max (lfArgmin(i) - lfMin / coeffs(i))
          if (coeffs(i) > 0) newhigh(i) = high(i) min (lfArgmin(i) - lfMin / coeffs(i))
        }
      case 1 => {
        val posinf = infinities.head
        if (coeffs(posinf) < 0)
          newlow(posinf) = low(posinf) max ((-dotprod_lo(coeffs, lfArgmin, posinf) - known) / coeffs(posinf))
        else
          newhigh(posinf) = high(posinf) min ((-dotprod_hi(coeffs, lfArgmin, posinf) - known) / coeffs(posinf))
      }
      case _ =>
    }
    new BoxDouble(newlow, newhigh)
  }

  /**
   * @inheritdoc
   * @note @inheritdoc
   * @throws $ILLEGAL
   */
  def linearDisequality(lf: LinearForm[Double]): BoxDouble = {
    val count = lf.homcoeffs.count(_ != 0)
    count match {
      case 0 =>
        if (lf.known == 0) bottom else this
      case 1 =>
        val dim = lf.homcoeffs.indexWhere(_ != 0)
        if (low(dim) == lf.known && high(dim) == lf.known)
          bottom
        else
          this
      case _ => this
    }
  }

  /*
  def addVariable(v: Int): BoxDouble = {
    require(0 <= v && v <= dimension)
    if (isEmpty)
      BoxDouble.empty(dimension + 1)
    else if (v == dimension)
      new BoxDouble(low :+ Double.NegativeInfinity, high :+ Double.PositiveInfinity)
    else
      new BoxDouble((low.take(v) :+ Double.NegativeInfinity) ++ low.takeRight(dimension-v),
  	                (high.take(v) :+ Double.PositiveInfinity) ++ high.takeRight(dimension-v))
  }
*/

  def addVariable: BoxDouble =
    if (isEmpty)
      BoxDouble.bottom(dimension + 1)
    else
      new BoxDouble(low :+ Double.NegativeInfinity, high :+ Double.PositiveInfinity)

  /**
   * @inheritdoc
   * This is a complete operator for boxes.
   * @note @inheritdoc
   * @throws $ILLEGAL
   */
  def delVariable(n: Int): BoxDouble = {
    require(n < low.length && n >= 0)
    val newlow = new Array[Double](dimension - 1)
    val newhigh = new Array[Double](dimension - 1)
    Array.copy(low, 0, newlow, 0, n)
    Array.copy(high, 0, newhigh, 0, n)
    Array.copy(low, n + 1, newlow, n, dimension - n - 1)
    Array.copy(high, n + 1, newhigh, n, dimension - n - 1)
    new BoxDouble(newlow, newhigh)
  }

  /**
   * @inheritdoc
   * This is a complete operator for boxes.
   * @note @inheritdoc
   * @throws IllegalArgumentException if parameters are not correct (but we do not check injectivity of `rho`)
   */
  def mapVariables(rho: Seq[Int]) = {
    require(rho.length == dimension)
    val newdim = rho.count(_ >= 0)
    require(rho forall { i => i >= -1 && i < newdim })
    // we do not check injectivity
    val newlow = new Array[Double](newdim)
    val newhigh = new Array[Double](newdim)
    for ((newi, i) <- rho.zipWithIndex; if newi >= 0) {
      newlow(newi) = low(i)
      newhigh(newi) = high(i)
    }
    new BoxDouble(newlow, newhigh)
  }

  /**
   * @inheritdoc
   * @throws $ILLEGAL
   */
  def mkString(vars: Seq[String]): String = {
    require(vars.length >= dimension)
    if (isEmpty)
      "empty"
    else {
      val bounds = for (i <- 0 until dimension) yield {
        if(low(i)<high(i))
          low(i) + " <= " + vars(i) + " <= " + high(i)
          else vars(i) + " = " + high(i)
      }
      bounds.mkString("[ "," , "," ]")
    }
  }

  val dimension: Int = low.length

  def isEmpty = (low, high).zipped.exists(_ > _)

  def isBottom = isEmpty

  def isTop =  low.forall(_.isNegInfinity) && high.forall(_.isPosInfinity)

  def bottom = BoxDouble.bottom(low.length)

  def top = BoxDouble.top(low.length)

  def tryCompareTo[B >: BoxDouble](other: B)(implicit arg0: (B) => PartiallyOrdered[B]): Option[Int] = other match {
    case other: BoxDouble =>
      if (this.equals(other))
        Some(0)
      else if ((this.low, other.low).zipped.forall(_ <= _) && (this.high, other.high).zipped.forall(_ >= _))
        Some(1)
      else if ((this.low, other.low).zipped.forall(_ >= _) && (this.high, other.high).zipped.forall(_ <= _))
        Some(-1)
      else
        None
    case _ => None
  }

  override def equals(other: Any): Boolean = other match {
    case other: BoxDouble => java.util.Arrays.equals(this.low, other.low) && java.util.Arrays.equals(this.high, other.high)
    case _ => false
  }

  override def hashCode: Int = 41 * (41 + low.hashCode) + high.hashCode
}

/**
 * The factory object for `BoxDouble` properties. It caches the value of `empty(n)` for
 * different `n`, so that we do not create multiple copies of them. This works since
 * numerical properties are immutables.
 *
 * The caching should be probably implemented in [[it.unich.sci.jandom.domains.NumericalDomain]], and
 * should be extended to both full and empty values.
 */
object BoxDouble extends NumericalDomain {

  type Property = BoxDouble

  /**
   * This is the cache for empty boxes.
   */
  private var cacheEmpty: Map[Int, BoxDouble] = Map()

  /**
   * Returns a normalized box with given bounds.
   * @param low lower bounds.
   * @param high upper bounds.
   * @note `low` should have the same length as `high`.
   * @return the normalized box with the specified bounds.
   * @throws $ILLEGAL
   */
  def apply(low: Array[Double], high: Array[Double]): BoxDouble = {
    require(low.length == high.length)
    if ((low, high).zipped.exists(_ > _))
      bottom(low.length)
    else
      new BoxDouble(low, high)
  }

  /**
   * Returns a box consisting of the single point `poDouble`.
   */
  def apply(poDouble: Array[Double]): BoxDouble = apply(poDouble, poDouble)

  /**
   * @inheritdoc
   * @note @inheritdoc
   * @throws $ILLEGAL
   */
  def top(n: Int): BoxDouble = {
    new BoxDouble(Array.fill(n)(Double.NegativeInfinity), Array.fill(n)(Double.PositiveInfinity))
  }

  /**
   * @inheritdoc
   * @note @inheritdoc
   * @throws $ILLEGAL
   */
  def bottom(n: Int): BoxDouble = {
    if (!(cacheEmpty isDefinedAt n))
      cacheEmpty += (n -> new BoxDouble(Array.fill(n)(Double.PositiveInfinity), Array.fill(n)(Double.NegativeInfinity)))
    cacheEmpty(n)
  }
}

