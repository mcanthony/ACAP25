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

import org.anhonesteffort.p25.primitive.Oscillator;
import org.junit.Test;

import java.util.stream.LongStream;

import static org.anhonesteffort.p25.filter.FilterSinks.SignalSink;

public class ComplexNumberFrequencyTranslatingFilterTest {

  @Test
  public void test() {
    final long       SOURCE_RATE   =  40;
    final double     SOURCE_FREQ   =  20;
    final double     CHANNEL_FREQ  =   5;
    final int        SIGNAL_LENGTH = (int) (SOURCE_RATE / CHANNEL_FREQ);
    final Oscillator SOURCE        = new Oscillator(SOURCE_RATE, SOURCE_FREQ);
    final SignalSink SINK          = new SignalSink(SIGNAL_LENGTH);

    final ComplexNumberFrequencyTranslatingFilter TRANSLATOR =
        new ComplexNumberFrequencyTranslatingFilter(SOURCE_RATE, SOURCE_FREQ, CHANNEL_FREQ);

    TRANSLATOR.addSink(SINK);
    LongStream.range(0, SOURCE_RATE).forEach(l -> TRANSLATOR.consume(SOURCE.next()));

    assert SINK.isOk();
  }

}
