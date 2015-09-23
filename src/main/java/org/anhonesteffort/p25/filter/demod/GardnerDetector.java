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
 *   SDRTrunk - CQPSKDemodulator.java (Copyright 2014, 2015 Dennis Sheirer)
 *   OP25 - gardner_costas_cc_impl.cc (Copyright 2010, 2011, 2012, 2013 KA1RBI)
 *   GNURadio - control_loop (Copyright 2011,2013 Free Software Foundation, Inc.)
 */

package org.anhonesteffort.p25.filter.demod;

import org.anhonesteffort.p25.Sink;
import org.anhonesteffort.p25.filter.interpolate.ComplexNumberMmseInterpolatingFilter;
import org.anhonesteffort.p25.filter.Filter;
import org.anhonesteffort.p25.primitive.ComplexNumber;

public class GardnerDetector extends Filter<ComplexNumber> {

  private final CostasLoop costasLoop;
  private final ComplexNumberMmseInterpolatingFilter interpolator;

  private final float muGain;
  private final float omegaGain;
  private final float omegaRel;
  private final float omegaMid;

  private float mu;
  private float omega;

  public GardnerDetector(CostasLoop costasLoop, int samplesPerSymbol) {
    this.costasLoop = costasLoop;
    interpolator    = new ComplexNumberMmseInterpolatingFilter(samplesPerSymbol);

    mu        = (float) samplesPerSymbol;
    muGain    = 0.05f;
    omega     = (float) samplesPerSymbol;
    omegaGain = 0.1f * muGain * muGain;
    omegaRel  = 0.005f;
    omegaMid  = (float) samplesPerSymbol;

    interpolator.addSink(new SymbolSink());
    this.addSink(costasLoop);
  }

  @Override
  public void consume(ComplexNumber element) {
    mu--;
    costasLoop.increment();
    interpolator.consume(costasLoop.getCurrentVector().multiply(element));

    if (mu <= 1.0f) {
      float halfOmega = omega / 2.0f;
      int   halfSps   = (int)Math.floor(halfOmega);
      float halfMU    = mu + halfOmega - (float)halfSps;

      if(halfMU > 1.0f) {
        halfMU  -= 1.0f;
        halfSps += 1;
      }

      interpolator.interpolate(0, mu);
      interpolator.interpolate(halfSps, halfMU);
    }
  }

  private class SymbolSink implements Sink<ComplexNumber> {

    private ComplexNumber middleSample;
    private ComplexNumber priorMiddleSample = new ComplexNumber(0, 0);
    private ComplexNumber priorSample       = new ComplexNumber(0, 0);
    private ComplexNumber priorSymbol       = new ComplexNumber(0, 0);

    private float clip(float num, float max) {
           if (Float.isNaN(num)) return 0f;
      else if (num > max)        return max;
      else if (num < -max)       return -max;
      else                       return num;
    }

    @Override
    public void consume(ComplexNumber currentSample) {
      if (middleSample == null) {
        middleSample = currentSample;
        return;
      }

      ComplexNumber middleSymbol  = middleSample.multiply(priorMiddleSample.conjugate()).normalize();
      ComplexNumber currentSymbol = currentSample.multiply(priorSample.conjugate()).normalize();

      float inPhaseError    = (priorSymbol.getInPhase()    - currentSymbol.getInPhase())    * middleSymbol.getInPhase();
      float quadratureError = (priorSymbol.getQuadrature() - currentSymbol.getQuadrature()) * middleSymbol.getQuadrature();
      float gardnerError    = clip(inPhaseError + quadratureError, 1.0f);

      omega  = omega + omegaGain * gardnerError;
      omega  = omegaMid + clip(omega - omegaMid, omegaRel);
      mu    += omega + (muGain * gardnerError);

      priorMiddleSample = middleSample;
      middleSample      = null;
      priorSample       = currentSample;
      priorSymbol       = currentSymbol;

      GardnerDetector.this.broadcast(currentSymbol);
    }
  }
}
