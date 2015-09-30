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

import org.anhonesteffort.p25.ecc.ReedSolomon_24_16_9;
import org.anhonesteffort.p25.util.DiBitByteBufferSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogicalLinkDataUnit2 extends LogicalLinkDataUnit {

  private static final Logger log = LoggerFactory.getLogger(LogicalLinkDataUnit2.class);

  private final byte[]          messageIndicator;
  private final int             algorithmId;
  private final int             keyId;
  private final boolean         intact;

  public LogicalLinkDataUnit2(Nid nid, DiBitByteBufferSink sink) {
    super(nid, sink);

    ReedSolomon_24_16_9 reedSolomon = new ReedSolomon_24_16_9();
    int                 rsResult    = reedSolomon.decode(rsHexbits24);

    messageIndicator = new byte[0];
    algorithmId      = (rsHexbits24[12] << 2) + (rsHexbits24[13] >> 4);
    keyId            = ((rsHexbits24[13] & 0x0F) << 12) + (rsHexbits24[14] << 6) + rsHexbits24[15];
    intact           = rsResult >= 0;

    log.debug("decoded to: " + toString());
  }

  private LogicalLinkDataUnit2(Nid                 nid,
                               DiBitByteBufferSink sink,
                               byte[]              messageIndicator,
                               int                 algorithmId,
                               int                 keyId,
                               boolean             intact)
  {
    super(nid, sink);

    this.messageIndicator = messageIndicator;
    this.algorithmId      = algorithmId;
    this.keyId            = keyId;
    this.intact           = intact;
  }

  @Override
  public boolean isIntact() {
    return intact;
  }

  public byte[] getMessageIndicator() {
    return messageIndicator;
  }

  public int getAlgorithmId() {
    return algorithmId;
  }

  public int getKeyId() {
    return keyId;
  }

  @Override
  public DataUnit copy() {
    return new LogicalLinkDataUnit2(
        nid, sink.copy(), messageIndicator, algorithmId, keyId, intact
    );
  }

  @Override
  public String toString() {
    return "[nid: "   + nid.toString() + ", " +
           "intact: " + intact         + ", " +
           "alg: "    + algorithmId    + ", " +
           "kid: "    + keyId          + "]";
  }

}
