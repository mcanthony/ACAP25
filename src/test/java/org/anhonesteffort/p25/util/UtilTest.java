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

import java.util.List;

public class UtilTest {

  @Test
  public void testBinaryIntArrayToInt() {
    int[] INTS00 = new int[] {0, 1, 0, 1};
    assert (Util.binaryIntArrayToInt(INTS00, 0, 4) & 0x05) == 0x05;

    int[] INTS01 = new int[] {1, 1, 0, 0, 0};
    assert (Util.binaryIntArrayToInt(INTS01, 0, 5) & 0x18) == 0x18;

    int[] INTS02 = new int[] {1, 1, 1, 1, 1, 1, 1, 1};
    assert (Util.binaryIntArrayToInt(INTS02, 0, 8) & 0xFF) == 0xFF;

    int[] INTS03 = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
    assert Util.binaryIntArrayToInt(INTS03, 0, 8) == 0;

    int[] INTS04 = new int[] {0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1};
    assert (Util.binaryIntArrayToInt(INTS04, 0, 14) & 0x3C3) == 0x3C3;
  }

  @Test
  public void testToBinaryIntArray() {
    byte[] BYTES00 = new byte[] {0x05, 0x03};

    assert Util.toBinaryIntArray(BYTES00, 0, 4).length == 4;
    assert Util.toBinaryIntArray(BYTES00, 0, 4)[0] == 0;
    assert Util.toBinaryIntArray(BYTES00, 0, 4)[1] == 0;
    assert Util.toBinaryIntArray(BYTES00, 0, 4)[2] == 0;
    assert Util.toBinaryIntArray(BYTES00, 0, 4)[3] == 0;

    assert Util.toBinaryIntArray(BYTES00, 0, 8).length == 8;
    assert Util.toBinaryIntArray(BYTES00, 0, 8)[4] == 0;
    assert Util.toBinaryIntArray(BYTES00, 0, 8)[5] == 1;
    assert Util.toBinaryIntArray(BYTES00, 0, 8)[6] == 0;
    assert Util.toBinaryIntArray(BYTES00, 0, 8)[7] == 1;

    assert Util.toBinaryIntArray(BYTES00, 8, 12).length == 4;
    assert Util.toBinaryIntArray(BYTES00, 8, 12)[0] == 0;
    assert Util.toBinaryIntArray(BYTES00, 8, 12)[1] == 0;
    assert Util.toBinaryIntArray(BYTES00, 8, 12)[2] == 0;
    assert Util.toBinaryIntArray(BYTES00, 8, 12)[3] == 0;

    assert Util.toBinaryIntArray(BYTES00, 12, 16).length == 4;
    assert Util.toBinaryIntArray(BYTES00, 12, 16)[0] == 0;
    assert Util.toBinaryIntArray(BYTES00, 12, 16)[1] == 0;
    assert Util.toBinaryIntArray(BYTES00, 12, 16)[2] == 1;
    assert Util.toBinaryIntArray(BYTES00, 12, 16)[3] == 1;

    byte[] BYTES01 = new byte[] {0x01};

    assert Util.toBinaryIntArray(BYTES01, 0, 8).length == 8;
    assert Util.toBinaryIntArray(BYTES01, 0, 8)[0] == 0;
    assert Util.toBinaryIntArray(BYTES01, 0, 8)[1] == 0;
    assert Util.toBinaryIntArray(BYTES01, 0, 8)[2] == 0;
    assert Util.toBinaryIntArray(BYTES01, 0, 8)[3] == 0;

    assert Util.toBinaryIntArray(BYTES01, 0, 8)[4] == 0;
    assert Util.toBinaryIntArray(BYTES01, 0, 8)[5] == 0;
    assert Util.toBinaryIntArray(BYTES01, 0, 8)[6] == 0;
    assert Util.toBinaryIntArray(BYTES01, 0, 8)[7] == 1;

    byte[] BYTES02 = new byte[] {0x01, 0x02};

    assert Util.toBinaryIntArray(BYTES02, 0, 16).length == 16;
    assert Util.toBinaryIntArray(BYTES02, 0, 16)[0] == 0;
    assert Util.toBinaryIntArray(BYTES02, 0, 16)[1] == 0;
    assert Util.toBinaryIntArray(BYTES02, 0, 16)[2] == 0;
    assert Util.toBinaryIntArray(BYTES02, 0, 16)[3] == 0;

    assert Util.toBinaryIntArray(BYTES02, 4, 5)[0] == 0;
    assert Util.toBinaryIntArray(BYTES02, 5, 6)[0] == 0;
    assert Util.toBinaryIntArray(BYTES02, 6, 7)[0] == 0;
    assert Util.toBinaryIntArray(BYTES02, 7, 8)[0] == 1;

    assert Util.toBinaryIntArray(BYTES02, 0, 16)[8]  == 0;
    assert Util.toBinaryIntArray(BYTES02, 0, 16)[9]  == 0;
    assert Util.toBinaryIntArray(BYTES02, 0, 16)[10] == 0;
    assert Util.toBinaryIntArray(BYTES02, 0, 16)[11] == 0;

    assert Util.toBinaryIntArray(BYTES02,  0, 16)[12] == 0;
    assert Util.toBinaryIntArray(BYTES02,  0, 16)[13] == 0;
    assert Util.toBinaryIntArray(BYTES02, 14, 16)[0]  == 1;
    assert Util.toBinaryIntArray(BYTES02, 14, 16)[1]  == 0;

    assert Util.toBinaryIntArray(new byte[3], 0, 24).length == 24;
  }

}
