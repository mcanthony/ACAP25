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

import org.anhonesteffort.p25.Sink;
import org.anhonesteffort.p25.primitive.DiBit;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class DiBitByteBufferSink implements Copyable<DiBitByteBufferSink>, Sink<DiBit> {

  private final ByteBuffer byteBuffer;
  private final int        bitLength;
  private       int        bitCount = 0;

  private DiBitByteBufferSink(int bitLength, int bitCount, ByteBuffer byteBuffer) {
    this.bitLength  = bitLength;
    this.bitCount   = bitCount;
    this.byteBuffer = byteBuffer;
  }

  public DiBitByteBufferSink(int bitLength) {
    this.bitLength = bitLength;
    byteBuffer     = ByteBuffer.allocate((int) Math.ceil(bitLength / 8.0d));
  }

  public boolean isFull() {
    return bitCount == bitLength;
  }

  public ByteBuffer getBytes() {
    return byteBuffer;
  }

  @Override
  public void consume(DiBit element) {
    int byteIndex = (bitCount + 1) / 8;
    int previous  = byteBuffer.get(byteIndex);

    byteBuffer.put(byteIndex,
        (byte) (Integer.rotateLeft(previous, 2) + element.getValue())
    );

    bitCount += 2;
  }

  @Override
  public DiBitByteBufferSink copy() {
    byte[] bytes = byteBuffer.array();
    return new DiBitByteBufferSink(
        bitLength,
        bitCount,
        ByteBuffer.wrap(Arrays.copyOf(bytes, bytes.length))
    );
  }

}
