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

package org.anhonesteffort.p25.sample.usrp;

import org.anhonesteffort.p25.sample.Samples;
import org.anhonesteffort.p25.sample.TunableSamplesSource;
import org.anhonesteffort.p25.sample.SamplesSourceException;
import org.anhonesteffort.p25.sample.SamplesSourceBrokenException;
import org.anhonesteffort.p25.util.StreamInterruptedException;
import guard.banana.uhd.RxStreamer;
import guard.banana.uhd.StreamArgs;
import guard.banana.uhd.types.DeviceAddress;
import guard.banana.uhd.types.RxMetadata;
import guard.banana.uhd.types.StreamCommand;
import guard.banana.uhd.types.TuneRequest;
import guard.banana.uhd.types.TuneResult;
import guard.banana.uhd.usrp.MultiUsrp;
import guard.banana.uhd.util.ComplexFloatVector;
import guard.banana.uhd.util.StringVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;
import java.util.stream.Stream;

// todo: should work for all usrp devices
public class UsrpB100 extends TunableSamplesSource {

  private final Logger log = LoggerFactory.getLogger(UsrpB100.class);

  private final LinkedBlockingQueue<float[]> producedSamples = new LinkedBlockingQueue<>(100);
  private       ExecutorService              executor;
  private       SampleProducer               sampleProducer;

  private final DeviceAddress  address;
  private final MultiUsrp      multiUsrp;
  private final UsrpB100Config config;
  private       double         clockRate;
  private       long           rxBufferSize = UsrpB100Config.DEFAULT_RX_BUFFER_SIZE;

  protected UsrpB100(DeviceAddress address, MultiUsrp multiUsrp, UsrpB100Config config)
      throws SamplesSourceException
  {
    super(UsrpB100Config.MAX_SAMPLE_RATE,
          UsrpB100Config.MIN_FREQUENCY,
          UsrpB100Config.MAX_FREQUENCY);

    this.address   = address;
    this.multiUsrp = multiUsrp;
    this.config    = config;

    applyConfig();
  }

  private void applyConfig() throws SamplesSourceException {
    rxBufferSize = config.getRxBufferSize();

    try {

      multiUsrp.set_clock_source(config.getClockSource(), MultiUsrp.ALL_MBOARDS);
      multiUsrp.set_rx_antenna(config.getRxAntenna(), 0);

      multiUsrp.set_rx_gain(config.getRxGainAdcPga(), "ADC-pga", 0);
      multiUsrp.set_rx_gain(config.getRxGainPga0(), "PGA0", 0);

      clockRate = multiUsrp.get_master_clock_rate(0);
      setSampleRate(config.getRxSampleRate());
      setFrequency(config.getFrequency());

      StringVector rxSensorNames = multiUsrp.get_rx_sensor_names(0);
      for (long i = 0; i < rxSensorNames.size(); i++) {
        if (rxSensorNames.get(i).equals("lo_locked")) {
          if (!multiUsrp.get_rx_sensor("lo_locked", 0).to_bool())
            throw new SamplesSourceException("ettus says that lo_locked must be true");
          break;
        }
      }

    } catch (RuntimeException e) {
      throw new SamplesSourceException("unable to configure uhd device " + address.to_string(), e);
    }
  }

  @Override
  protected Long setSampleRate(Long minSampleRate) throws SamplesSourceException {
    int  decimation        = (int) Math.ceil(clockRate / minSampleRate);
    long allowedSampleRate = (long) (clockRate / decimation);

    if (allowedSampleRate < minSampleRate) {
      decimation        = ((decimation & 1) == 1) ? decimation - 1 : decimation - 2;
      allowedSampleRate = (long) (clockRate / decimation);
    }

    try {

      multiUsrp.set_rx_rate(allowedSampleRate);
      this.sampleRate = (long) multiUsrp.get_rx_rate(0);
      return this.sampleRate;

    } catch (RuntimeException e) {
      throw new SamplesSourceException("error setting usrp sample rate " + address.to_string(), e);
    }
  }

  @Override
  protected Double setFrequency(Double frequency) throws SamplesSourceException {
    try {

      TuneRequest tuneRequest = new TuneRequest(frequency);
      TuneResult  tuneResult  = multiUsrp.set_rx_freq(tuneRequest, 0);

      this.frequency = tuneResult.actual_rf_freq();
      return this.frequency;

    } catch (RuntimeException e) {
      throw new SamplesSourceException("error setting usrp frequency " + address.to_string(), e);
    }
  }

