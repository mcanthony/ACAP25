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

import org.anhonesteffort.p25.ecc.DeinterleaveTrellisDecoder;
import org.anhonesteffort.p25.protocol.P25;
import org.anhonesteffort.p25.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class TrunkingSignalingBlockFactory {

  private static final Logger log = LoggerFactory.getLogger(TrunkingSignalingBlockFactory.class);

  private TrunkingSignalingBlock getBlockFor(byte[] bytes12) {
    int[] intBytes12 = new int[12];
    IntStream.range(0, 12).forEach(i -> intBytes12[i] = (bytes12[i] & 0xFF));

    boolean isLast      = (intBytes12[0] & 0x80) == 0x80;
    boolean isEncrypted = (intBytes12[0] & 0x40) == 0x40;
    int     opCode      =  intBytes12[0] & 0x3F;

    switch (opCode) {
      case P25.TSBK_GROUP_VOICE_CHAN_GRANT:
        return new GroupVoiceChannelGrant(intBytes12, isLast, isEncrypted, opCode);

      case P25.TSBK_ID_UPDATE_VUHF:
        return new IdUpdateVuhf(intBytes12, isLast, isEncrypted, opCode);

      case P25.TSBK_NETWORK_STATUS:
        return new NetworkStatusBroadcastMessage(intBytes12, isLast, isEncrypted, opCode);

      case P25.TSBK_ID_UPDATE_NO_VUHF:
        return new IdUpdateNoVuhf(intBytes12, isLast, isEncrypted, opCode);

      default:
        return new TrunkingSignalingBlock(isLast, isEncrypted, opCode);
    }
  }

  public List<TrunkingSignalingBlock> getBlocksFor(byte[] bytes) {
    List<TrunkingSignalingBlock> blocks  = new LinkedList<>();
    DeinterleaveTrellisDecoder   decoder = new DeinterleaveTrellisDecoder();

    for (int bit = 0; bit < (bytes.length * 8); bit += 196) {
      int[]  bits196 = Util.toBinaryIntArray(bytes, bit, 196);
      byte[] bytes12 = new byte[12];
      int    result  = decoder.decode(bits196, bytes12);

      if (result == 0) {
        TrunkingSignalingBlock block = getBlockFor(bytes12);
        blocks.add(block);

        if (block.isLast()) {
          break;
        }
      } else {
        log.warn("unable to recover block #" + (bit / 196));
        break;
      }
    }

    return blocks;
  }

}
