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

package org.anhonesteffort.p25.filter;

import org.anhonesteffort.p25.primitive.ComplexNumber;

import java.util.stream.IntStream;

public class ComplexNumberFirFilter extends ConsistentRateFilter<ComplexNumber> {

  private final float[] coefficients;
  private final float   gain;

  private final ComplexNumber[] delayLine;
  private final int             length;
  private       int             delayIndex;

  public ComplexNumberFirFilter(float[] coefficients, float gain) {
    this.coefficients = coefficients;
    this.gain         = gain;

    delayIndex = 0;
    length     = coefficients.length;
    delayLine  = new ComplexNumber[length];

    IntStream.range(0, length)
             .forEach(i -> delayLine[i] = new ComplexNumber(0f, 0f));
  }

  protected int getLength() {
    return length;
  }

  protected float[] getCoefficients() {
    return coefficients;
  }

  @Override
  protected ComplexNumber getNextOutput(ComplexNumber input) {
    delayLine[delayIndex] = input;

    int   accumulateIndex = delayIndex;
    float iAccumulator    = 0.0f;
    float qAccumulator    = 0.0f;

    for (int coefficientIndex = 0; coefficientIndex < length; coefficientIndex++) {
      iAccumulator    += coefficients[coefficientIndex] * delayLine[accumulateIndex].getInPhase();
      qAccumulator    += coefficients[coefficientIndex] * delayLine[accumulateIndex].getQuadrature();
      accumulateIndex  = (accumulateIndex == 0) ? (length - 1) : (accumulateIndex - 1);
    }

    delayIndex = (delayIndex + 1) % length;

    return new ComplexNumber(iAccumulator * gain, qAccumulator * gain);
  }

}
