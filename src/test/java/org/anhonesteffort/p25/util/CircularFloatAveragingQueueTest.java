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

package org.anhonesteffort.p25.util;

import org.junit.Test;

import java.util.NoSuchElementException;

public class CircularFloatAveragingQueueTest {

  @Test
  public void testIllegalRemove() throws Exception {
    try {

      new CircularFloatAveragingQueue(4).remove();
      assert false;

    } catch (NoSuchElementException e) {
      assert true;
    }
  }

  @Test
  public void testAverageEqualValues() throws Exception {
    final CircularFloatAveragingQueue QUEUE = new CircularFloatAveragingQueue(4);

    QUEUE.add(new float[] {1, 2, 3, 4});
    QUEUE.add(new float[] {1, 2, 3, 4});
    QUEUE.add(new float[] {1, 2, 3, 4});
    QUEUE.add(new float[] {1, 2, 3, 4});

    int AVERAGES_REMOVED = 0;
    while (!QUEUE.isEmpty()) {
      final float[] AVERAGE = QUEUE.remove();
      assert AVERAGE[0] == 1 && AVERAGE[1] == 2 && AVERAGE[2] == 3 && AVERAGE[3] == 4;
      AVERAGES_REMOVED++;
    }

    assert AVERAGES_REMOVED == 4;
  }

  private boolean approxEquals(float x, float y) {
    return Math.abs(x - y) < 0.1;
  }

  @Test
  public void testAverageDifferentValues() throws Exception {
    final CircularFloatAveragingQueue QUEUE = new CircularFloatAveragingQueue(4);

    QUEUE.add(new float[] {1, 2, 3, 4});
    QUEUE.add(new float[] {5, 6, 7, 8});
    QUEUE.add(new float[] {9, 0, 1, 2});
    QUEUE.add(new float[] {3, 4, 5, 6});

    final float[] AVERAGE0 = QUEUE.remove();
    final float[] AVERAGE1 = QUEUE.remove();
    final float[] AVERAGE2 = QUEUE.remove();
    final float[] AVERAGE3 = QUEUE.remove();

    assert approxEquals(AVERAGE0[0], 4.5f) && approxEquals(AVERAGE0[1], 3f) &&
           approxEquals(AVERAGE0[2], 4f)   && approxEquals(AVERAGE0[3], 5f);

    assert approxEquals(AVERAGE1[0], 5.6f) && approxEquals(AVERAGE1[1], 3.3f) &&
           approxEquals(AVERAGE1[2], 4.3f) && approxEquals(AVERAGE1[3], 5.3f);

    assert approxEquals(AVERAGE2[0], 6f) && approxEquals(AVERAGE2[1], 2f) &&
           approxEquals(AVERAGE2[2], 3f) && approxEquals(AVERAGE2[3], 4f);

    assert approxEquals(AVERAGE3[0], 3f) && approxEquals(AVERAGE3[1], 4f) &&
           approxEquals(AVERAGE3[2], 5f) && approxEquals(AVERAGE3[3], 6f);
  }

}
