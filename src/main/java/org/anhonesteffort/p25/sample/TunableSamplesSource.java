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

package org.anhonesteffort.p25.sample;

import org.anhonesteffort.p25.SelfStartingSource;
import org.anhonesteffort.p25.primitive.ChannelSpec;

public abstract class TunableSamplesSource extends SelfStartingSource<Samples, SamplesSink> {

  private final Long   maxSampleRate;
  private final Double minTunableFreq;
  private final Double maxTunableFreq;

  protected Long   sampleRate = -1l;
  protected Double frequency  = -1d;

  protected TunableSamplesSource(Long maxSampleRate, Double minTunableFreq, Double maxTunableFreq)
      throws SamplesSourceException
  {
    this.maxSampleRate  = maxSampleRate;
    this.minTunableFreq = minTunableFreq;
    this.maxTunableFreq = maxTunableFreq;
  }

  public ChannelSpec getTunedChannel() {
    return new ChannelSpec(frequency, sampleRate, sampleRate);
  }

  public boolean isTunable(ChannelSpec spec) {
    return spec.getSampleRate() <= maxSampleRate  &&
           spec.getMinFreq()    >= minTunableFreq &&
           spec.getMaxFreq()    <= maxTunableFreq;
  }

  protected abstract Long setSampleRate(Long minSampleRate) throws SamplesSourceException;

  protected abstract Double setFrequency(Double frequency) throws SamplesSourceException;

  public void tune(ChannelSpec channelSpec) throws SamplesSourceException {
    try {

      Double tunedFrequency = setFrequency(channelSpec.getCenterFrequency());
      Double tunedDiffHz    = Math.ceil(Math.abs(channelSpec.getCenterFrequency() - tunedFrequency));

      Long minSampleRate   = channelSpec.getSampleRate() + (long) (tunedDiffHz * 2);
      Long tunedSampleRate = setSampleRate(minSampleRate);

      ChannelSpec tunedChannel = new ChannelSpec(tunedFrequency, tunedSampleRate, tunedSampleRate);
      if (!tunedChannel.containsChannel(channelSpec))
        throw new SamplesSourceException("source failed to tune channel " + channelSpec.getCenterFrequency());

    } finally {
      sinks.forEach(sink -> sink.onSourceStateChange(sampleRate, frequency));
    }
  }

  @Override
  public void addSink(SamplesSink sink) {
    sink.onSourceStateChange(sampleRate, frequency);
    super.addSink(sink);
  }

}
