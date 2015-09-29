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

public class ReedSolomon_24_16_9 {

  private final ReedSolomon_63 rs63;

  public ReedSolomon_24_16_9() {
    rs63 = new ReedSolomon_63();
  }

  public int decode(int[] coded24) {
    assert coded24.length == 24;

    int[] coded63 = new int[63];
    for (int i = 0; i < 24; i++)
      coded63[39 + i] = coded24[i];

    int result = rs63.decode(8, 39, coded63);

    for (int i = 0; i < 24; i++)
      coded24[i] = coded63[39 + i];

    return result;
  }

}
