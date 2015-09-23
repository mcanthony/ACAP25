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
import org.anhonesteffort.p25.primitive.ComplexNumber;

public class CostasLoop implements Sink<ComplexNumber> {

  private static final float TWO_PI = 2.0f * (float) Math.PI;

  private final float maxFrequency;
  private final float alphaGain;
  private final float betaGain;

  private float loopPhase = 0.0f;
  private float loopFreq  = 0.0f;

  public CostasLoop(long sampleRate, long symbolRate, float damping, float loopBandwidth) {
    maxFrequency = ((float)symbolRate * (float)Math.PI) / (float)sampleRate;

    float divisor   = (1.0f + (2.0f * damping * loopBandwidth) + (loopBandwidth * loopBandwidth));
          alphaGain = (4.0f * damping * loopBandwidth)       / divisor;
          betaGain  = (4.0f * loopBandwidth * loopBandwidth) / divisor;
  }

  public CostasLoop(long sampleRate, long symbolRate) {
    this(sampleRate, symbolRate, (float) Math.sqrt(2.0f) / 2.0f, TWO_PI / 150f);
  }

  public ComplexNumber getCurrentVector() {
    return new ComplexNumber(loopPhase);
  }

  public void correctPhaseError(float correctionDeg) {
    if (correctionDeg > 180f)
      correctionDeg = (correctionDeg - 180f) * -1.0f;

    float symbolFreq = maxFrequency * 2.0f;
    float divisor    = 360.0f / correctionDeg;
          loopFreq  += symbolFreq / divisor;

    if(loopFreq > maxFrequency) {
      loopFreq -= 2.0f * maxFrequency;
    } else if(loopFreq < -maxFrequency) {
      loopFreq += 2.0f * maxFrequency;
    }
  }

  private void unwrapPhase() {
    while(loopPhase > TWO_PI)
      loopPhase -= TWO_PI;

    while(loopPhase < -TWO_PI)
      loopPhase += TWO_PI;
  }

  public void increment() {
    loopPhase += loopFreq;
    unwrapPhase();
  }

  private void adjust(float phaseError) {
    loopFreq  += betaGain * phaseError;
    loopPhase += loopFreq + alphaGain * phaseError;

    unwrapPhase();

    if(loopFreq > maxFrequency)
      loopFreq = maxFrequency;

    if(loopFreq < -maxFrequency)
      loopFreq = -maxFrequency;
  }

  @Override
  public void consume(ComplexNumber element) {
    float phaseError;

    if(Math.abs(element.getInPhase()) > Math.abs(element.getQuadrature())) {
      phaseError = (element.getInPhase() > 0) ? -element.getQuadrature() : element.getQuadrature();
    } else {
      phaseError = (element.getQuadrature() > 0) ? element.getInPhase() : -element.getInPhase();
    }

    adjust(phaseError);
  }

}
