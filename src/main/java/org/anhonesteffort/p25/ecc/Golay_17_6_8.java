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

public class Golay_17_6_8 {

  private final Golay_23_12_8 golay23_12;

  public Golay_17_6_8() {
    golay23_12 = new Golay_23_12_8();
  }

  public int encode(int info6) {
    int code23   = golay23_12.encode(info6);
    int parity11 = code23 & 0x7FF;
    return parity11 | (info6 << 11);
  }

  public int[] decode(int codeword17) {
    int parity11 = codeword17 & 0x7FF;
    int info6    = (codeword17 >> 11) & 0x3F;
    return golay23_12.decode(parity11 | (info6 << 11));
  }

}
