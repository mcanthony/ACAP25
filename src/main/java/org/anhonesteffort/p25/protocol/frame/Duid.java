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

public class Duid {

  public static final int ID_HEADER             = 0x00;
  public static final int ID_TERMINATOR_WO_LINK = 0x03;
  public static final int ID_LLDU1              = 0x05;
  public static final int ID_TRUNK_SIGNALING    = 0x07;
  public static final int ID_LLDU2              = 0x0A;
  public static final int ID_PACKET             = 0x0C;
  public static final int ID_TERMINATOR_W_LINK  = 0x0F;

  private final int id;
  private final int bitLength;

  public Duid(int id) {
    assert id <= 0x0F && id >= 0x00;
    this.id = id;

    switch (id) {
      case ID_HEADER:
        bitLength = 658;
        break;

      case ID_TERMINATOR_WO_LINK:
        bitLength = 28;
        break;

      case ID_LLDU1:
        bitLength = 1568;
        break;

      case ID_TRUNK_SIGNALING:
        bitLength = 588;
        break;

      case ID_LLDU2:
        bitLength = 1568;
        break;

      case ID_PACKET:
        bitLength = 2;
        break;

      case ID_TERMINATOR_W_LINK:
        bitLength = 308;
        break;

      default:
        bitLength = 2;
    }
  }

  public int getId() {
    return id;
  }

  public int getBitLength() {
    return bitLength;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)                               return true;
    if (o == null || getClass() != o.getClass()) return false;

    Duid duid = (Duid) o;
    return id == duid.id && bitLength == duid.bitLength;
  }

  @Override
  public String toString() {
    return "[" + id + "i:" + bitLength + "b]";
  }

}
