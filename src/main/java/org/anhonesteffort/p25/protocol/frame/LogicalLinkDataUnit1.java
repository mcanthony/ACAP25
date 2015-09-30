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
import org.anhonesteffort.p25.protocol.frame.linkcontrol.LinkControlWord;
import org.anhonesteffort.p25.protocol.frame.linkcontrol.LinkControlWordFactory;
import org.anhonesteffort.p25.util.DiBitByteBufferSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogicalLinkDataUnit1 extends LogicalLinkDataUnit {

  private static final Logger log = LoggerFactory.getLogger(LogicalLinkDataUnit1.class);

  private final LinkControlWord linkControlWord;
  private final boolean         intact;

  public LogicalLinkDataUnit1(Nid nid, DiBitByteBufferSink sink) {
    super(nid, sink);

    ReedSolomon_24_12_13 reedSolomon = new ReedSolomon_24_12_13();
    int                  rsResult    = reedSolomon.decode(rsHexbits24);

    linkControlWord = new LinkControlWordFactory().getLinkControlFor(rsHexbits24);
    intact          = rsResult >= 0;

    log.debug("decoded to: " + toString());
  }

  private LogicalLinkDataUnit1(Nid                 nid,
                               DiBitByteBufferSink sink,
                               LinkControlWord     linkControlWord,
                               boolean             intact)
  {
    super(nid, sink);

    this.linkControlWord = linkControlWord;
    this.intact          = intact;
  }

  @Override
  public boolean isIntact() {
    return intact;
  }

  public LinkControlWord getLinkControlWord() {
    return linkControlWord;
  }

  @Override
  public DataUnit copy() {
    return new LogicalLinkDataUnit1(
        nid, sink.copy(), linkControlWord, intact
    );
  }

  @Override
  public String toString() {
    return "[nid: "   + nid.toString()             + ", " +
           "intact: " + intact                     + ", " +
           "link: "   + linkControlWord.toString() + "] ";
  }

}
