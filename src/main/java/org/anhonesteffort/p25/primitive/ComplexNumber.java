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

import org.anhonesteffort.p25.util.Copyable;

public class ComplexNumber implements Copyable<ComplexNumber> {

  private final float inPhase;
  private final float quadrature;

  public ComplexNumber(float inPhase, float quadrature) {
    this.inPhase    = inPhase;
    this.quadrature = quadrature;
  }

  public ComplexNumber(float angleRad) {
    this((float) Math.cos(angleRad), (float) Math.sin(angleRad));
  }

  public float getInPhase() {
    return inPhase;
  }

  public float getQuadrature() {
    return quadrature;
  }

  public ComplexNumber add(ComplexNumber other) {
    return new ComplexNumber(
        inPhase    + other.getInPhase(),
        quadrature + other.getQuadrature()
    );
  }

  public ComplexNumber subtract(ComplexNumber other) {
    return new ComplexNumber(
        inPhase    - other.getInPhase(),
        quadrature - other.getQuadrature()
    );
  }

  public ComplexNumber multiply(float factor) {
    return new ComplexNumber(
        inPhase * factor, quadrature * factor
    );
  }

  public ComplexNumber multiply(ComplexNumber factor) {
    return new ComplexNumber(
        (inPhase * factor.getInPhase())    - (quadrature * factor.getQuadrature()),
        (inPhase * factor.getQuadrature()) + (quadrature * factor.getInPhase())
    );
  }

  public float angleRadians() {
    return (float) Math.atan2(quadrature, inPhase);
  }

  public float angleDegrees() {
    return (float) Math.toDegrees(angleRadians());
  }

  private float magnitudeSquared() {
    return (inPhase * inPhase) + (quadrature * quadrature);
  }

  public float magnitude() {
    return (float) Math.sqrt(magnitudeSquared());
  }

  public float envelope() {
    float inPhaseAbs    = Math.abs(inPhase);
    float quadratureAbs = Math.abs(quadrature);

    return (inPhaseAbs > quadratureAbs) ? inPhaseAbs    + (0.4f * quadratureAbs) :
                                          quadratureAbs + (0.4f * inPhaseAbs);
  }

  public ComplexNumber normalize() {
    float magnitude = magnitude();
    return (magnitude != 0f) ? copy().multiply(1.0f / magnitude) : new ComplexNumber(0, 0);
  }

  public ComplexNumber sloppyNormalize() {
    return copy().multiply(1.95f - magnitudeSquared());
  }

  public ComplexNumber conjugate() {
    return new ComplexNumber(inPhase, -quadrature);
  }

  @Override
  public ComplexNumber copy() {
    return new ComplexNumber(inPhase, quadrature);
  }

  @Override
  public String toString() {
    return "[" + inPhase + "," + quadrature + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)                               return true;
    if (o == null || getClass() != o.getClass()) return false;

    ComplexNumber that = (ComplexNumber) o;

    if (Float.compare(that.inPhase, inPhase) != 0) return false;
    return Float.compare(that.quadrature, quadrature) == 0;
  }

}
