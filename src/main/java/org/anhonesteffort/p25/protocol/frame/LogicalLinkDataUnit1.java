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

import org.anhonesteffort.p25.ecc.ReedSolomon_24_12_13;
import org.anhonesteffort.p25.util.DiBitByteBufferSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogicalLinkDataUnit1 extends LogicalLinkDataUnit {

  private static final Logger log = LoggerFactory.getLogger(LogicalLinkDataUnit1.class);

  private final int             manufacturerId;
  private final int             talkGroupId;
  private final int             destinationId;
  private final int             sourceId;
  private final boolean         emergency;
  private final VoiceCodeWord[] voiceCodeWords;
  private final boolean         intact;

  public LogicalLinkDataUnit1(Nid nid, DiBitByteBufferSink sink) {
    super(nid, sink);

    ReedSolomon_24_12_13 reedSolomon = new ReedSolomon_24_12_13();
    int                  rsResult    = reedSolomon.decode(rsLinkControl);

    if (rsLinkControl[0] != 0 && rsResult >= 0)
      log.warn("FIRST HEX BIT IS NOT ZERO: " + rsLinkControl[0]);

    int lcFormat   = rsLinkControl[1] >> 4;
    manufacturerId = ((rsLinkControl[1] & 0x0F) << 4) + (rsLinkControl[2] >> 2);
    sourceId       = (rsLinkControl[8] << 18) + (rsLinkControl[9] << 12) + (rsLinkControl[10] << 6) + rsLinkControl[11];

    if (lcFormat == 0x00) {
      talkGroupId   = ((rsLinkControl[5] & 0x0F) << 12) + (rsLinkControl[6] << 6) + rsLinkControl[7];
      emergency     = (rsLinkControl[2] & 0x02) == 0x02;
      destinationId = -1;
    } else if (lcFormat == 0x03) {
      destinationId = (rsLinkControl[4] << 18) + (rsLinkControl[5] << 12) + (rsLinkControl[6] << 6) + rsLinkControl[7];
      talkGroupId   = -1;
      emergency     = false;
    } else {
      if (rsResult >= 0)
        log.warn("LINK CONTROL FORMAT IS " + lcFormat + ", WHY? D:");

      talkGroupId   = -1;
      destinationId = -1;
      emergency     = false;
    }

    voiceCodeWords = new VoiceCodeWord[0];
    intact         = rsResult >= 0;

    log.debug("decoded to: " + toString());
  }

  private LogicalLinkDataUnit1(Nid                 nid,
                               DiBitByteBufferSink sink,
                               int                 manufacturerId,
                               int                 talkGroupId,
                               int                 destinationId,
                               int                 sourceId,
                               boolean             emergency,
                               VoiceCodeWord[]     voiceCodeWords,
                               boolean             intact)
  {
    super(nid, sink);

    this.manufacturerId = manufacturerId;
    this.talkGroupId    = talkGroupId;
    this.destinationId  = destinationId;
    this.sourceId       = sourceId;
    this.emergency      = emergency;
    this.voiceCodeWords = voiceCodeWords; // todo
    this.intact         = intact;
  }

  @Override
  public boolean isIntact() {
    return intact;
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

  public int getDestinationId() {
    return destinationId;
  }

  public int getSourceId() {
    return sourceId;
  }

  public VoiceCodeWord[] getVoiceCodeWords() {
    return voiceCodeWords;
  }

  @Override
  public DataUnit copy() {
    return new LogicalLinkDataUnit1(
        nid, sink.copy(), manufacturerId, talkGroupId, destinationId, sourceId, emergency, voiceCodeWords, intact
    );
  }

  @Override
  public String toString() {
    return "[nid: "      + nid.toString() + ", " +
           "intact: "    + intact         + ", " +
           "emergency: " + emergency      + ", " +
           "mfid: "      + manufacturerId + ", " +
           "src: "       + sourceId       + ", " +
           "tgid: "      + talkGroupId    + ", " +
           "dst: "       + destinationId  + "]";
  }

}
