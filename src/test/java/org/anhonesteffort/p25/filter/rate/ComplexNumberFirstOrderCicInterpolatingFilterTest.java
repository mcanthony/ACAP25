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

import org.anhonesteffort.p25.filter.ComplexNumberFirFilter;
import org.anhonesteffort.p25.filter.Filter;
import org.anhonesteffort.p25.filter.FilterFactory;
import org.anhonesteffort.p25.primitive.ComplexNumber;
import org.anhonesteffort.p25.primitive.Oscillator;
import org.anhonesteffort.p25.util.FrequencySweep;
import org.junit.Test;

import java.util.stream.LongStream;

import static org.anhonesteffort.p25.filter.FilterSinks.CountAndSumSink;
import static org.anhonesteffort.p25.filter.FilterSinks.SignalSink;

public class ComplexNumberFirstOrderCicInterpolatingFilterTest {

  @Test
  public void testSampleCount() {
    final long SOURCE_RATE   =   50;
    final long DESIRED_RATE  = 1500;
    final int  INTERPOLATION = (int) (DESIRED_RATE / SOURCE_RATE);

    final CountAndSumSink        SINK     = new CountAndSumSink();
    final ComplexNumberFirFilter CLEANUP  = new ComplexNumberFirFilter(new float[] {1f}, 1f);
    final ComplexNumberFirstOrderCicInterpolatingFilter CIC =
        new ComplexNumberFirstOrderCicInterpolatingFilter(INTERPOLATION, CLEANUP);

    CIC.addSink(SINK);

    LongStream.range(0, SOURCE_RATE).forEach(l ->
            CIC.consume(new ComplexNumber(1, 1))
    );

    assert SINK.getCount() == DESIRED_RATE;
  }

  @Test
  public void testSignalInterpolation() {
    final long   SIGNAL_RATE         =  400;
    final double SIGNAL_FREQ         =   40;
    final long   DESIRED_RATE        =  800;
    final int    SIGNAL_LENGTH       = (int) (SIGNAL_RATE  / SIGNAL_FREQ);
    final int    INTRP_SIGNAL_LENGTH = (int) (DESIRED_RATE / SIGNAL_FREQ);
    final int    INTERPOLATION       = (int) (DESIRED_RATE / SIGNAL_RATE);

    final Oscillator SIGNAL            = new Oscillator(SIGNAL_RATE, SIGNAL_FREQ);
    final SignalSink SIGNAL_SINK       = new SignalSink(SIGNAL_LENGTH);
    final SignalSink INTRP_SIGNAL_SINK = new SignalSink(INTRP_SIGNAL_LENGTH);

    final ComplexNumberFirFilter CLEANUP = new ComplexNumberFirFilter(new float[] {1f}, 1f);
    final ComplexNumberFirstOrderCicInterpolatingFilter CIC =
        new ComplexNumberFirstOrderCicInterpolatingFilter(INTERPOLATION, CLEANUP);

    LongStream.range(0, DESIRED_RATE * 2).forEach(l -> {
      if (l == (SIGNAL_LENGTH))
        CIC.addSink(INTRP_SIGNAL_SINK);

      ComplexNumber next = SIGNAL.next();
      SIGNAL_SINK.consume(next);
      CIC.consume(next);
    });

    assert SIGNAL_SINK.isOk();
    assert INTRP_SIGNAL_SINK.isOk();
  }

  @Test
  public void testSweep() {
    final long SOURCE_RATE    = 1000l;
    final long DESIRED_RATE   = 8000l;
    final long PASSBAND_STOP  =  250l;
    final long STOPBAND_START =  350l;

    final Filter<ComplexNumber> CIC = FilterFactory.getCicInterpolate(
        SOURCE_RATE, DESIRED_RATE, PASSBAND_STOP, STOPBAND_START
    );

    final long           SWEEP_START = 0l;
    final long           SWEEP_END   = DESIRED_RATE / 2;
    final FrequencySweep SWEEP       = new FrequencySweep(
        DESIRED_RATE, SWEEP_START, SWEEP_END, CIC
    );

    SWEEP.run();
    final float[] RESPONSE = SWEEP.getResponse();

    for (int offsetHz = 0; offsetHz < RESPONSE.length; offsetHz++) {
      long  impulseFreq        = SWEEP_START + offsetHz;
      float impulseAttenuation = 20f * (float) Math.log10(RESPONSE[offsetHz]/DESIRED_RATE);

      if (impulseFreq < PASSBAND_STOP) {
        assert Math.abs(impulseAttenuation) <= 1.0f;
      } else if (impulseFreq >= DESIRED_RATE) {
        assert Math.abs(impulseAttenuation) >= 12.0f;
      }
    }
  }

}
