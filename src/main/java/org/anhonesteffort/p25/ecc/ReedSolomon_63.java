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
 *   OP25 - rs.cc (Copyright 2013 KA1RBI)
 */

package org.anhonesteffort.p25.ecc;

public class ReedSolomon_63 {

  private static final int[] rsGFexp = {
      1, 2, 4, 8, 16, 32, 3, 6, 12, 24, 48, 35, 5, 10, 20, 40,
      19, 38, 15, 30, 60, 59, 53, 41, 17, 34, 7, 14, 28, 56, 51, 37,
      9, 18, 36, 11, 22, 44, 27, 54, 47, 29, 58, 55, 45, 25, 50, 39,
      13, 26, 52, 43, 21, 42, 23, 46, 31, 62, 63, 61, 57, 49, 33, 0
  };

  private static final int[] rsGFlog = {
      63, 0, 1, 6, 2, 12, 7, 26, 3, 32, 13, 35, 8, 48, 27, 18,
      4, 24, 33, 16, 14, 52, 36, 54, 9, 45, 49, 38, 28, 41, 19, 56,
      5, 62, 25, 11, 34, 31, 17, 47, 15, 23, 53, 51, 37, 44, 55, 40,
      10, 61, 46, 30, 50, 22, 39, 43, 29, 60, 42, 21, 20, 59, 57, 58
  };

  public int decode(int nroots, int FirstInfo, int HB[]) {
    int[] lambda = new int[18];
    int[] S      = new int[17];
    int[] b      = new int[18];
    int[] t      = new int[18];
    int[] omega  = new int[18];
    int[] root   = new int[17];
    int[] reg    = new int[18];
    int[] locn   = new int[17];

    int i, j, count, r, el, SynError, DiscrR, q, DegOmega, tmp, num1, num2, den, DegLambda;

    for (i = 0; i <= nroots - 1; i++) {
      S[i] = HB[0];
    }

    for (j = 1; j <= 62; j++) {
      for (i = 0; i <= nroots - 1; i++) {
        if (S[i] == 0) {
          S[i] = HB[j];
        } else {
          S[i] = HB[j] ^ rsGFexp[(rsGFlog[S[i]] + i + 1) % 63];
        }
      }
    }

    SynError = 0;
    for (i = 0; i <= nroots - 1; i++) {
      SynError = SynError | S[i];
      S[i]     = rsGFlog[S[i]];
    }
    if (SynError == 0) {
      return 0;
    }


    for (i = 1; i <= nroots; i++) {
      lambda[i] = 0;
    }
    lambda[0] = 1;

    for (i = 0; i <= nroots; i++) {
      b[i] = rsGFlog[lambda[i]];
    }


    r  = 0;
    el = 0;
    while (r < nroots) {
      r      = r + 1;
      DiscrR = 0;

      for (i = 0; i <= r - 1; i++) {
        if ((lambda[i] != 0) && (S[r - i - 1] != 63)) {
          DiscrR = DiscrR ^ rsGFexp[(rsGFlog[lambda[i]] + S[r - i - 1]) % 63];
        }
      }

      DiscrR = rsGFlog[DiscrR] ;//index form

      if (DiscrR == 63) {
        for (i = nroots; i >= 1; i += -1) {
          b[i] = b[i - 1];
        }
        b[0] = 63;
      } else {
        t[0] = lambda[0];
        for (i = 0; i <= nroots - 1; i++) {
          if (b[i] != 63) {
            t[i + 1] = lambda[i + 1] ^ rsGFexp[(DiscrR + b[i]) % 63];
          } else {
            t[i + 1] = lambda[i + 1];
          }
        }
        if (2 * el <= r - 1) {
          el = r - el;
          for (i = 0; i <= nroots; i++) {
            if (lambda[i] != 0) {
              b[i] = (rsGFlog[lambda[i]] - DiscrR + 63) % 63;
            } else {
              b[i] = 63;
            }
          }
        } else {
          for (i = nroots; i >= 1; i += -1) {
            b[i] = b[i - 1];
          }
          b[0] = 63;
        }
        for (i = 0; i <= nroots; i++) {
          lambda[i] = t[i];
        }
      }
    }

    DegLambda = 0;
    for (i = 0; i <= nroots; i++) {
      lambda[i] = rsGFlog[lambda[i]];
      if (lambda[i] != 63) {
        DegLambda = i;
      }
    }

    for (i = 1; i <= nroots; i++) {
      reg[i] = lambda[i];
    }
    count = 0;

    for (i = 1; i <= 63; i++) {
      q = 1 ;
      for (j = DegLambda; j >= 1; j += -1) {
        if (reg[j] != 63) {
          reg[j] = (reg[j] + j) % 63;
          q      = q ^ rsGFexp[reg[j]];
        }
      }

      if (q == 0) {
        root[count] = i;
        locn[count] = i - 1;
        count       = count + 1;

        if (count == DegLambda) {
          break;
        }
      }
    }

    if (DegLambda != count) {
      return -1;
    }


    DegOmega = 0;
    for (i = 0; i <= nroots - 1; i++) {
      tmp = 0;

      if (DegLambda < i) {
        j = DegLambda;
      } else {
        j = i;
      }

      for (/* j = j */; j >= 0; j += -1) {
        if ((S[i - j] != 63) && (lambda[j] != 63)) {
          tmp = tmp ^ rsGFexp[(S[i - j] + lambda[j]) % 63];
        }
      }
      if (tmp != 0) {
        DegOmega = i;
      }
      omega[i] = rsGFlog[tmp];
    }
    omega[nroots] = 63;


    for (j = count - 1; j >= 0; j += -1) {
      num1 = 0;
      for (i = DegOmega; i >= 0; i += -1) {
        if (omega[i] != 63) {
          num1 = num1 ^ rsGFexp[(omega[i] + i * root[j]) % 63];
        }
      }
      num2 = rsGFexp[0];
      den  = 0;

      if (DegLambda < nroots) {
        i = DegLambda;
      } else {
        i = nroots;
      }
      for (i = i & ~1; i >= 0; i += -2) {
        if (lambda[i + 1] != 63) {
          den = den ^ rsGFexp[(lambda[i + 1] + i * root[j]) % 63];
        }
      }

      if (den == 0) {
        return -1;
      }

      // apply error to data
      if (num1 != 0) {
        if (locn[j] < FirstInfo) {
          return -1;
        }

        HB[locn[j]] = HB[locn[j]] ^ (rsGFexp[(rsGFlog[num1] + rsGFlog[num2] + 63 - rsGFlog[den]) % 63]);
      }
    }

    return count;
  }

}
