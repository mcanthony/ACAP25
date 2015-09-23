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

package org.anhonesteffort.p25.ecc;

import org.anhonesteffort.p25.protocol.P25;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DecoderProvider {

  private static final Map<String, ReedSolomonDecoder> cache = new HashMap<>();

  public static ReedSolomonDecoder rsDecoderFor(int TT) {
    int    MM         = P25.RS_MM;
    int[]  polynomial = P25.RS_POLYNOMIAL;
    String key        = MM + "_" + TT + "_" + Arrays.toString(polynomial);

    ReedSolomonDecoder decoder = cache.get(key);
    if (decoder != null)
      return decoder;

    decoder = ReedSolomonDecoder.newInstance(MM, TT, polynomial);
    cache.put(key, decoder);

    return decoder;
  }

}
