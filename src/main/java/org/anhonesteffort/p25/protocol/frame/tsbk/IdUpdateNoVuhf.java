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

package org.anhonesteffort.p25.protocol.frame.tsbk;

public class IdUpdateNoVuhf extends IdUpdateBlock {

  private final int bandwidth;
  private final int transmitOffset;

  public IdUpdateNoVuhf(int[] bytes12, boolean isLast, boolean isEncrypted, int opCode) {
    super(bytes12, isLast, isEncrypted, opCode);

    bandwidth      = ((bytes12[2] & 0x0F) << 5) + ((bytes12[3] & 0xF8) >> 3);
    transmitOffset = ((bytes12[3] & 0x07) << 6) + ((bytes12[4] & 0xFC) >> 2);
  }

  public IdUpdateNoVuhf(boolean isLast,
                        boolean isEncrypted,
                        int     opCode,
                        int     id,
                        int     channelSpacing,
                        long    baseFreq,
                        int     bandwidth,
                        int     transmitOffset)
  {
    super(isLast, isEncrypted, opCode, id, channelSpacing, baseFreq);

    this.bandwidth      = bandwidth;
    this.transmitOffset = transmitOffset;
  }

  @Override
  public int getBandwidth() {
    return bandwidth;
  }

  @Override
  public long getTransmitOffset() {
    return transmitOffset;
  }

  @Override
  public IdUpdateNoVuhf copy() {
    return new IdUpdateNoVuhf(
        isLast, isEncrypted, opCode, id, channelSpacing, baseFreq, bandwidth, transmitOffset
    );
  }
}
