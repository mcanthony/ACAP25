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

package org.anhonesteffort.p25.protocol.frame;

import org.anhonesteffort.p25.ecc.Golay_17_6_8;
import org.anhonesteffort.p25.ecc.ReedSolomon_36_20_17;
import org.anhonesteffort.p25.util.DiBitByteBufferSink;
import org.anhonesteffort.p25.util.Util;

public class HeaderDataUnit extends DataUnit {

  private final byte[]  messageIndicator;
  private final int     manufacturerId;
  private final int     algorithmId;
  private final int     keyId;
  private final int     talkGroupId;
  private final boolean intact;

  public HeaderDataUnit(Nid nid, DiBitByteBufferSink sink) {
    super(nid, sink);

    Golay_17_6_8 golay    = new Golay_17_6_8();
    byte[]       bytes    = sink.getBytes().array();
    int[]        hexBits  = new int[36];
    int          hexCount = 0;

    for (int i = 0; i < 648; i += 18) {
      int   codeword18    = Util.bytesToInt(bytes, i, 18);
      int[] golayResult   = golay.decode(codeword18 >> 1);
      hexBits[hexCount++] = golayResult[1];
    }

    ReedSolomon_36_20_17 reedSolomon = new ReedSolomon_36_20_17();
    int                  rsResult    = reedSolomon.decode(hexBits);

    messageIndicator = new byte[0];
    manufacturerId   = (hexBits[12] << 2) + (hexBits[13] >> 4);
    algorithmId      = ((hexBits[13] & 0x0F) << 4) + (hexBits[14] >> 2);
    keyId            = ((hexBits[14] & 0x03) << 14) + (hexBits[15] << 8) + (hexBits[16] << 2) + (hexBits[17] >> 4);
    talkGroupId      = ((hexBits[17] & 0x0F) << 12) + (hexBits[18] << 6) + hexBits[19];
    intact           = rsResult >= 0;
  }

  private HeaderDataUnit(Nid                 nid,
                         DiBitByteBufferSink sink,
                         byte[]              messageIndicator,
                         int                 manufacturerId,
                         int                 algorithmId,
                         int                 keyId,
                         int                 talkGroupId,
                         boolean             intact)
  {
    super(nid, sink);

    this.messageIndicator = messageIndicator;
    this.manufacturerId   = manufacturerId;
    this.algorithmId      = algorithmId;
    this.keyId            = keyId;
    this.talkGroupId      = talkGroupId;
    this.intact           = intact;
  }

  @Override
  public boolean isIntact() {
    return intact;
  }

  public byte[] getMessageIndicator() {
    return messageIndicator;
  }

  public int getManufacturerId() {
    return manufacturerId;
  }

  public int getAlgorithmId() {
    return algorithmId;
  }

  public int getKeyId() {
    return keyId;
  }

  public int getTalkGroupId() {
    return talkGroupId;
  }

  @Override
  public DataUnit copy() {
    return new HeaderDataUnit(
        nid, sink.copy(), messageIndicator, manufacturerId, algorithmId, keyId, talkGroupId, intact
    );
  }

  @Override
  public String toString() {
    return "[nid: "   + nid.toString() + ", " +
           "intact: " + intact         + ", " +
           "alg: "    + algorithmId    + ", " +
           "tgid: "   + talkGroupId    + ", " +
           "mfid: "   + manufacturerId + "]";
  }

}
