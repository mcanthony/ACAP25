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

import org.anhonesteffort.p25.Sink;
import org.anhonesteffort.p25.primitive.ComplexNumber;

public class FilterSinks {

  public static class SingleImpulseSink implements Sink<ComplexNumber> {

    private final float[] coefficients;
    private       int     coefficientIndex = 0;

    public SingleImpulseSink(float[] coefficients) {
      this.coefficients = coefficients;
    }

    @Override
    public void consume(ComplexNumber element) {
      if (coefficientIndex < coefficients.length) {
        assert Math.abs(coefficients[coefficientIndex] - element.getInPhase())    < 0.000001;
        assert Math.abs(coefficients[coefficientIndex] - element.getQuadrature()) < 0.000001;
        coefficientIndex++;
      } else {
        assert element.getInPhase()    == 0.0;
        assert element.getQuadrature() == 0.0;
      }
    }

    public boolean isOk() {
      return coefficientIndex == coefficients.length;
    }

  }

  public static class ImpulseTrainSink implements Sink<ComplexNumber> {

    private final float[] coefficients;
    private final int     impulsePadding;
    private final int     decimation;
    private       int     sampleCount      = 0;
    private       int     coefficientIndex = 0;

    public ImpulseTrainSink(float[] coefficients, int impulsePadding, int decimation) {
      this.coefficients   = coefficients;
      this.impulsePadding = (impulsePadding / decimation);
      this.decimation     = decimation;
    }

    public ImpulseTrainSink(float[] coefficients, int impulsePadding) {
      this(coefficients, impulsePadding, 1);
    }

    @Override
    public void consume(ComplexNumber element) {
      if ((sampleCount % impulsePadding) == 0) {
        coefficientIndex = 0;
        assert element.getInPhase()    == (coefficients[coefficientIndex] / decimation);
        assert element.getQuadrature() == (coefficients[coefficientIndex] / decimation);
      } else if (coefficientIndex < coefficients.length) {
        assert element.getInPhase()    == (coefficients[coefficientIndex] / decimation);
        assert element.getQuadrature() == (coefficients[coefficientIndex] / decimation);
      } else {
        assert element.equals(new ComplexNumber(0f, 0f));
      }

      sampleCount++;
      coefficientIndex++;
    }

    public boolean isOk() {
      return sampleCount >= impulsePadding;
    }

  }

  public static class SignalSink implements Sink<ComplexNumber> {

    private final ComplexNumber[] signal;
    private int storeIndex   = 0;
    private int compareIndex = 0;
    private int sampleCount  = 0;

    public SignalSink(int length) {
      signal = new ComplexNumber[length];
    }

    @Override
    public void consume(ComplexNumber element) {
      if (storeIndex < signal.length) {
        signal[storeIndex++] = element;
      } else {
        assert Math.abs(signal[compareIndex].getInPhase()    - element.getInPhase())    < 0.00001;
        assert Math.abs(signal[compareIndex].getQuadrature() - element.getQuadrature()) < 0.00001;
        compareIndex = (compareIndex + 1) == signal.length ? 0 : compareIndex + 1;
      }

      sampleCount++;
    }

    public boolean isOk() {
      return storeIndex == signal.length && compareIndex == 0 && sampleCount >= signal.length * 2;
    }

  }

  public static class CountAndSumSink implements Sink<ComplexNumber> {

    private int           count = 0;
    private ComplexNumber sum   = new ComplexNumber(0, 0);

    @Override
    public void consume(ComplexNumber element) {
      count++;
      sum = sum.add(element);
    }

    public int getCount() {
      return count;
    }

    public ComplexNumber getSum() {
      return sum;
    }

  }

  public static class ForgetfulSink implements Sink<ComplexNumber> {

    private ComplexNumber sample;

    @Override
    public void consume(ComplexNumber element) {
      sample = element;
    }

    public ComplexNumber getSample() {
      return sample;
    }

  }

}
