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

public class ComplexNumberTest {

  @Test
  public void testAdd() {
    final ComplexNumber NUM0 = new ComplexNumber(1, 0);
    final ComplexNumber NUM1 = new ComplexNumber(1, 2);

    assert NUM0.add(NUM1).getInPhase()    == 2;
    assert NUM0.add(NUM1).getQuadrature() == 2;
  }

  @Test
  public void testSubtract() {
    final ComplexNumber NUM0 = new ComplexNumber(1, 0);
    final ComplexNumber NUM1 = new ComplexNumber(1, 2);

    assert NUM0.subtract(NUM1).getInPhase()    ==  0;
    assert NUM0.subtract(NUM1).getQuadrature() == -2;
  }

  @Test
  public void testMultiply() {
    final ComplexNumber NUM0 = new ComplexNumber(1, 0);
    final ComplexNumber NUM1 = new ComplexNumber(1, 2);

    assert NUM0.multiply(2).getInPhase()    == 2;
    assert NUM0.multiply(2).getQuadrature() == 0;

    assert NUM0.multiply(NUM1).getInPhase()    == 1;
    assert NUM0.multiply(NUM1).getQuadrature() == 2;
  }

  @Test
  public void testAngle() {
    final ComplexNumber N45  = new ComplexNumber((float) Math.toRadians(45.0));
    final ComplexNumber N135 = new ComplexNumber((float) Math.toRadians(135.0));
    final ComplexNumber N225 = new ComplexNumber((float) Math.toRadians(225.0));
    final ComplexNumber N315 = new ComplexNumber((float) Math.toRadians(315.0));

    assert Math.abs(N45.angleDegrees()  -   45.0f) < 0.0001f;
    assert Math.abs(N135.angleDegrees() -  135.0f) < 0.0001f;
    assert Math.abs(N225.angleDegrees() - -135.0f) < 0.0001f;
    assert Math.abs(N315.angleDegrees() -  -45.0f) < 0.0001f;
  }

}
