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

import org.anhonesteffort.p25.util.Copyable;

public class DiBit implements Copyable<DiBit> {

  public static final DiBit D0 = new DiBit(0);
  public static final DiBit D1 = new DiBit(1);
  public static final DiBit D2 = new DiBit(2);
  public static final DiBit D3 = new DiBit(3);

  private final int value;

  public DiBit(int value) {
    if (value < 0 || value > 3)
      throw new IllegalArgumentException("don't");

    this.value = value;
  }

  public int getValue() {
    return value;
  }

  @Override
  public DiBit copy() {
    return new DiBit(value);
  }

  @Override
  public String toString() {
    switch (value) {
      case 0:
        return "00";

      case 1:
        return "01";

      case 2:
        return "10";

      case 3:
        return "11";

      default:
        return "?!";
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DiBit diBit = (DiBit) o;

    return value == diBit.value;
  }

}
