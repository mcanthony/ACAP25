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

import org.anhonesteffort.p25.ecc.Hamming_10_6_3;
import org.anhonesteffort.p25.util.DiBitByteBufferSink;
import org.anhonesteffort.p25.util.Util;

import java.util.BitSet;
import java.util.stream.IntStream;

public abstract class LogicalLinkDataUnit extends DataUnit {

  private static final int[] VOICE_CODE_WORD_INDEXES = new int[] {
      0, 144, 328, 512, 696, 880, 1064, 1248, 1424
  };

  protected final int[]           rsHexbits24    = new int[24];
  protected final VoiceCodeWord[] voiceCodeWords = new VoiceCodeWord[9];

  public LogicalLinkDataUnit(Nid nid, DiBitByteBufferSink sink) {
    super(nid, sink);

    Hamming_10_6_3 hamming  = new Hamming_10_6_3();
    byte[]         bytes    = sink.getBytes().array();
    int            hexCount = 0;

    for (int i = 288; i < 1248; i += 184) {
      for (int j = 0; j < 40; j += 10) {
        int codeword10 = Util.bytesToInt(bytes, i + j, 10);
        int info6      = codeword10 >> 4;
        int parity4    = codeword10 & 0x0F;

        rsHexbits24[hexCount++] = hamming.decode(info6, parity4);
      }
    }

    // todo: gross
    BitSet bitSet = new BitSet(bytes.length * 8);
    IntStream.range(0, bytes.length * 8)
             .filter(bit -> Util.bytesToInt(bytes, bit, 1) == 1)
             .forEach(bitSet::set);

    int voiceCwCount = 0;
    for (int bitIndex : VOICE_CODE_WORD_INDEXES) {
      voiceCodeWords[voiceCwCount++] = new VoiceCodeWord(
          bitSet.get(bitIndex, bitIndex + 144).toByteArray()
      );
    }
  }

  public VoiceCodeWord[] getVoiceCodeWords() {
    return voiceCodeWords;
  }

}
