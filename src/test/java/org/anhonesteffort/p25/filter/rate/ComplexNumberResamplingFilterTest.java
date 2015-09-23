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

import org.anhonesteffort.p25.filter.FilterFactory;
import org.anhonesteffort.p25.primitive.ComplexNumber;
import org.junit.Test;

import java.util.stream.LongStream;

import static org.anhonesteffort.p25.filter.FilterSinks.CountAndSumSink;

public class ComplexNumberResamplingFilterTest {

  @Test
  public void testResampleDownCount() {
    final long SOURCE_RATE    = 1500;
    final long DESIRED_RATE   = 1000;
    final long PASSBAND_STOP  =  200;
    final long STOPBAND_START =  300;

    final CountAndSumSink SINK = new CountAndSumSink();
    final RateChangeFilter<ComplexNumber> FILTER =
        FilterFactory.getCicResampler(SOURCE_RATE, DESIRED_RATE, 0, PASSBAND_STOP, STOPBAND_START);

    FILTER.addSink(SINK);

    LongStream.range(0, SOURCE_RATE).forEach(l ->
            FILTER.consume(new ComplexNumber(1, 1))
    );

    assert SINK.getCount() == DESIRED_RATE;
  }

  @Test
  public void testResampleUpCount() {
    final long SOURCE_RATE    = 1000;
    final long DESIRED_RATE   = 1500;
    final long PASSBAND_STOP  =  200;
    final long STOPBAND_START =  300;

    final CountAndSumSink SINK = new CountAndSumSink();
    final RateChangeFilter<ComplexNumber> FILTER =
        FilterFactory.getCicResampler(SOURCE_RATE, DESIRED_RATE, 0, PASSBAND_STOP, STOPBAND_START);

    FILTER.addSink(SINK);

    LongStream.range(0, SOURCE_RATE).forEach(l ->
            FILTER.consume(new ComplexNumber(1, 1))
    );

    assert SINK.getCount() == DESIRED_RATE;
  }

}
