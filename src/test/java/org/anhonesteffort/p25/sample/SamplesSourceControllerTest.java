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

import org.anhonesteffort.p25.primitive.ChannelSpec;
import org.junit.Test;

import java.util.Optional;

public class SamplesSourceControllerTest {

  private static class DumbTunableSamplesSource extends TunableSamplesSource {
    public static final Long   MAX_SAMPLE_RATE =   400_000l;
    public static final Double MIN_FREQ        =   100_000d;
    public static final Double MAX_FREQ        = 1_000_000d;

    protected DumbTunableSamplesSource() throws SamplesSourceException {
      super(MAX_SAMPLE_RATE, MIN_FREQ, MAX_FREQ);
    }

    @Override
    protected Long setSampleRate(Long minSampleRate) throws SamplesSourceException {
      if (minSampleRate > MAX_SAMPLE_RATE)
        throw new SamplesSourceException("don't");

      this.sampleRate = minSampleRate;
      return minSampleRate;
    }

    @Override
    protected Double setFrequency(Double frequency) throws SamplesSourceException {
      if (frequency < MIN_FREQ || frequency > MAX_FREQ)
        throw new SamplesSourceException("don't");

      this.frequency = frequency;
      return frequency;
    }

    @Override
    protected void startProducing() { }
    @Override
    protected void stopProducing() { }
  }

  public static class DumbTunableSamplesSourceProvider implements TunableSamplesSourceProvider {
    @Override
    public Optional<TunableSamplesSource> get() {
      try {

        return Optional.of(new DumbTunableSamplesSource());

      } catch (SamplesSourceException e) {
        return Optional.empty();
      }
    }
  }

  private static class DumbSamplesSink implements SamplesSink {
    private final ChannelSpec channelSpec;

    public DumbSamplesSink(ChannelSpec channelSpec) {
      this.channelSpec = channelSpec;
    }

    @Override
    public Optional<ChannelSpec> getTargetChannel() {
      return Optional.of(channelSpec);
    }

    @Override
    public void onSourceStateChange(Long sampleRate, Double frequency) { }

    @Override
    public void consume(Samples samples) { }
  }

  private static SamplesSink sinkFor(Double minFreq, Double maxFreq) {
    return new DumbSamplesSink(ChannelSpec.fromMinMax(minFreq, maxFreq));
  }

  @Test
  public void testWithSingleSink() throws Exception {
    final Double                  DC_OFFSET  = 0d;
    final SamplesSourceController CONTROLLER = new SamplesSourceController(DC_OFFSET);

    final SamplesSink SINK0 = sinkFor(500_000d, 600_000d);
    assert CONTROLLER.configureSourceForSink(SINK0);
    CONTROLLER.removeSink(SINK0);

    final SamplesSink SINK1 = sinkFor(100_000d, 200_000d);
    assert CONTROLLER.configureSourceForSink(SINK1);
    CONTROLLER.removeSink(SINK1);

    final SamplesSink SINK2 = sinkFor(900_000d, 1_000_000d);
    assert CONTROLLER.configureSourceForSink(SINK2);
    CONTROLLER.removeSink(SINK2);

    assert !CONTROLLER.configureSourceForSink(sinkFor(1_000_000d, 1_001_000d));
    assert !CONTROLLER.configureSourceForSink(sinkFor(500_000d, 1_000_000d));
  }

  @Test
  public void testWithMultipleConsumers() throws Exception {
    final Double                  DC_OFFSET  = 0d;
    final SamplesSourceController CONTROLLER = new SamplesSourceController(DC_OFFSET);
    final SamplesSink             SINK0      = sinkFor(500_000d, 600_000d);
    final SamplesSink             SINK1      = sinkFor(600_000d, 700_000d);
    final SamplesSink             SINK2      = sinkFor(700_000d, 800_000d);
    final SamplesSink             SINK3      = sinkFor(800_000d, 900_000d);

    assert  CONTROLLER.configureSourceForSink(SINK0);
    assert  CONTROLLER.configureSourceForSink(SINK1);
    assert  CONTROLLER.configureSourceForSink(SINK2);
    assert  CONTROLLER.configureSourceForSink(SINK3);

    assert !CONTROLLER.configureSourceForSink(sinkFor(800_000d, 900_001d));
    assert !CONTROLLER.configureSourceForSink(sinkFor(499_999d, 900_000d));

    CONTROLLER.removeSink(SINK0);
    assert CONTROLLER.configureSourceForSink(sinkFor(900_000d, 950_000d));
    assert CONTROLLER.configureSourceForSink(sinkFor(950_000d, 1_000_000d));
  }
}
