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

public enum FftWidth {

  FFT_512     (512),
  FFT_1024   (1024),
  FFT_2048   (2048),
  FFT_4096   (4096),
  FFT_8192   (8192),
  FFT_16384 (16384),
  FFT_32768 (32768);

  private int width;

  private FftWidth(int width) {
    this.width = width;
  }

  public int getWidth() {
    return width;
  }

}
