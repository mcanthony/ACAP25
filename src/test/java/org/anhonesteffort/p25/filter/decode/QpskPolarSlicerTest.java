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

package org.anhonesteffort.p25.filter.decode;

import org.anhonesteffort.p25.primitive.ComplexNumber;
import org.anhonesteffort.p25.primitive.DiBit;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.LongStream;

public class QpskPolarSlicerTest {

  @Test
  public void test() {
    final long            SAMPLE_RATE = 1000l;
    final long            SYMBOL_RATE =  100l;
    final QpskPolarSlicer SLICER      = new QpskPolarSlicer();

    final List<DiBit> OUTPUTS = new LinkedList<>();
    SLICER.addSink(OUTPUTS::add);

    LongStream.range(0, SAMPLE_RATE).forEach(l -> {
      int SYMBOL = (int) ((l / SYMBOL_RATE) % 4l);
      switch (SYMBOL) {
        case 0:
          SLICER.consume(new ComplexNumber(0, 1));
          break;

        case 1:
          SLICER.consume(new ComplexNumber(-1, 0));
          break;

        case 2:
          SLICER.consume(new ComplexNumber(1, 0));
          break;

        default:
          SLICER.consume(new ComplexNumber(0, -1));
      }

      assert OUTPUTS.remove(0).getValue() == SYMBOL;
    });
  }

}
