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

import org.anhonesteffort.p25.filter.Filter;
import org.anhonesteffort.p25.filter.FilterFactory;
import org.anhonesteffort.p25.filter.FilterSinks;
import org.anhonesteffort.p25.primitive.ComplexNumber;
import org.anhonesteffort.p25.primitive.Oscillator;
import org.anhonesteffort.p25.util.FrequencySweep;
import org.junit.Test;

import java.util.stream.LongStream;

import static org.anhonesteffort.p25.filter.FilterSinks.CountAndSumSink;

public class ComplexNumberFirstOrderCicDecimatingFilterTest {

  @Test
  public void testSampleCount() {
    final long SOURCE_RATE  = 1500;
    final long DESIRED_RATE =   50;
    final int  DECIMATION   = (int) (SOURCE_RATE / DESIRED_RATE);

    final CountAndSumSink SINK = new CountAndSumSink();
    final ComplexNumberFirstOrderCicDecimatingFilter CIC =
        new ComplexNumberFirstOrderCicDecimatingFilter(DECIMATION);

    CIC.addSink(SINK);

    LongStream.range(0, SOURCE_RATE).forEach(l ->
            CIC.consume(new ComplexNumber(1, 1))
    );

    assert SINK.getCount() == DESIRED_RATE;
  }

  @Test
  public void testSampleSum() {
    final long SOURCE_RATE  = 1500;
    final long DESIRED_RATE =   50;
    final int  DECIMATION   = (int) (SOURCE_RATE / DESIRED_RATE);

    final CountAndSumSink SINK = new CountAndSumSink();
    final ComplexNumberFirstOrderCicDecimatingFilter CIC =
        new ComplexNumberFirstOrderCicDecimatingFilter(DECIMATION);

    CIC.addSink(SINK);

    final ComplexNumber INPUT_SAMPLE       = new ComplexNumber(0.5f, 0.5f);
    final long          SAMPLES_TO_CONSUME = 4;
    final long          SAMPLES_TO_FEED    = SAMPLES_TO_CONSUME * DECIMATION;

    LongStream.range(0, SAMPLES_TO_FEED).forEach(l ->
            CIC.consume(INPUT_SAMPLE)
    );

    assert SINK.getCount() == SAMPLES_TO_CONSUME;
    assert SINK.getSum().equals(INPUT_SAMPLE.multiply(SAMPLES_TO_CONSUME));
  }

  @Test
  public void testSignalInterpolation() {
    final long   SIGNAL_RATE         =  800;
    final double SIGNAL_FREQ         =   80;
    final long   DESIRED_RATE        =  400;
    final int    SIGNAL_LENGTH       = (int) (SIGNAL_RATE  / SIGNAL_FREQ);
    final int    DECM_SIGNAL_LENGTH  = (int) (DESIRED_RATE / SIGNAL_FREQ);
    final int    DECIMATION          = (int) (SIGNAL_RATE / DESIRED_RATE);

    final Oscillator             SIGNAL            = new Oscillator(SIGNAL_RATE, SIGNAL_FREQ);
    final FilterSinks.SignalSink SIGNAL_SINK       = new FilterSinks.SignalSink(SIGNAL_LENGTH);
    final FilterSinks.SignalSink DECM_SIGNAL_SINK = new FilterSinks.SignalSink(DECM_SIGNAL_LENGTH);

    final ComplexNumberFirstOrderCicDecimatingFilter CIC =
        new ComplexNumberFirstOrderCicDecimatingFilter(DECIMATION);

    LongStream.range(0, DESIRED_RATE * 2).forEach(l -> {
      if (l == (SIGNAL_LENGTH))
        CIC.addSink(DECM_SIGNAL_SINK);

      ComplexNumber next = SIGNAL.next();
      SIGNAL_SINK.consume(next);
      CIC.consume(next);
    });

    assert SIGNAL_SINK.isOk();
    assert DECM_SIGNAL_SINK.isOk();
  }

  @Test
  public void testSweepNoComp() {
    final long SOURCE_RATE    = 8000l;
    final long DESIRED_RATE   = 1000l;
    final long PASSBAND_STOP  =  250l;
    final long STOPBAND_START =  350l;

    final Filter<ComplexNumber> CIC = FilterFactory.getCicDecimate(
        SOURCE_RATE, DESIRED_RATE, PASSBAND_STOP, STOPBAND_START
    );

    final long           SWEEP_START = 0l;
    final long           SWEEP_END   = SOURCE_RATE / 2;
    final FrequencySweep SWEEP       = new FrequencySweep(
        SOURCE_RATE, SWEEP_START, SWEEP_END, CIC
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
