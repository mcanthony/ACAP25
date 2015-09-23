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

import org.anhonesteffort.p25.primitive.ComplexNumber;
import org.junit.Test;

import java.util.stream.IntStream;

public class ComplexNumberMovingGainControlTest {

  @Test
  public void test() {
    final int                            HISTORY = 4;
    final ComplexNumberMovingGainControl FILTER  = new ComplexNumberMovingGainControl(HISTORY);

    IntStream.range(0, HISTORY).forEach(i ->
        FILTER.consume(new ComplexNumber(1, 1))
    );

    final ComplexNumber INPUT0  = new ComplexNumber(1, 1);
    final ComplexNumber OUTPUT0 = FILTER.getNextOutput(INPUT0);
    assert OUTPUT0.equals(INPUT0.multiply(1.0f / INPUT0.envelope()));

    final ComplexNumber INPUT1  = new ComplexNumber(2, 2);
    final ComplexNumber OUTPUT1 = FILTER.getNextOutput(INPUT1);
    assert OUTPUT1.equals(INPUT1.multiply(1.0f / INPUT1.envelope()));

    final ComplexNumber INPUT2  = new ComplexNumber(1, 1);
    final ComplexNumber OUTPUT2 = FILTER.getNextOutput(INPUT2);
    assert OUTPUT2.equals(INPUT2.multiply(1.0f / INPUT1.envelope()));

    final ComplexNumber INPUT3  = new ComplexNumber(3, 3);
    final ComplexNumber OUTPUT3 = FILTER.getNextOutput(INPUT3);
    assert OUTPUT3.equals(INPUT3.multiply(1.0f / INPUT3.envelope()));

    final ComplexNumber INPUT4  = new ComplexNumber(2, 2);
    final ComplexNumber OUTPUT4 = FILTER.getNextOutput(INPUT4);
    assert OUTPUT4.equals(INPUT4.multiply(1.0f / INPUT3.envelope()));

    FILTER.consume(new ComplexNumber(1, 1));
    FILTER.consume(new ComplexNumber(1, 1));

    final ComplexNumber INPUT5  = new ComplexNumber(1, 1);
    final ComplexNumber OUTPUT5 = FILTER.getNextOutput(INPUT5);
    assert OUTPUT5.equals(INPUT5.multiply(1.0f / INPUT4.envelope()));
  }

}
