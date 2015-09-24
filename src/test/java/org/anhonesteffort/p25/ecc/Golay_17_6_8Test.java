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
package org.anhonesteffort.p25.ecc;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Golay_17_6_8Test {

  @Test
  public void test() {
    final int          WORD_LENGTH = 17;
    final int          INFO_MASK   = 0x3F;
    final Random       RANDOM      = new Random();
    final Golay_17_6_8 GOLAY       = new Golay_17_6_8();

    for (int i = 0; i < INFO_MASK; i++) {
      final int[] RESULT = GOLAY.decode(GOLAY.encode(i));
      assert RESULT[0] == 0;
      assert RESULT[1] == i;
    }

    for (int i = 0; i < 100000; i++) {
      final int           INFO6       = RANDOM.nextInt(INFO_MASK);
            int           WORD17      = GOLAY.encode(INFO6);
      final int           ERROR_COUNT = RANDOM.nextInt(4);
      final List<Integer> ERRORS      = new LinkedList<>();

      for (int j = 0; j < ERROR_COUNT; j++) {
        int ERROR = RANDOM.nextInt(WORD_LENGTH);
        while (ERRORS.contains(ERROR))
          ERROR = RANDOM.nextInt(WORD_LENGTH);

        WORD17 ^= 1 << ERROR;
        ERRORS.add(ERROR);
      }

      final int[] RESULT6 = GOLAY.decode(WORD17);

      assert RESULT6[0] == ERROR_COUNT;
      assert RESULT6[1] == INFO6;
    }
  }

}
