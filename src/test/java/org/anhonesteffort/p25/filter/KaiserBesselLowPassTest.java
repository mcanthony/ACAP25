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

import org.anhonesteffort.p25.util.FrequencySweep;
import org.junit.Test;

public class KaiserBesselLowPassTest {

  @Test
  public void testSweep() {
    final long  SAMPLE_RATE    = 1000l;
    final long  PASSBAND_STOP  =  200l;
    final long  STOPBAND_START =  300l;
    final int   ATTENUATION    =   40;
    final float GAIN           =  1.0f;

    final ComplexNumberFirFilter FILTER = FilterFactory.getKaiserBessel(
        SAMPLE_RATE, PASSBAND_STOP, STOPBAND_START, ATTENUATION, GAIN
    );

    final long           SWEEP_START = 0l;
    final long           SWEEP_END   = SAMPLE_RATE / 2;
    final FrequencySweep SWEEP       = new FrequencySweep(
        SAMPLE_RATE, SWEEP_START, SWEEP_END, FILTER
    );

    SWEEP.run();
    final float[] RESPONSE = SWEEP.getResponse();

    for (int offsetHz = 1; offsetHz < RESPONSE.length; offsetHz++) {
      long  impulseFreq        = SWEEP_START + offsetHz;
      float impulseAttenuation = 20f * (float) Math.log10(RESPONSE[offsetHz]/SAMPLE_RATE);

      if (impulseFreq < PASSBAND_STOP) {
        assert Math.abs(impulseAttenuation) <= 0.1;
      } else if (impulseFreq >= STOPBAND_START) {
        assert Math.abs(impulseAttenuation) >= (ATTENUATION * 0.90);
      }
    }
  }

}
