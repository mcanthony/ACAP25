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

import org.anhonesteffort.p25.Source;
import org.anhonesteffort.p25.primitive.ChannelSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public class SamplesSourceController extends Source<Samples, SamplesSink> {

  private static final Logger log = LoggerFactory.getLogger(SamplesSourceController.class);

  private final List<ChannelSpec>    tunedChannels = new LinkedList<>();
  private final Double               dcOffsetHz;
  private       TunableSamplesSource source;

  public SamplesSourceController(Double dcOffsetHz) throws SamplesSourceException {
    this.dcOffsetHz = dcOffsetHz;

    ServiceLoader.load(TunableSamplesSourceProvider.class).forEach(provider -> {
      if (source == null) {
        Optional<TunableSamplesSource> source = provider.get();
        if (source.isPresent())
          this.source = source.get();
      }
    });

    if (source == null)
      throw new SamplesSourceException("no sources available");
  }

  private Optional<Double> getMinChannelFrequency() {
    if (tunedChannels.isEmpty())
      return Optional.empty();

    return Optional.of(tunedChannels.stream()
                                    .mapToDouble(ChannelSpec::getMinFreq)
                                    .min()
                                    .getAsDouble());
  }

  private Optional<Double> getMaxChannelFrequency() {
    if (tunedChannels.isEmpty())
      return Optional.empty();

    return Optional.of(tunedChannels.stream()
                                    .mapToDouble(ChannelSpec::getMaxFreq)
                                    .max()
                                    .getAsDouble());
  }

  private Optional<Long> getMaxChannelSampleRate() {
    if (tunedChannels.isEmpty())
      return Optional.empty();

    return Optional.of(tunedChannels.stream()
                                    .mapToLong(ChannelSpec::getSampleRate)
                                    .max()
                                    .getAsLong());
  }

  private ChannelSpec getIdealChannelSpec(ChannelSpec newChannel) {
    double minRequiredFreq   = Math.min(getMinChannelFrequency().get(), newChannel.getMinFreq());
    double maxRequiredFreq   = Math.max(getMaxChannelFrequency().get(), newChannel.getMaxFreq());
    double requiredBandwidth = maxRequiredFreq - minRequiredFreq;

    long maxChannelSampleRate = Math.max(getMaxChannelSampleRate().get(), newChannel.getSampleRate());
    long requiredSampleRate   = Math.max(maxChannelSampleRate, (long) requiredBandwidth);

    return ChannelSpec.fromMinMax(minRequiredFreq, maxRequiredFreq, requiredSampleRate);
  }

  private ChannelSpec accommodateDcOffset(ChannelSpec spec) {
    double      offsetCenterFreq = spec.getCenterFrequency() + dcOffsetHz;
    double      offsetBandwidth  = spec.getBandwidth() + (Math.abs(dcOffsetHz) * 2d);
    ChannelSpec offsetSpec       = new ChannelSpec(offsetCenterFreq, offsetBandwidth, spec.getSampleRate());

    if ((offsetSpec.getSampleRate() / 2d) < offsetSpec.getBandwidth()) {
      double aliasedBw        = offsetSpec.getBandwidth() - (offsetSpec.getSampleRate() / 2d);
      long   offsetSampleRate = (long) Math.ceil(offsetSpec.getSampleRate() + (aliasedBw * 2d));
             offsetSpec       = new ChannelSpec(offsetCenterFreq, offsetBandwidth, offsetSampleRate);
    }

    return offsetSpec;
  }

  private boolean isTunable(ChannelSpec spec) {
    if (tunedChannels.isEmpty())
      return source.isTunable(accommodateDcOffset(spec));

    return source.isTunable(getIdealChannelSpec(spec));
  }

  private void handleTuneToFitNewChannel(ChannelSpec newChannel) throws SamplesSourceException {
    if (tunedChannels.isEmpty())
      source.tune(accommodateDcOffset(newChannel));
    else
      source.tune(getIdealChannelSpec(newChannel));
  }

  public synchronized boolean configureSourceForSink(SamplesSink sink) {
    if (!sink.getTargetChannel().isPresent()) {
      source.addSink(sink);
      return true;
    }

    ChannelSpec channelSpec  = sink.getTargetChannel().get();
    ChannelSpec tunedChannel = source.getTunedChannel();

    if (source.isTunable(channelSpec) && tunedChannel.containsChannel(channelSpec)) {
      source.addSink(sink);
      tunedChannels.add(channelSpec);
      return true;
    } else if (isTunable(channelSpec)) {
      try {

        handleTuneToFitNewChannel(channelSpec);
        source.addSink(sink);
        tunedChannels.add(channelSpec);
        return true;

      } catch (SamplesSourceException e) {
        log.error("unable to configure source for consumer channel " + channelSpec, e);
      }
    }

    return false;
  }

  @Override
  public void addSink(SamplesSink sink) {
    super.addSink(sink);

    synchronized (this) {
      source.addSink(sink);
    }
  }

  @Override
  public void removeSink(SamplesSink sink) {
    super.removeSink(sink);

    synchronized (this) {
      source.removeSink(sink);
      if (sink.getTargetChannel().isPresent())
        tunedChannels.remove(sink.getTargetChannel().get());
    }
  }

}
