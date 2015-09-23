/*
 * Copyright (C) 2015 An Honest Effort LLC, fuck the police.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.anhonesteffort.p25.filter.rate;

import org.anhonesteffort.p25.filter.Filter;
import org.anhonesteffort.p25.util.Copyable;

public abstract class RateChangeFilter<T extends Copyable<T>> extends Filter<T> {

  private final int interpolation;
  private final int decimation;

  public RateChangeFilter(int interpolation, int decimation) {
    this.interpolation = interpolation;
    this.decimation    = decimation;
  }

  public int getInterpolation() {
    return interpolation;
  }

  public int getDecimation() {
    return decimation;
  }

  public double getRateChange() {
    return ((double) interpolation) / ((double) decimation);
  }

}
