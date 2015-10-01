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
 *
 * Derived from:
 *   Wireshark - packet-p25cai.c (Copyright 2008, Michael Ossmann)
 *   OP25      - p25p1_fdma.cc   (Copyright 2010, 2011, 2012, 2013, 2014 Max H. Parke KA1RBI )
 */

package org.anhonesteffort.p25.ecc;

public class DeinterleaveTrellisDecoder {

  private static int[] deinterleave_tb = new int[] {
      0,  1,  2,  3,  52, 53, 54, 55, 100,101,102,103, 148,149,150,151,
      4,  5,  6,  7,  56, 57, 58, 59, 104,105,106,107, 152,153,154,155,
      8,  9, 10, 11,  60, 61, 62, 63, 108,109,110,111, 156,157,158,159,
      12, 13, 14, 15,  64, 65, 66, 67, 112,113,114,115, 160,161,162,163,
      16, 17, 18, 19,  68, 69, 70, 71, 116,117,118,119, 164,165,166,167,
      20, 21, 22, 23,  72, 73, 74, 75, 120,121,122,123, 168,169,170,171,
      24, 25, 26, 27,  76, 77, 78, 79, 124,125,126,127, 172,173,174,175,
      28, 29, 30, 31,  80, 81, 82, 83, 128,129,130,131, 176,177,178,179,
      32, 33, 34, 35,  84, 85, 86, 87, 132,133,134,135, 180,181,182,183,
      36, 37, 38, 39,  88, 89, 90, 91, 136,137,138,139, 184,185,186,187,
      40, 41, 42, 43,  92, 93, 94, 95, 140,141,142,143, 188,189,190,191,
      44, 45, 46, 47,  96, 97, 98, 99, 144,145,146,147, 192,193,194,195,
      48, 49, 50, 51
  };

  private static byte[][] next_words = new byte[][] {
    {0x2, 0xC, 0x1, 0xF},
    {0xE, 0x0, 0xD, 0x3},
    {0x9, 0x7, 0xA, 0x4},
    {0x5, 0xB, 0x6, 0x8}
  };

  private int find_min(int[] list, int len) {
    int min    = list[0];
    int index  = 0;
    int unique = 1;
    int i;

    for (i = 1; i < len; i++) {
      if (list[i] < min) {
        min    = list[i];
        index  = i;
        unique = 1;
      } else if (list[i] == min) {
        unique = 0;
      }
    }

    return (unique == 1) ? index : -1;
  }

  private int crc16(byte[] buf, int len) {
    int poly = (1<<12) + (1<<5) + 1;
    int crc  = 0;

    for(int i = 0; i < len; i++) {
      byte bits = buf[i];

      for (int j = 0; j < 8; j++) {
        int bit = (bits >> (7-j)) & 1;
            crc = ((crc << 1) | bit) & 0x1ffff;

        if ((crc & 0x10000) != 0x00)
          crc = (crc & 0xffff) ^ poly;
      }
    }

    crc = crc ^ 0xffff;
    return crc & 0xffff;
  }

  private int crc32(byte buf[], int len) {
    int  g   = 0x04c11db7;
    long crc = 0;

    for (int i = 0; i < len; i++) {
      crc <<= 1;
      int b = ( buf [i / 8] >> (7 - (i % 8)) ) & 1;
      if ((((crc >> 32) ^ b) & 1) != 0x00)
        crc ^= g;
    }
    crc = (crc & 0xffffffff) ^ 0xffffffff;
    return (int) crc;
  }

  public int decode(int[] bits196, byte[] bytes12) {
    int[] hd = new int[4];
    int b, d, j;
    int codeword, crc, crc1, crc2;
    int state = 0;

    for (b = 0; b < (98 * 2); b += 4) {
      codeword = (bits196[deinterleave_tb[b]]     << 3) +
                 (bits196[deinterleave_tb[b + 1]] << 2) +
                 (bits196[deinterleave_tb[b + 2]] << 1) +
                  bits196[deinterleave_tb[b + 3]];

      for (j = 0; j < 4; j++) {
        hd[j] = Integer.bitCount(codeword ^ next_words[state][j]);
      }

      state = find_min(hd, 4);
      if(state == -1) {
        return -1; // trellis error
      }

      d = b >> 2;
      if (d < 48) {
        bytes12[d >> 2] |= state << (6 - ((d % 4) * 2));
      }
    }

    crc = crc16(bytes12, 12);
    if (crc == 0) {
      return 0;
    }

    crc1 = crc32(bytes12, 8 * 8);
    crc2 = (bytes12[8] << 24) + (bytes12[9] << 16) + (bytes12[10] << 8) + bytes12[11];

    if (crc1 == crc2) return 0;
    else              return -2; // crc error
  }

}
