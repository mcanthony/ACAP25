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

public class GroupVoiceChannelGrant extends ChannelGrantBlock {

  private final int groupAddress;
  private final int sourceAddress;

  public GroupVoiceChannelGrant(int[] bytes12, boolean isLast, boolean isEncrypted, int opCode) {
    super(bytes12, isLast, isEncrypted, opCode);

    groupAddress  = (bytes12[5] << 8)  + bytes12[6];
    sourceAddress = (bytes12[7] << 16) + (bytes12[8] << 8) + bytes12[9];
  }

  private GroupVoiceChannelGrant(boolean isLast,
                                 boolean isEncrypted,
                                 int     opCode,
                                 int     channelId,
                                 int     channelNumber,
                                 int     groupAddress,
                                 int     sourceAddress)
  {
    super(isLast, isEncrypted, opCode, channelId, channelNumber);

    this.groupAddress  = groupAddress;
    this.sourceAddress = sourceAddress;
  }

  public int getGroupAddress() {
    return groupAddress;
  }

  public int getSourceAddress() {
    return sourceAddress;
  }

  @Override
  public double getDownlinkFreq(IdUpdateBlock idBlock) {
    return idBlock.getBaseFreq() + (channelNumber * idBlock.getChannelSpacing());
  }

  @Override
  public GroupVoiceChannelGrant copy() {
    return new GroupVoiceChannelGrant(
        isLast, isEncrypted, opCode, channelId, channelNumber, groupAddress, sourceAddress
    );
  }

}
