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

package org.anhonesteffort.p25.protocol;

import org.anhonesteffort.p25.Sink;
import org.anhonesteffort.p25.Source;
import org.anhonesteffort.p25.filter.demod.ComplexNumberCqpskDemodulator;
import org.anhonesteffort.p25.filter.ComplexNumberFrequencyTranslatingFilter;
import org.anhonesteffort.p25.filter.ComplexNumberMovingGainControl;
import org.anhonesteffort.p25.filter.Filter;
import org.anhonesteffort.p25.filter.FilterFactory;
import org.anhonesteffort.p25.filter.decode.QpskPolarSlicer;
import org.anhonesteffort.p25.filter.rate.RateChangeFilter;
import org.anhonesteffort.p25.primitive.ChannelSpec;
import org.anhonesteffort.p25.primitive.ComplexNumber;
import org.anhonesteffort.p25.protocol.frame.DataUnit;
import org.anhonesteffort.p25.protocol.frame.DataUnitFramer;
import org.anhonesteffort.p25.sample.DynamicSink;
import org.anhonesteffort.p25.sample.Samples;
import org.anhonesteffort.p25.sample.SamplesSink;
import org.anhonesteffort.p25.util.StreamInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class P25Channel extends Source<DataUnit, Sink<DataUnit>>
    implements SamplesSink, Supplier<List<ComplexNumber>>, Callable<Void>
{

  private static final Logger log            = LoggerFactory.getLogger(P25Channel.class);
  private static final Long   TARGET_RATE    = P25.SAMPLE_RATE;
  private static final Long   MAX_RATE_DIFF  = 150l;
  private static final Long   SYMBOL_RATE    = P25.SYMBOL_RATE;
  private static final Long   PASSBAND_STOP  = P25.PASSBAND_STOP;
  private static final Long   STOPBAND_START = P25.STOPBAND_START;
  private static final int    ATTENUATION    = 40;

  private final Map<FilterType, List<DynamicSink<ComplexNumber>>> spies = new HashMap<>();
  private final LinkedBlockingQueue<float[]> iqSampleQueue = new LinkedBlockingQueue<>(10);
  private final Object processChainLock = new Object();
  private final P25ChannelSpec spec;

  private ComplexNumberFrequencyTranslatingFilter freqTranslation;
  private Filter<ComplexNumber>                   baseband;
  private Filter<ComplexNumber>                   gainControl;
  private ComplexNumberCqpskDemodulator           cqpskDemodulation;
  private DataUnitFramer                          framer;
  private Long                                    channelRate = -1l;

  public enum FilterType {
    TRANSLATION, BASEBAND, GAIN, DEMODULATION
  }

  public P25Channel(P25ChannelSpec spec) {
    this.spec = spec;
    spies.put(FilterType.TRANSLATION,  new LinkedList<>());
    spies.put(FilterType.BASEBAND,     new LinkedList<>());
    spies.put(FilterType.GAIN,         new LinkedList<>());
    spies.put(FilterType.DEMODULATION, new LinkedList<>());
  }

  public P25ChannelSpec getSpec() {
    return spec;
  }

  @Override
  public Optional<ChannelSpec> getTargetChannel() {
    return Optional.of(spec);
  }

  @Override
  public void onSourceStateChange(Long sampleRate, Double frequency) {
    synchronized (processChainLock) {
      RateChangeFilter<ComplexNumber> resampling = FilterFactory.getCicResampler(
          sampleRate, TARGET_RATE, MAX_RATE_DIFF, PASSBAND_STOP, STOPBAND_START
      );
      channelRate = (long) (sampleRate * resampling.getRateChange());
      iqSampleQueue.clear();
      log.info("source rate: " + sampleRate + ", channel rate: " + channelRate);
      log.info("interpolation: " + resampling.getInterpolation() + ", decimation: " + resampling.getDecimation());

      freqTranslation   = new ComplexNumberFrequencyTranslatingFilter(sampleRate, frequency, spec.getCenterFrequency());
      baseband          = FilterFactory.getKaiserBessel(channelRate, PASSBAND_STOP, STOPBAND_START, ATTENUATION, 1f);
      gainControl       = new ComplexNumberMovingGainControl((int) (channelRate / SYMBOL_RATE));
      cqpskDemodulation = new ComplexNumberCqpskDemodulator(channelRate, SYMBOL_RATE);

      QpskPolarSlicer slicer = new QpskPolarSlicer();
                      framer = new DataUnitFramer(Optional.of(cqpskDemodulation));

      freqTranslation.addSink(resampling);
      resampling.addSink(baseband);
      baseband.addSink(gainControl);
      gainControl.addSink(cqpskDemodulation);
      cqpskDemodulation.addSink(slicer);
      slicer.addSink(framer);

      spies.get(FilterType.TRANSLATION).forEach(freqTranslation::addSink);
      spies.get(FilterType.BASEBAND).forEach(baseband::addSink);
      spies.get(FilterType.GAIN).forEach(gainControl::addSink);
      spies.get(FilterType.DEMODULATION).forEach(cqpskDemodulation::addSink);

      spies.keySet().forEach(
          key -> spies.get(key).forEach(
              sink -> sink.onSourceStateChange(channelRate, 0d)
          )
      );

      sinks.forEach(framer::addSink);
    }
  }

  @Override
  public void addSink(Sink<DataUnit> sink) {
    synchronized (processChainLock) {
      super.addSink(sink);
      framer.addSink(sink);
    }
  }

  @Override
  public void removeSink(Sink<DataUnit> sink) {
    synchronized (processChainLock) {
      super.removeSink(sink);
      framer.removeSink(sink);
    }
  }

  public void addFilterSpy(FilterType type, DynamicSink<ComplexNumber> sink) {
    synchronized (processChainLock) {
      switch (type) {
        case TRANSLATION:
          freqTranslation.addSink(sink);
          break;

        case BASEBAND:
          baseband.addSink(sink);
          break;

        case GAIN:
          gainControl.addSink(sink);
          break;

        case DEMODULATION:
          cqpskDemodulation.addSink(sink);
          break;
      }

      sink.onSourceStateChange(channelRate, 0d);
      spies.get(type).add(sink);
    }
  }

  public void removeFilterSpy(FilterType type, DynamicSink<ComplexNumber> sink) {
    synchronized (processChainLock) {
      switch (type) {
        case TRANSLATION:
          freqTranslation.removeSink(sink);
          break;

        case BASEBAND:
          baseband.removeSink(sink);
          break;

        case GAIN:
          gainControl.removeSink(sink);
          break;

        case DEMODULATION:
          cqpskDemodulation.removeSink(sink);
          break;
      }

      spies.get(type).remove(sink);
    }
  }

  @Override
  public void consume(Samples samples) {
    if (!iqSampleQueue.offer(samples.getSamples())) {
      iqSampleQueue.clear();
      iqSampleQueue.offer(samples.getSamples());
      log.warn("sample queue for channel " + spec + " has overflowed");
    }
  }

  @Override
  public List<ComplexNumber> get() {
    try {

      float[] iqSamples = iqSampleQueue.take();
      return IntStream.range(0, iqSamples.length)
                      .filter(i -> ((i & 1) == 0) && i != (iqSamples.length + 1))
                      .mapToObj(i -> new ComplexNumber(iqSamples[i], iqSamples[i + 1]))
                      .collect(Collectors.toList());

    } catch (InterruptedException e) {
      throw new StreamInterruptedException("interrupted while supplying ComplexNumber stream", e);
    }
  }

  @Override
  public Void call() throws Exception {
    try {

      Stream.generate(this).forEach(samples -> {
        if (Thread.currentThread().isInterrupted())
          throw new StreamInterruptedException("interrupted while reading from ComplexNumber stream");

        synchronized (processChainLock) {
          samples.forEach(freqTranslation::consume);
        }
      });

    } catch (StreamInterruptedException e) {
      log.debug("channel " + spec + " interrupted, assuming execution was canceled");
    } finally {
      iqSampleQueue.clear();
    }

    return null;
  }

}
