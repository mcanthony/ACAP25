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

public class ChannelSpec {

  private final Double centerFrequency;
  private final Double bandwidth;
  private final Long   sampleRate;

  public ChannelSpec(Double centerFrequency, Double bandwidth, Long sampleRate) {
    this.centerFrequency = centerFrequency;
    this.bandwidth       = bandwidth;
    this.sampleRate      = sampleRate;
  }

  public ChannelSpec(Double centerFrequency, Long bandwidth, Long sampleRate) {
    this(centerFrequency, (double) bandwidth, sampleRate);
  }

  public ChannelSpec(Double centerFrequency, Double bandwidth) {
    this(centerFrequency, bandwidth, (long) (bandwidth * 2));
  }

  public static ChannelSpec fromMinMax(Double minFrequency, Double maxFrequency, Long sampleRate) {
    Double centerFreq = minFrequency + ((maxFrequency - minFrequency) / 2);
    Double width      = maxFrequency - minFrequency;

    return new ChannelSpec(centerFreq, width, sampleRate);
  }

  public static ChannelSpec fromMinMax(Double minFrequency, Double maxFrequency) {
    Double centerFreq = minFrequency + ((maxFrequency - minFrequency) / 2);
    Double width      = maxFrequency - minFrequency;

    return new ChannelSpec(centerFreq, width);
  }

  public Double getCenterFrequency() {
    return centerFrequency;
  }

  public Double getBandwidth() {
    return bandwidth;
  }

  public Long getSampleRate() {
    return sampleRate;
  }

  public Double getMinFreq() {
    return centerFrequency - (bandwidth / 2);
  }

  public Double getMaxFreq() {
    return centerFrequency + (bandwidth / 2);
  }

  public boolean containsChannel(ChannelSpec spec) {
    return spec.getSampleRate() <= getSampleRate() &&
           spec.getMinFreq()    >= getMinFreq()    &&
           spec.getMaxFreq()    <= getMaxFreq();
  }

  @Override
  public String toString() {
    return "[" + getMinFreq() + "->" + getMaxFreq() + "Hz @" + sampleRate + "]";
  }

}
