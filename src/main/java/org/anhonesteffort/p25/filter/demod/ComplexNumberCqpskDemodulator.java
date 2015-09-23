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

package org.anhonesteffort.p25.filter.demod;

import org.anhonesteffort.p25.Sink;
import org.anhonesteffort.p25.filter.Filter;
import org.anhonesteffort.p25.primitive.ComplexNumber;

public class ComplexNumberCqpskDemodulator extends Filter<ComplexNumber> {

  private final CostasLoop      costasLoop;
  private final GardnerDetector gardnerDetector;

  public ComplexNumberCqpskDemodulator(long sampleRate, long symbolRate) {
    int samplesPerSymbol = (int) (sampleRate / symbolRate);

    costasLoop      = new CostasLoop(sampleRate, symbolRate);
    gardnerDetector = new GardnerDetector(costasLoop, samplesPerSymbol);
  }

  public void correctPhaseError(float correctionDeg) {
    costasLoop.correctPhaseError(correctionDeg);
  }

  @Override
  public void consume(ComplexNumber element) {
    gardnerDetector.consume(element);
  }

  @Override
  public void addSink(Sink<ComplexNumber> sink) {
    gardnerDetector.addSink(sink);
  }

  @Override
  public void removeSink(Sink<ComplexNumber> sink) {
    gardnerDetector.removeSink(sink);
  }

}
