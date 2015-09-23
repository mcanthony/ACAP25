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

package org.anhonesteffort.p25.primitive;

import org.junit.Test;

public class OscillatorTest {

  @Test
  public void test() {
    final long            RATE          = 20;
    final double          FREQ          =  4;
    final int             SIGNAL_LENGTH = (int) (RATE / FREQ);
    final ComplexNumber[] SIGNAL        = new ComplexNumber[SIGNAL_LENGTH];
    final Oscillator      OSCILLATOR    = new Oscillator(RATE, FREQ);

    int storeIndex   = 0;
    int compareIndex = 0;

    for (int i = 0; i < RATE; i++) {
      ComplexNumber next = OSCILLATOR.next();
      if (storeIndex < SIGNAL.length) {
        SIGNAL[storeIndex++] = next;
      } else {
        assert Math.abs(SIGNAL[compareIndex].getInPhase()    - next.getInPhase())    < 0.00001;
        assert Math.abs(SIGNAL[compareIndex].getQuadrature() - next.getQuadrature()) < 0.00001;
        compareIndex = (compareIndex + 1) == SIGNAL.length ? 0 : compareIndex + 1;
      }
    }
  }

  @Test
  public void testSloppy() {
    final long            RATE          = 20;
    final double          FREQ          =  4;
    final int             SIGNAL_LENGTH = (int) (RATE / FREQ);
    final ComplexNumber[] SIGNAL        = new ComplexNumber[SIGNAL_LENGTH];
    final Oscillator      OSCILLATOR    = new Oscillator(RATE, FREQ, true);

    int storeIndex   = 0;
    int compareIndex = 0;

    for (int i = 0; i < RATE; i++) {
      ComplexNumber next = OSCILLATOR.next();
      if (storeIndex < SIGNAL.length) {
        SIGNAL[storeIndex++] = next;
      } else {
        assert Math.abs(SIGNAL[compareIndex].getInPhase()    - next.getInPhase())    < 0.1;
        assert Math.abs(SIGNAL[compareIndex].getQuadrature() - next.getQuadrature()) < 0.1;
        compareIndex = (compareIndex + 1) == SIGNAL.length ? 0 : compareIndex + 1;
      }
    }
  }

}
