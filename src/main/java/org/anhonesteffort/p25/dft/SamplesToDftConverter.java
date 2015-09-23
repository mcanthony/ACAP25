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

import org.anhonesteffort.p25.SelfStartingSource;
import org.anhonesteffort.p25.Sink;
import org.anhonesteffort.p25.sample.Samples;
import org.anhonesteffort.p25.util.StreamInterruptedException;
import org.jtransforms.fft.FloatFFT_1D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SamplesToDftConverter extends SelfStartingSource<DftFrame, Sink<DftFrame>> implements Sink<Samples> {

  private static final Logger log = LoggerFactory.getLogger(SamplesToDftConverter.class);

  private final LinkedBlockingQueue<float[]> consumedSamples = new LinkedBlockingQueue<>();

  private final int             fftLength;
  private final FloatFFT_1D     fft;

  private ExecutorService executor;
  private float[]         queuedSamples;
  private int             queuedSamplesIndex;

  public SamplesToDftConverter(FftWidth fftWidth) {
    this.fftLength = fftWidth.getWidth() * 2;
    fft            = new FloatFFT_1D(fftWidth.getWidth());
  }

  @Override
  protected void startProducing() {
    assert executor == null || executor.isTerminated();

    consumedSamples.clear();
    queuedSamples      = new float[fftLength];
    queuedSamplesIndex = 0;

    executor = Executors.newFixedThreadPool(1);
    executor.submit(new DFTCalculationTask());
  }

  @Override
  protected void stopProducing() {
    assert !executor.isShutdown();
    executor.shutdownNow();
  }

  @Override
  public void consume(Samples samples) {
    if (executor != null && !executor.isShutdown() && !consumedSamples.offer(samples.getSamples())) {
      consumedSamples.clear();
      consumedSamples.offer(samples.getSamples());
      log.warn("sample receive queue has overflowed");
    }
  }

  private class DFTCalculationTask implements Supplier<float[]>, Runnable {

    private final Logger log = LoggerFactory.getLogger(DFTCalculationTask.class);

    @Override
    public float[] get() {
      try {

        float[] frame      = new float[fftLength];
        int     frameIndex = 0;

        while (frameIndex < fftLength) {
          while (frameIndex < fftLength && queuedSamplesIndex < queuedSamples.length) {
            frame[frameIndex++] = queuedSamples[queuedSamplesIndex++];
          }
          if (frameIndex < fftLength) {
            queuedSamples      = consumedSamples.take();
            queuedSamplesIndex = 0;
          }
        }

        return frame;

      } catch (InterruptedException e) {
        throw new StreamInterruptedException("interrupted while slicing dft frame", e);
      }
    }

    @Override
    public void run() {
      try {

        Stream.generate(this).forEach(frame -> {
          fft.complexForward(frame);
          broadcast(new DftFrame(frame));
        });

      } catch (StreamInterruptedException e) {
        log.debug("dft calculation stream interrupted, assuming intended shutdown");
      }
    }
  }

}
