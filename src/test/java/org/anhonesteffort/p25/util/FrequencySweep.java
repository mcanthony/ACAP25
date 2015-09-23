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

package org.anhonesteffort.p25.util;

import org.anhonesteffort.p25.Sink;
import org.anhonesteffort.p25.filter.Filter;
import org.anhonesteffort.p25.primitive.ComplexNumber;
import org.anhonesteffort.p25.primitive.Oscillator;

import java.util.stream.LongStream;

public class FrequencySweep implements Runnable, Sink<ComplexNumber> {

  private final Filter<ComplexNumber> target;
  private final long                  sampleRate;
  private final long                  startFreq;
  private final long                  endFreq;
  private final float[]               response;

  private int offsetFreq = 0;

  public FrequencySweep(long sampleRate, long startFreq, long endFreq, Filter<ComplexNumber> target) {
    this.sampleRate = sampleRate;
    this.startFreq  = startFreq;
    this.endFreq    = endFreq;
    this.target     = target;
    response        = new float[(int)(endFreq - startFreq)];
  }

  @Override
  public void consume(ComplexNumber element) {
    response[offsetFreq] += element.magnitude();
  }

  @Override
  public void run() {
    target.addSink(this);
    while ((startFreq + offsetFreq) < endFreq) {
      Oscillator oscillator = new Oscillator(sampleRate, startFreq + offsetFreq);
      LongStream.range(0, sampleRate).forEach(l ->
              target.consume(oscillator.next())
      );
      offsetFreq++;
    }
  }

  public float[] getResponse() {
    return response;
  }

}
