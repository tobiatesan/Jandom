/*
 * Distributed as part of Scalala, a linear algebra library.
 *
 * Copyright (C) 2008- Daniel Ramage
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110 USA
 */
package scalala;
package library;
package plotting;

import scalala.generic.collection.CanViewAsTensor1;

/**
 * Bins for a histogram.  These can be implicitly constructed from:
 * <pre>
 *   x : HistogramBins = 10  // 10 dynamically determined histogram bins
 *   x : HistogramBins = Array(1.0,2.0,3.2) // five buckets wit the given splits
 *   x : HistogramBins = (0,100,10) // ten bins evenly dividing 0 to 100.
 * </pre>
 *
 * @author dramage
 */
sealed trait HistogramBins;

/**
 * Set of histograms for binning data using the given splits.
 *
 * @author dramage
 */
case class StaticHistogramBins(splits : Array[Double])
extends HistogramBins {
  /** Returns the bin for the given value, between 0 and splits.length inclusive. */
  def bin(value : Double) = {
    var i = 0;
    while (i < splits.length && value > splits(i)) {
      i += 1;
    }
    i;
  }
}

/**
 * Create a set of StaticHistogramBins from a number and an (eventual)
 * lower and upper bound.
 *
 * @author dramage
 */
case class DynamicHistogramBins(number : Int = 10)
extends HistogramBins {
  def apply(lower : Double, upper : Double) =
    StaticHistogramBins(Array.tabulate(number-1)(i => lower + ((i + 1.0) / (number)) * (upper - lower)));
}

/**
 * Static constructors for HistogramBins.
 *
 * @author dramage
 */
object HistogramBins {
  implicit def fromNumber(number : Int) : HistogramBins =
    DynamicHistogramBins(number);

//  implicit def fromSplits[S,K,V](splits : S)(implicit tt : CanViewAsTensor1[S,K,V], v : V=>Double) : HistogramBins =
//    StaticHistogramBins(t.domain(splits).map(d => v(t.get(splits, d))).toArray);

  implicit def fromRange(minMaxCount : (Double,Double,Int)) : HistogramBins =
    DynamicHistogramBins(minMaxCount._3)(minMaxCount._1, minMaxCount._2);
}
