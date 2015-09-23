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
import org.junit.Test;

import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.anhonesteffort.p25.filter.FilterSinks.SingleImpulseSink;
import static org.anhonesteffort.p25.filter.FilterSinks.ImpulseTrainSink;
import static org.anhonesteffort.p25.filter.FilterSinks.ForgetfulSink;

public class ComplexNumberFirFilterTest {

  @Test
  public void testSingleImpulse() {
    final float   GAIN         =  1.0f;
    final float[] COEFFICIENTS = new float[] {1, 2, 3, 4, 5, 6, 7};

    final ComplexNumberFirFilter FILTER = new ComplexNumberFirFilter(COEFFICIENTS, GAIN);
    final SingleImpulseSink      SINK   = new SingleImpulseSink(COEFFICIENTS);
    FILTER.addSink(SINK);

    IntStream.range(0, COEFFICIENTS.length + 1).forEach(i -> {
          if (i == 0) FILTER.consume(new ComplexNumber(1f, 1f));
          else        FILTER.consume(new ComplexNumber(0f, 0f));
        }
    );

    assert SINK.isOk();
  }

  @Test
  public void testImpulseTrain() {
    final int     IMPULSE_PADDING =  10;
    final float   GAIN            = 1.0f;
    final float[] COEFFICIENTS    = new float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f};

    final ComplexNumberFirFilter FILTER = new ComplexNumberFirFilter(COEFFICIENTS, GAIN);
    final ImpulseTrainSink       SINK   = new ImpulseTrainSink(COEFFICIENTS, IMPULSE_PADDING);

    FILTER.addSink(SINK);

    LongStream.range(0, IMPULSE_PADDING * 2).forEach(l -> {
      if (l % IMPULSE_PADDING == 0)
        FILTER.consume(new ComplexNumber(1f, 1f));
      else
        FILTER.consume(new ComplexNumber(0f, 0f));
    });
  }

  @Test
  public void testStep() {
    final float   GAIN         =  1.0f;
    final float[] COEFFICIENTS = new float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f};

    final ComplexNumberFirFilter FILTER = new ComplexNumberFirFilter(COEFFICIENTS, GAIN);
    final ForgetfulSink          SINK   = new ForgetfulSink();
    FILTER.addSink(SINK);

    IntStream.range(0, FILTER.getLength())
             .forEach(i -> FILTER.consume(new ComplexNumber(1, 1)));

    float FILTER_SUM = 0;
    for (float f : FILTER.getCoefficients())
      FILTER_SUM += f;

    assert Math.abs((FILTER_SUM * GAIN) - SINK.getSample().getInPhase())    < 0.000001;
    assert Math.abs((FILTER_SUM * GAIN) - SINK.getSample().getQuadrature()) < 0.000001;
  }

}
