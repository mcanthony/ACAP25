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

package org.anhonesteffort.p25.filter;

import org.anhonesteffort.p25.util.Util;

public class FilterUtil {

  public static int[] getInterpolationAndDecimation(long sourceRate, long channelRate, long maxRateDiff) {
    int   commonFactor  = Util.greatestCommonFactor(sourceRate, channelRate);
    int   interpolation = (int) (channelRate / commonFactor);
    int   decimation    = (int) (sourceRate / commonFactor);
    int[] bestFit       = new int[] {interpolation, decimation};
    int   rateDiff      = 0;

    while (rateDiff < maxRateDiff) {
      rateDiff++;

      commonFactor  = Util.greatestCommonFactor(sourceRate, channelRate + rateDiff);
      interpolation = (int) ((channelRate + rateDiff) / commonFactor);
      decimation    = (int) (sourceRate / commonFactor);

      if (interpolation < bestFit[0])
        bestFit = new int[] {interpolation, decimation};
    }

    while (rateDiff > 0) {
      commonFactor  = Util.greatestCommonFactor(sourceRate - rateDiff, channelRate);
      interpolation = (int) (channelRate / commonFactor);
      decimation    = (int) ((sourceRate - rateDiff) / commonFactor);

      if (interpolation < bestFit[0])
        bestFit = new int[] {interpolation, decimation};

      rateDiff--;
    }

    return bestFit;
  }

  public static float[] getLowPassIdealImpulseResponse(long sampleRate, long transitionFreq, int length) {
    if ((length & 1) == 0)
      throw new IllegalArgumentException("filter length must be odd");

    int     m          = length;
    int     m2         = (m - 1) / 2;
    float   fs         = (float) sampleRate;
    float   ft         = ((float) transitionFreq) / fs;
    float[] response   = new float[length];

    response[m2] = 2.0f * ft;

    for (int i = 0; i < m2; i++) {
      float n   = (float) i;
      float val = (float) (
          (Math.sin(2f * Math.PI * ft * (n - m2)) / (Math.PI * (n - m2)))
      );

      response[i] = response[m - i - 1] = val;
    }

    return response;
  }

  public static int getKaiserBesselFilterLength(long sampleRate, long transitionWidth, int attenuation) {
    float fs = (float) sampleRate;
    float aa = (float) attenuation;
    float tw = (float) Math.PI * 2f * ((float)transitionWidth / fs);

    int length;
    if (attenuation <= 21)
      length = (int) Math.ceil(5.79f / tw);
    else
      length = (int) Math.ceil((aa - 7.95f) / (2.285f * tw));

    return ((length & 1) == 1) ? length : length + 1;
  }

  public static float getKaiserBesselWindowShape(int attenuation) {
    float aa = (float) attenuation;
    if (attenuation <= 21)
      return 0.0f;
    else if (attenuation > 21 && attenuation <= 50)
      return 0.5842f * ((float) Math.pow((aa - 21f), 0.4f)) + 0.07886f * (aa - 21f);
    else
      return 0.1102f * (aa - 8.7f);
  }

  public static float zeroOrderBessel(float num) {
    float d  = 0;
    float ds = 1;
    float s  = 1;

    do {
      d  += 2;
      ds *= num*num / (d*d);
      s  += ds;
    } while (ds > (s * 1e-6f));

    return s;
  }

  public static float zeroOrderBessel(double num) {
    return zeroOrderBessel((float) num);
  }

  public static float[] getKaiserBesselWindow(float[] idealImpulseResponse, float shape) {
    int     m        = idealImpulseResponse.length;
    int     m2       = (m - 1) / 2;
    float   zobShape = zeroOrderBessel(shape);
    float[] result   = new float[m];

    for (int n = 0; n < m; n++) {
      float j         = (float) n;
      float val       = 1f - ((j - m2) / m2) * ((j - m2) / m2);
            result[n] = zeroOrderBessel(shape * Math.sqrt(val)) / zobShape;
    } for (int n = 0; n < m; n++) {
      result[n] *= idealImpulseResponse[n];
    }

    return result;
  }

}
