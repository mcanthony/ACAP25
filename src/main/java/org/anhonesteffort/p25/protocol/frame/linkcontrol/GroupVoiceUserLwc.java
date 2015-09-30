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

package org.anhonesteffort.p25.protocol.frame.linkcontrol;

public class GroupVoiceUserLwc extends LinkControlWord {

  private final int     manufacturerId;
  private final boolean emergency;
  private final int     talkGroupId;
  private final int     sourceId;

  public GroupVoiceUserLwc(int[]   hexBits12,
                           boolean protectedFlag,
                           boolean implicitMfid,
                           int     linkControlOpcode)
  {
    super(protectedFlag, implicitMfid, linkControlOpcode);

    manufacturerId = ((hexBits12[1] & 0x0F) << 4) + (hexBits12[2] >> 2);
    emergency      = (hexBits12[2] & 0x02) == 0x02;
    talkGroupId    = ((hexBits12[5] & 0x0F) << 12) + (hexBits12[6] << 6) + hexBits12[7];
    sourceId       = (hexBits12[8] << 18) + (hexBits12[9] << 12) + (hexBits12[10] << 6) + hexBits12[11];
  }

  private GroupVoiceUserLwc(boolean protectedFlag,
                            boolean implicitMfid,
                            int     linkControlOpcode,
                            int     manufacturerId,
                            boolean emergency,
                            int     talkGroupId,
                            int     sourceId)
  {
    super(protectedFlag, implicitMfid, linkControlOpcode);

    this.manufacturerId = manufacturerId;
    this.emergency      = emergency;
    this.talkGroupId    = talkGroupId;
    this.sourceId       = sourceId;
  }

  public int getManufacturerId() {
    return manufacturerId;
  }

  public boolean isEmergency() {
    return emergency;
  }

  public int getTalkGroupId() {
    return talkGroupId;
  }

  public int getSourceId() {
    return sourceId;
  }

  @Override
  public GroupVoiceUserLwc copy() {
    return new GroupVoiceUserLwc(
        protectedFlag, implicitMfid, linkControlOpcode, manufacturerId, emergency, talkGroupId, sourceId
    );
  }

  @Override
  public String toString() {
    return "[p: "   + protectedFlag     + ", " +
           "lco: "  + linkControlOpcode + ", " +
           "e: "    + emergency         + ", " +
           "tgid: " + talkGroupId       + ", " +
           "src: "  + sourceId          + "]";
  }

}
