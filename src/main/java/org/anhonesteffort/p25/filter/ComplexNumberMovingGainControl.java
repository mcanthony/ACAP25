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

package org.anhonesteffort.p25.filter;

import org.anhonesteffort.p25.primitive.ComplexNumber;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.Queue;
import java.util.stream.IntStream;

public class ComplexNumberMovingGainControl extends ConsistentRateFilter<ComplexNumber> {

  public static final float MINIMUM_ENVELOPE = 0.0001f;

  private final Queue<Float> history;
  private       float        maxEnvelope = 0.0f;
  private       float        gain        = 1.0f;

  public ComplexNumberMovingGainControl(int historyLength) {
    history = new CircularFifoQueue<>(historyLength);
    IntStream.range(0, historyLength)
             .forEach(i -> history.offer(MINIMUM_ENVELOPE));
  }

  @Override
  protected ComplexNumber getNextOutput(ComplexNumber input) {
    float envelope       = input.envelope();
    float oldestEnvelope = history.remove();

    history.offer(envelope);

    if (envelope > maxEnvelope) {
      maxEnvelope = envelope;
      gain        = 1.0f / maxEnvelope;
    } else if (maxEnvelope == oldestEnvelope) {
      maxEnvelope = history.stream().max(Float::compare).get();
      gain        = 1.0f / maxEnvelope;
    }

    return input.multiply(gain);
  }

}
