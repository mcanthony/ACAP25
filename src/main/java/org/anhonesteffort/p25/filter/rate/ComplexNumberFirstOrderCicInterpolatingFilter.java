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

public class ComplexNumberFirstOrderCicInterpolatingFilter extends RateChangeFilter<ComplexNumber> {

  private final Filter<ComplexNumber> lastStage;

  public ComplexNumberFirstOrderCicInterpolatingFilter(int interpolation) {
    super(interpolation, 1);
    lastStage = new InterpolatingCicStage(interpolation, 1);
  }

  @Override
  public void consume(ComplexNumber element) {
    lastStage.consume(element);
  }

  @Override
  public void addSink(Sink<ComplexNumber> sink) {
    lastStage.addSink(sink);
  }

  @Override
  public void removeSink(Sink<ComplexNumber> sink) {
    lastStage.removeSink(sink);
  }

  public static class InterpolatingCicStage extends Filter<ComplexNumber> {

    private final ComplexNumber[] delayLine;
    private final int             interpolation;
    private final int             diffDelay;
    private final float           gain;

    private ComplexNumber last       = new ComplexNumber(0, 0);
    private int           delayIndex = 0;

    public InterpolatingCicStage(int interpolation, int diffDelay) {
      this.interpolation = interpolation;
      this.diffDelay     = diffDelay;
      gain               = 1f / (interpolation * diffDelay);
      delayLine          = new ComplexNumber[interpolation * diffDelay];

      IntStream.range(0, delayLine.length)
               .forEach(i -> delayLine[i] = new ComplexNumber(0, 0));
    }

    private ComplexNumber comb(ComplexNumber element) {
      ComplexNumber output  = element.subtract(delayLine[delayIndex]);
      delayLine[delayIndex] = element;
      delayIndex            = (delayIndex + 1) % (interpolation * diffDelay);

      return output;
    }

    private void integrate(ComplexNumber element) {
      last = element.add(last);
      broadcast(last.multiply(gain));
    }

    @Override
    public void consume(ComplexNumber element) {
      integrate(comb(element));

      for (int i = 0; i < (interpolation - 1); i++)
        broadcast(new ComplexNumber(0, 0));
    }
  }

}
