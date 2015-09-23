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

package org.anhonesteffort.p25.filter.gate;

import org.anhonesteffort.p25.primitive.DiBit;

public class DiBitSyncGate extends SyncGate<DiBit> {

  private final long sync;
  private final long mask;
  private final int  hammingDistance;
  private       long bits;

  public DiBitSyncGate(long sync, int bitLength, int hammingDistance) {
    this.sync            = sync;
    mask                 = (long) (Math.pow(2, bitLength) - 1);
    this.hammingDistance = hammingDistance;
  }

  public long getSync() {
    return sync;
  }

  @Override
  public void consume(DiBit element) {
    if (bits == sync) {
      broadcast(element);
      return;
    }

    bits  = Long.rotateLeft(bits, 2) & mask;
    bits += element.getValue();

    if (bits == sync) {
      onSyncConsumed();
    } else if (Long.bitCount(bits ^ sync) <= hammingDistance) {
        bits = sync;
        onSyncConsumed();
    }
  }

}
