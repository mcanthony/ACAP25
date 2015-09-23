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

import org.anhonesteffort.p25.Source;
import org.anhonesteffort.p25.primitive.ComplexNumber;
import org.anhonesteffort.p25.sample.DynamicSink;
import org.anhonesteffort.p25.sample.Samples;
import org.anhonesteffort.p25.sample.SamplesSink;

public class SampleToSamplesConverter extends Source<Samples, SamplesSink> implements DynamicSink<ComplexNumber> {

  @Override
  public void onSourceStateChange(Long sampleRate, Double frequency) {
    sinks.forEach(sink -> sink.onSourceStateChange(sampleRate, frequency));
  }

  @Override
  public void consume(ComplexNumber element) {
    broadcast(new Samples(new float[] {element.getInPhase(), element.getQuadrature()}));
  }

}
