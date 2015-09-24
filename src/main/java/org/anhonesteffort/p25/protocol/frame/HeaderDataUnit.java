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
import org.anhonesteffort.p25.util.DiBitByteBufferSink;
import org.anhonesteffort.p25.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeaderDataUnit extends DataUnit {

  private static final Logger log = LoggerFactory.getLogger(HeaderDataUnit.class);

  private final byte[]  messageIndicator;
  private final int     manufacturerId;
  private final int     algorithmId;
  private final int     keyId;
  private final int     talkGroupId;
  private final boolean intact;

  public HeaderDataUnit(Nid nid, DiBitByteBufferSink sink) {
    super(nid, sink);

    byte[]       bytes  = sink.getBytes().array();
    Golay_17_6_8 golay  = new Golay_17_6_8();
    int          errors = 0;

    for (int i = 0; i < 648; i += 18) {
      int   codeword18 = Util.bytesToInt(bytes, i, 18);
      int[] decode     = golay.decode(codeword18 >> 1);

      errors+= decode[0];
    }

    log.debug("header data word golay error count: " + errors);

    // todo: error correct with RS
    messageIndicator = new byte[0];
    manufacturerId   = 0;
    algorithmId      = 0;
    keyId            = 0;
    talkGroupId      = 0;
    intact           = false;
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
           "tgid: "   + talkGroupId    + "] ";
  }

}
