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

import org.anhonesteffort.p25.Sink;
import org.anhonesteffort.p25.filter.Filter;
import org.anhonesteffort.p25.primitive.ComplexNumber;

import java.util.stream.IntStream;

public class ComplexNumberFirstOrderCicDecimatingFilter extends RateChangeFilter<ComplexNumber> {

  private final Filter<ComplexNumber> firstStage;
  private final Filter<ComplexNumber> compensation;

  public ComplexNumberFirstOrderCicDecimatingFilter(int decimation, Filter<ComplexNumber> compensation) {
    super(1, decimation);

    firstStage        = new DecimatingCicStage(decimation, 1);
    this.compensation = compensation;

    firstStage.addSink(compensation);
  }

  @Override
  public void consume(ComplexNumber element) {
    firstStage.consume(element);
  }

  @Override
  public void addSink(Sink<ComplexNumber> sink) {
    compensation.addSink(sink);
  }

  @Override
  public void removeSink(Sink<ComplexNumber> sink) {
    compensation.removeSink(sink);
  }

  public static class DecimatingCicStage extends Filter<ComplexNumber> {

    private final ComplexNumber[] delayLine;
    private final int             decimation;
    private final int             diffDelay;
    private final float           gain;

    private ComplexNumber last       = new ComplexNumber(0, 0);
    private int           delayIndex = 0;

    public DecimatingCicStage(int decimation, int diffDelay) {
      this.decimation = decimation;
      this.diffDelay  = diffDelay;
      gain            = 1f / decimation;
      delayLine       = new ComplexNumber[decimation * diffDelay];

      IntStream.range(0, delayLine.length)
               .forEach(i -> delayLine[i] = new ComplexNumber(0, 0));
    }

    private ComplexNumber integrate(ComplexNumber element) {
      last = element.add(last);
      return last;
    }

    private void comb(ComplexNumber element) {
      ComplexNumber  output = element.subtract(delayLine[delayIndex]);
      delayLine[delayIndex] = element;
      delayIndex            = (delayIndex + 1) % (decimation * diffDelay);

      if (delayIndex == 0)
        broadcast(output.multiply(gain));
    }

    @Override
    public void consume(ComplexNumber element) {
      comb(integrate(element));
    }

  }

}
