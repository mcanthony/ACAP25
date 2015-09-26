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
 * adapted from https://github.com/szechyjs/dsd/blob/master/include/ReedSolomon.hpp
 *   which was adapted from http://www.eccpage.com/rs.c
 *     which was authored by Simon Rockliff back when variable names were limited to 3 characters
 *
 * I have to take a shower every time I look at this code, honestly.
 */

package org.anhonesteffort.p25.ecc;

import java.util.stream.IntStream;

public class BchDecoder {

  private final int NN;
  private final int KK;
  private final int TT;

  private final int[] alphaTo;
  private final int[] indexOf;

  private BchDecoder(int NN, int KK, int TT, int[] alphaTo, int[] indexOf) {
    this.NN      = NN;
    this.KK      = KK;
    this.TT      = TT;
    this.alphaTo = alphaTo;
    this.indexOf = indexOf;
  }

  public static BchDecoder newInstance(int MM, int TT, int generatorPolynomial[]) {
    int NN = (int) (Math.pow(2, MM) - 1);
    int KK = NN - 2 * TT;

    int[][] golay = generateGolayField(generatorPolynomial, MM, NN);
    return new BchDecoder(NN, KK, TT, golay[0], golay[1]);
  }

  private static int[][] generateGolayField(int[] polynomial, int MM, int NN) {
    int[] alphaTo = new int[NN + 1];
    int[] indexOf = new int[NN + 1];
    int   mask    = 1;

    alphaTo[MM] = 0;

    for (int i = 0; i < MM; i++) {
      alphaTo[i]          = mask;
      indexOf[alphaTo[i]] = i;

      if (polynomial[i] != 0) {
        alphaTo[MM] ^= mask;
      }

      mask <<= 1;
    }

    indexOf[alphaTo[MM]] = MM;
    mask >>= 1;

    for (int i = MM + 1; i < NN; i++) {
      if (alphaTo[i - 1] >= mask) {
        alphaTo[i] = alphaTo[MM] ^ ((alphaTo[i - 1] ^ mask) << 1);
      } else {
        alphaTo[i] = alphaTo[i - 1] << 1;
      }

      indexOf[alphaTo[i]] = i;
    }

    indexOf[0] = -1;

    return new int[][] {alphaTo, indexOf};
  }

