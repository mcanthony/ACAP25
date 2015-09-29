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
 *
 * Derived from:
 *   OP25 - rs.cc (Copyright 2013 KA1RBI)
 */

package org.anhonesteffort.p25.ecc;

public class Hamming_10_6_3 {

  private static final int[] hmg1063DecTbl = new int[] {
      0, 0, 0, 2, 0, 0, 0, 4, 0, 0, 0, 8, 1, 16, 32, 0
  };

  private static final int[] hmg1063EncTbl = new int[] {
      0, 12, 3, 15, 7, 11, 4, 8, 11, 7, 8, 4, 12, 0, 15, 3,
      13, 1, 14, 2, 10, 6, 9, 5, 6, 10, 5, 9, 1, 13, 2, 14,
      14, 2, 13, 1, 9, 5, 10, 6, 5, 9, 6, 10, 2, 14, 1, 13,
      3, 15, 0, 12, 4, 8, 7, 11, 8, 4, 11, 7, 15, 3, 12, 0
  };

  public int decode(int info, int parity) {
    assert info <= 0x3F && parity <= 0x0F;
    return info ^ hmg1063DecTbl[hmg1063EncTbl[info] ^ parity];
  }

}
