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

public class ChannelGrantBlock extends TrunkingSignalingBlock {

  protected final int channelId;
  protected final int channelNumber;

  public ChannelGrantBlock(int bytes12[], boolean isLast, boolean isEncrypted, int opCode) {
    super(isLast, isEncrypted, opCode);

    channelId     = (bytes12[3] & 0xF0) >> 4;
    channelNumber = ((bytes12[3] & 0x0F) << 8) + bytes12[4];
  }

  protected ChannelGrantBlock(boolean isLast,
                              boolean isEncrypted,
                              int     opCode,
                              int     channelId,
                              int     channelNumber)
  {
    super(isLast, isEncrypted, opCode);

    this.channelId     = channelId;
    this.channelNumber = channelNumber;
  }

  public int getChannelId() {
    return channelId;
  }

  public int getChannelNumber() {
    return channelNumber;
  }

  @Override
  public ChannelGrantBlock copy() {
    return new ChannelGrantBlock(isLast, isEncrypted, opCode, channelId, channelNumber);
  }

}