  @Override
  protected void startProducing() {
    log.debug("start producing");

    assert executor       == null || executor.isTerminated();
    assert sampleProducer == null || sampleProducer.isStopped();

    executor       = Executors.newFixedThreadPool(2);
    sampleProducer = new SampleProducer();

    executor.submit(sampleProducer);
    executor.submit(new SamplesBroadcaster());
  }

  @Override
  protected void stopProducing() {
    log.debug("stop producing");

    assert !sampleProducer.isStopped();
    assert !executor.isShutdown();

    sampleProducer.stop();
    executor.shutdownNow();
  }

  private class SampleProducer implements Runnable {

    private final Logger log = LoggerFactory.getLogger(SampleProducer.class);

    private final RxStreamer rxStreamer;
    private final Object     streamLock = new Object();
    private       boolean    stopped    = true;

    public SampleProducer() {
      rxStreamer = multiUsrp.getRxStream(new StreamArgs("fc32", "sc16"));
    }

    public boolean isStopped() {
      return stopped;
    }

    private void start() {
      multiUsrp.issue_stream_cmd(
          new StreamCommand(StreamCommand.START_CONTINUOUS),
          MultiUsrp.ALL_CHANS
      );
      stopped = false;
    }

    private void drainHardwareBuffer() {
      long               drainTimeout  = System.currentTimeMillis() + 5000;
      RxMetadata         rxMetadata    = new RxMetadata();
      ComplexFloatVector samplesVector = new ComplexFloatVector(rxBufferSize);

      while (rxMetadata.error_code()    != RxMetadata.ERROR_TIMEOUT &&
             System.currentTimeMillis() <= drainTimeout)
      {
        synchronized (streamLock) {
          rxStreamer.recv(samplesVector.front(),
                          samplesVector.size(),
                          rxMetadata, 0.1, false);
        }
      }

      if (rxMetadata.error_code() != RxMetadata.ERROR_TIMEOUT)
        throw new SamplesSourceBrokenException("failed to drain usrp hardware receive buffer");
    }

    public void stop() {
      try {

        multiUsrp.issue_stream_cmd(
            new StreamCommand(StreamCommand.STOP_CONTINUOUS),
            MultiUsrp.ALL_CHANS
        );

        stopped = true;
        producedSamples.clear();
        drainHardwareBuffer();

      } catch (RuntimeException e) {
        throw new SamplesSourceBrokenException("unable to stop usrp receive stream", e);
      }
    }

    @Override
    public void run() {
      ComplexFloatVector samplesVector = new ComplexFloatVector(rxBufferSize);
      RxMetadata         rxMetadata    = new RxMetadata();

      try {

        start();
        while (!stopped) {
          synchronized (streamLock) {
            rxStreamer.recv(samplesVector.front(),
                            samplesVector.size(),
                            rxMetadata, 0.1, false);
          }

          if (rxMetadata.error_code() == RxMetadata.ERROR_OVERFLOW) {
            log.warn("usrp hardware receive buffer has overflowed");
          } else if (rxMetadata.error_code() != RxMetadata.ERROR_NONE) {
            throw new SamplesSourceBrokenException("usrp receive returned error " + rxMetadata.error_code());
          } else if (!stopped) {
            producedSamples.put(samplesVector.toFloatArray());
          }

          samplesVector = new ComplexFloatVector(rxBufferSize);
        }

      } catch (RuntimeException e) {
        throw new SamplesSourceBrokenException("error receiving samples from usrp", e);
      } catch (InterruptedException e) {
        log.debug("usrp receive loop interrupted, assuming intended shutdown");
      }
    }
  }

  private class SamplesBroadcaster implements Supplier<float[]>, Runnable {

    private final Logger log = LoggerFactory.getLogger(SamplesBroadcaster.class);

    @Override
    public float[] get() {
      try {

        return producedSamples.take();

      } catch (InterruptedException e) {
        throw new StreamInterruptedException("interrupted while waiting on samples", e);
      }
    }

    @Override
    public void run() {
      try {

        Stream.generate(this).forEach(samples -> broadcast(new Samples(samples)));

      } catch (StreamInterruptedException e) {
        log.debug("sample broadcast stream interrupted, assuming intended shutdown");
      }
    }
  }
}