  public boolean decode(final int[] input, int[] output) {
    int u, q, count;
    boolean syn_error           = false;
    boolean irrecoverable_error = false;

    int[]   d    = new int[NN - KK + 2];
    int[]   l    = new int[NN - KK + 2];
    int[]   u_lu = new int[NN - KK + 2];
    int[]   s    = new int[NN - KK + 1];
    int[]   root = new int[TT];
    int[]   loc  = new int[TT];
    int[]   z    = new int[TT + 1];
    int[]   err  = new int[NN];
    int[]   reg  = new int[TT + 1];
    int[][] elp  = new int[NN - KK + 2][NN - KK];

    int[] reversed = new int[output.length];
    for (int i = 0; i < NN; i++) {
      reversed[i] = indexOf[input[input.length - 1 - i]];
    }

    for (int i = 1; i <= NN - KK; i++) {
      s[i] = 0;

      for (int j = 0; j < NN; j++) {
        if (reversed[j] != -1) {
          s[i] ^= alphaTo[(reversed[j] + i * j) % NN];
        }
      }

      if (s[i] != 0) {
        syn_error = true;
      }

      s[i] = indexOf[s[i]];
    }

    if (syn_error) {
      d[0]      = 0;
      d[1]      = s[1];
      elp[0][0] = 0;
      elp[1][0] = 1;

      for (int i = 1; i < NN - KK; i++) {
        elp[0][i] = -1;
        elp[1][i] = 0;
      }

      l[0]    = 0;
      l[1]    = 0;
      u_lu[0] = -1;
      u_lu[1] = 0;
      u       = 0;

      do {
        u++;

        if (d[u] == -1) {
          l[u + 1] = l[u];

          for (int i = 0; i <= l[u]; i++) {
            elp[u + 1][i] = elp[u][i];
            elp[u][i]     = indexOf[elp[u][i]];
          }
        } else {
          q = u - 1;

          while ((d[q] == -1) && (q > 0)) {
            q--;
          }

          if (q > 0) {
            int j = q;

            do {
              j--;

              if ((d[j] != -1) && (u_lu[q] < u_lu[j])) {
                q = j;
              }
            }
            while (j > 0);
          }

          if (l[u] > l[q] + u - q) {
            l[u + 1] = l[u];
          } else {
            l[u + 1] = l[q] + u - q;
          }

          for (int i = 0; i < NN - KK; i++) {
            elp[u + 1][i] = 0;
          }

          for (int i = 0; i <= l[q]; i++) {
            if (elp[q][i] != -1) {
              elp[u + 1][i + u - q] = alphaTo[(d[u] + NN - d[q] + elp[q][i]) % NN];
            }
          }
          for (int i = 0; i <= l[u]; i++) {
            elp[u + 1][i] ^= elp[u][i];
            elp[u][i]      = indexOf[elp[u][i]];
          }
        }

        u_lu[u + 1] = u - l[u + 1];

        if (u < NN - KK) {
          if (s[u + 1] != -1) {
            d[u + 1] = alphaTo[s[u + 1]];
          } else {
            d[u + 1] = 0;
          }
          for (int i = 1; i <= l[u + 1]; i++) {
            if ((s[u + 1 - i] != -1) && (elp[u + 1][i] != 0)) {
              d[u + 1] ^= alphaTo[(s[u + 1 - i] + indexOf[elp[u + 1][i]]) % NN];
            }
          }

          d[u + 1] = indexOf[d[u + 1]];
        }
      }
      while ((u < NN - KK) && (l[u + 1] <= TT));

      u++;

      if (l[u] <= TT) {
        for (int i = 0; i <= l[u]; i++) {
          elp[u][i] = indexOf[elp[u][i]];
        }

        for (int i = 1; i <= l[u]; i++) {
          reg[i] = elp[u][i];
        }

        count = 0;

        for (int i = 1; i <= NN; i++) {
          q = 1;

          for (int j = 1; j <= l[u]; j++) {
            if (reg[j] != -1) {
              reg[j] = (reg[j] + j) % NN;
              q     ^= alphaTo[reg[j]];
            }
          }

          if (q == 0) {
            root[count] = i;
            loc[count]  = NN - i;
            count++;
          }
        }

        if (count == l[u]) {
          for (int i = 1; i <= l[u]; i++) {
            if ((s[i] != -1) && (elp[u][i] != -1)) {
              z[i] = alphaTo[s[i]] ^ alphaTo[elp[u][i]];
            } else if ((s[i] != -1) && (elp[u][i] == -1)) {
              z[i] = alphaTo[s[i]];
            } else if ((s[i] == -1) && (elp[u][i] != -1)) {
              z[i] = alphaTo[elp[u][i]];
            } else {
              z[i] = 0;
            }

            for (int j = 1; j < i; j++) {
              if ((s[j] != -1) && (elp[u][i - j] != -1)) {
                z[i] ^= alphaTo[(elp[u][i - j] + s[j]) % NN];
              }
            }

            z[i] = indexOf[z[i]];
          }

          for (int i = 0; i < NN; i++) {
            err[i] = 0;

            if (reversed[i] != -1) {
              reversed[i] = alphaTo[reversed[i]];
            } else {
              reversed[i] = 0;
            }
          }

          for (int i = 0; i < l[u]; i++) {
            err[loc[i]] = 1;

            for (int j = 1; j <= l[u]; j++) {
              if (z[j] != -1) {
                err[loc[i]] ^= alphaTo[(z[j] + j * root[i]) % NN];
              }
            }

            if (err[loc[i]] != 0) {
              err[loc[i]] = indexOf[err[loc[i]]];
              q           = 0;

              for (int j = 0; j < l[u]; j++) {
                if (j != i) {
                  q += indexOf[1 ^ alphaTo[(loc[j] + root[i]) % NN]];
                }
              }

              q                 = q % NN;
              err[loc[i]]       = alphaTo[(err[loc[i]] - q + NN) % NN];
              reversed[loc[i]] ^= err[loc[i]];
            }
          }
        } else {
          irrecoverable_error = true;
        }

      } else {
        irrecoverable_error = true;
      }
    } else {
      for (int i = 0; i < NN; i++) {
        if (reversed[i] != -1) {
          reversed[i] = alphaTo[reversed[i]];
        } else {
          reversed[i] = 0;
        }
      }
    }

    IntStream.range(0, output.length)
             .forEach(i -> output[i] = reversed[reversed.length - 1 - i]);

    return !irrecoverable_error;
  }
}
