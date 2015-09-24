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

import java.math.BigInteger;

public class Util {

  public static int greatestCommonFactor(long a, long b) {
    return BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).intValue();
  }

  public static int bytesToInt(byte[] bytes, int bitOffset, int bitCount) {
    int bitReadIndex  = bitOffset;
    int byteReadIndex = bitReadIndex / 8;
    int bitWriteCount = 0;
    int result        = 0;

    while (bitWriteCount < bitCount) {
      int value       = bytes[byteReadIndex];
      int bitRelative = bitReadIndex - (8 * byteReadIndex);

      result = Integer.rotateLeft(result, 1) + (Integer.rotateRight(value, 7 - bitRelative) & 0x01);

      bitWriteCount += 1;
      bitReadIndex  += 1;
      byteReadIndex  = bitReadIndex / 8;
    }

    return result;
  }

  public static int[] toBinaryIntArray(byte[] bytes, int bitOffset, int bitCount) {
    int   bitReadIndex  = bitOffset;
    int   byteReadIndex = bitReadIndex / 8;
    int   bitWriteIndex = 0;
    int[] result        = new int[bitCount];

    while (bitWriteIndex < bitCount) {
      int value       = bytes[byteReadIndex];
      int bitRelative = bitReadIndex - (8 * byteReadIndex);

      result[bitWriteIndex++] = Integer.rotateRight(value, 7 - bitRelative) & 0x01;

      bitReadIndex  += 1;
      byteReadIndex  = bitReadIndex / 8;
    }

    return result;
  }

  public static int binaryIntArrayToInt(int[] bits, int bitOffset, int bitCount) {
    int result = 0;

    for (int bit = 0; bit < bitCount; bit++)
      result = Integer.rotateLeft(result, 1) + bits[bit + bitOffset];

    return result;
  }

}
