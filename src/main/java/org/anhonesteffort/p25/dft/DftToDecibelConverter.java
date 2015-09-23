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

package org.anhonesteffort.p25.dft;

import org.anhonesteffort.p25.Sink;
import org.anhonesteffort.p25.Source;
import org.anhonesteffort.p25.primitive.ComplexNumber;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DftToDecibelConverter extends Source<DftFrame, Sink<DftFrame>> implements Sink<DftFrame> {

  @Override
  public void consume(DftFrame frame) {
    float[] complexFrame = frame.getFrame();
    float[] dbFrame      = new float[complexFrame.length / 2];
    int     halfIndex    = dbFrame.length / 2;

    List<Float> magFrame =
        IntStream.range(0, complexFrame.length)
                 .filter(i -> (i & 1) == 0)
                 .mapToObj(i -> new ComplexNumber(complexFrame[i], complexFrame[i + 1]).magnitude())
                 .collect(Collectors.toList());

    float largestMagnitude = magFrame.stream().max(Float::compare).get();

    IntStream.range(0, dbFrame.length)
             .forEach(i -> {
               if (i >= halfIndex)
                 dbFrame[i - halfIndex] = 20.0f * (float) Math.log10(magFrame.get(i) / largestMagnitude);
               else
                 dbFrame[i + halfIndex] = 20.0f * (float) Math.log10(magFrame.get(i) / largestMagnitude);
             });

    broadcast(new DftFrame(dbFrame));
  }

}
