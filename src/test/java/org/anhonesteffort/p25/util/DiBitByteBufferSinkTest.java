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

import org.anhonesteffort.p25.primitive.DiBit;
import org.junit.Test;

import java.util.stream.IntStream;

public class DiBitByteBufferSinkTest {

  @Test
  public void test() {
    final int                 BIT_LENGTH = 80;
    final DiBitByteBufferSink SINK       = new DiBitByteBufferSink(BIT_LENGTH);

    IntStream.range(0, BIT_LENGTH / 2).forEach(i -> {
      assert !SINK.isFull();
      SINK.consume(new DiBit(i % 4));
    });

    assert SINK.isFull();
    SINK.getBytes().position(0);

    while (SINK.getBytes().position() < SINK.getBytes().capacity()) {
      assert SINK.getBytes().get() == 0x1B;

      int NEXT_POSITION = SINK.getBytes().position() + 1;
      if (NEXT_POSITION <= SINK.getBytes().limit())
        SINK.getBytes().position(NEXT_POSITION);
    }
  }

}
