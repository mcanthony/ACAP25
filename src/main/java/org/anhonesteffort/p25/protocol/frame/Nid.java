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

import org.anhonesteffort.p25.ecc.DecoderProvider;
import org.anhonesteffort.p25.ecc.BchDecoder;
import org.anhonesteffort.p25.protocol.P25;
import org.anhonesteffort.p25.util.Util;

public class Nid {

  private final int     nac;
  private final Duid    duid;
  private final boolean intact;

  public Nid(int nac, Duid duid) {
    this.nac    = nac;
    this.duid   = duid;
    this.intact = true;
  }

  public Nid(byte[] eccBytes) {
    BchDecoder decoder     = DecoderProvider.bchDecoderFor(P25.RS_ERROR_NID);
    int[]      encodedBits = Util.toBinaryIntArray(eccBytes, 0, 63);
    int[]      decodedBits = new int[63];

    intact = decoder.decode(encodedBits, decodedBits);
    nac    = Util.binaryIntArrayToInt(decodedBits, 0, 12);
    duid   = new Duid(Util.binaryIntArrayToInt(decodedBits, 12, 4));
  }

  public int getNac() {
    return nac;
  }

  public Duid getDuid() {
    return duid;
  }

  public boolean isIntact() {
    return intact;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)                               return true;
    if (o == null || getClass() != o.getClass()) return false;

    Nid nid = (Nid) o;
    return nac == nid.nac && duid.equals(nid.duid) && intact == nid.intact;
  }

  @Override
  public String toString() {
    return "[duid: " + duid.toString() + ", nac: " + nac + "]";
  }

}
