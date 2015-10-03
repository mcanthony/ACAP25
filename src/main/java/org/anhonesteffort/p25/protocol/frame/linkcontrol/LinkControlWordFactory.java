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

import org.anhonesteffort.p25.protocol.P25;

public class LinkControlWordFactory {

  public LinkControlWord getLinkControlFor(int[] hexBits12) {
    boolean protectedFlag     = (hexBits12[0] >> 5) == 1;
    boolean implicitMfid      = ((hexBits12[0] & 0x10) >> 4) == 1;
    int     linkControlOpcode = ((hexBits12[0] & 0x0F) << 2) + (hexBits12[1] >> 4);

    switch (linkControlOpcode) {
      case P25.LCO_GROUP_VOICE_USER:
        return new GroupVoiceUserLwc(hexBits12, protectedFlag, implicitMfid, linkControlOpcode);

      case P25.LCO_UNIT_TO_UNIT_USER:
        return new UnitToUnitVoiceUserLwc(hexBits12, protectedFlag, implicitMfid, linkControlOpcode);

      default:
        return new LinkControlWord(protectedFlag, implicitMfid, linkControlOpcode);
    }
  }

}
