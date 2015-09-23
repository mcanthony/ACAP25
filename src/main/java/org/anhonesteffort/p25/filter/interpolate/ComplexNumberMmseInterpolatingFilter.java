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
 *
 * Derived from:
 *   SDRTrunk - QPSKInterpolator.java (Copyright 2014, 2015 Dennis Sheirer)
 */

package org.anhonesteffort.p25.filter.interpolate;

import org.anhonesteffort.p25.filter.Filter;
import org.anhonesteffort.p25.primitive.ComplexNumber;

import java.util.stream.IntStream;

// todo: use an array of ComplexNumberFirFilter's instead
public class ComplexNumberMmseInterpolatingFilter extends Filter<ComplexNumber> {

  private final ComplexNumber[] delayLine;
  private int delayIndex = 0;

  public ComplexNumberMmseInterpolatingFilter(int samplesPerSymbol) {
    delayLine = new ComplexNumber[samplesPerSymbol * 4];
    IntStream.range(0, delayLine.length)
             .forEach(i -> delayLine[i] = new ComplexNumber(0, 0));
  }

  @Override
  public void consume(ComplexNumber element) {
    delayLine[delayIndex] = delayLine[delayIndex + (delayLine.length / 2)] = element;
    delayIndex            = (delayIndex + 1) % (delayLine.length / 2);
  }

  public void interpolate(int offset, float mu) {
          offset                = delayIndex + offset;
    int   tapIndex              = (int) (InterpolatorTaps.STEP_COUNT * mu);
    int   tapAccIndex           = InterpolatorTaps.TAP_COUNT - 1;
    int   delayAccIndex         = 0;
    float inPhaseAccumulator    = 0f;
    float quadratureAccumulator = 0f;

    while (tapAccIndex >= 0 && delayAccIndex < InterpolatorTaps.TAP_COUNT) {
      inPhaseAccumulator    += InterpolatorTaps.TAPS[tapIndex][tapAccIndex] * delayLine[offset + delayAccIndex].getInPhase();
      quadratureAccumulator += InterpolatorTaps.TAPS[tapIndex][tapAccIndex] * delayLine[offset + delayAccIndex].getQuadrature();

      tapAccIndex--;
      delayAccIndex++;
    }

    broadcast(new ComplexNumber(inPhaseAccumulator, quadratureAccumulator));
  }

}
