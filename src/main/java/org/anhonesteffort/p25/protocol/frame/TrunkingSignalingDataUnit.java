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

import org.anhonesteffort.p25.protocol.frame.tsbk.TrunkingSignalingBlockFactory;
import org.anhonesteffort.p25.protocol.frame.tsbk.TrunkingSignalingBlock;
import org.anhonesteffort.p25.util.DiBitByteBufferSink;

import java.util.List;
import java.util.Optional;

public class TrunkingSignalingDataUnit extends DataUnit {

  private final List<TrunkingSignalingBlock> blocks;
  private final boolean intact;

  public TrunkingSignalingDataUnit(Nid nid, DiBitByteBufferSink sink) {
    super(nid, sink);

    blocks = new TrunkingSignalingBlockFactory().getBlocksFor(sink.getBytes().array());
    intact = blocks.size() > 0;
  }

  private TrunkingSignalingDataUnit(Nid                          nid,
                                    DiBitByteBufferSink          sink,
                                    List<TrunkingSignalingBlock> blocks,
                                    boolean                      intact)
  {
    super(nid, sink);

    this.blocks = blocks;
    this.intact = intact;
  }

  @Override
  public boolean isIntact() {
    return intact;
  }

  public List<TrunkingSignalingBlock> getBlocks() {
    return blocks;
  }

  public Optional<TrunkingSignalingBlock> getFirstOf(int opCode) {
    return blocks.stream()
                 .filter(block -> block.getOpCode() == opCode)
                 .findFirst();
  }

  @Override
  public TrunkingSignalingDataUnit copy() {
    return new TrunkingSignalingDataUnit(nid, sink.copy(), blocks, intact);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("[nid: ");
    builder.append(nid.toString());
    builder.append(", intact: ");
    builder.append(intact);
    builder.append(", blocks: [");

    blocks.forEach(block -> {
      builder.append(block);
      builder.append(", ");
    });
    builder.append("]]");

    return builder.toString();
  }

}
