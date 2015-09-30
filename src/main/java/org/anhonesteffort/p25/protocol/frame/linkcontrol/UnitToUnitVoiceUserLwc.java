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

public class UnitToUnitVoiceUserLwc extends LinkControlWord {

  private final int manufacturerId;
  private final int destinationId;
  private final int sourceId;

  public UnitToUnitVoiceUserLwc(int[]   hexBits12,
                                boolean protectedFlag,
                                boolean implicitMfid,
                                int     linkControlOpcode)
  {
    super(protectedFlag, implicitMfid, linkControlOpcode);

    manufacturerId = ((hexBits12[1] & 0x0F) << 4) + (hexBits12[2] >> 2);
    destinationId  = (hexBits12[4] << 18) + (hexBits12[5] << 12) + (hexBits12[6] << 6)  + hexBits12[7];
    sourceId       = (hexBits12[8] << 18) + (hexBits12[9] << 12) + (hexBits12[10] << 6) + hexBits12[11];
  }

  private UnitToUnitVoiceUserLwc(boolean protectedFlag,
                                 boolean implicitMfid,
                                 int     linkControlOpcode,
                                 int     manufacturerId,
                                 int     destinationId,
                                 int     sourceId)
  {
    super(protectedFlag, implicitMfid, linkControlOpcode);

    this.manufacturerId = manufacturerId;
    this.destinationId  = destinationId;
    this.sourceId       = sourceId;
  }

  public int getManufacturerId() {
    return manufacturerId;
  }

  public int getDestinationId() {
    return destinationId;
  }

  public int getSourceId() {
    return sourceId;
  }

  @Override
  public UnitToUnitVoiceUserLwc copy() {
    return new UnitToUnitVoiceUserLwc(
        protectedFlag, implicitMfid, linkControlOpcode, manufacturerId, destinationId, sourceId
    );
  }

  @Override
  public String toString() {
    return "[p: "  + protectedFlag     + ", " +
           "lco: " + linkControlOpcode + ", " +
           "dst: " + destinationId     + ", " +
           "src: " + sourceId          + "]";
  }

}
