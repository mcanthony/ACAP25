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

package org.anhonesteffort.p25.primitive;

public class Oscillator {

  private final boolean       sloppy;
  private final ComplexNumber incrementFactor;
  private       ComplexNumber currentAngle;

  public Oscillator(long sampleRate, double frequency, ComplexNumber initialAngle, boolean sloppy) {
    this.sloppy     = sloppy;
    incrementFactor = new ComplexNumber((float) (2.0d * Math.PI * frequency / (double) sampleRate));
    currentAngle    = initialAngle;
  }

  public Oscillator(long sampleRate, double frequency, ComplexNumber initialAngle) {
    this(sampleRate, frequency, initialAngle, false);
  }

  public Oscillator(long sampleRate, double frequency, boolean sloppy) {
    this(sampleRate, frequency, new ComplexNumber(1f, 0f), sloppy);
  }

  public Oscillator(long sampleRate, double frequency) {
    this(sampleRate, frequency, false);
  }

  public ComplexNumber next() {
    if (!sloppy)
      currentAngle = currentAngle.multiply(incrementFactor).normalize();
    else
      currentAngle = currentAngle.multiply(incrementFactor).sloppyNormalize();

    return currentAngle.copy();
  }

}

