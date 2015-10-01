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

import org.anhonesteffort.p25.LoggingDataUnitSink;
import org.anhonesteffort.p25.audio.ImbeAudioOutput;
import org.anhonesteffort.p25.plot.SpectrumFrame;
import org.anhonesteffort.p25.filter.SampleToSamplesConverter;
import org.anhonesteffort.p25.plot.ConstellationFrame;
import org.anhonesteffort.p25.sample.SamplesSourceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.LineUnavailableException;
import java.awt.event.WindowEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ControlChannelQualifier implements Callable<Boolean> {

  private static final Logger log = LoggerFactory.getLogger(ControlChannelQualifier.class);

  private final ExecutorService         pool;
  private final SamplesSourceController samplesController;
  private final P25Channel              channel;
  private final Integer                 systemId;
  private final Integer                 systemWacn;

  public ControlChannelQualifier(ExecutorService         pool,
                                 SamplesSourceController samplesController,
                                 P25Channel              channel,
                                 Integer                 systemId,
                                 Integer                 systemWacn)
  {
    this.pool              = pool;
    this.samplesController = samplesController;
    this.channel           = channel;
    this.systemId          = systemId;
    this.systemWacn        = systemWacn;
  }

  @Override
  public Boolean call() throws Exception {
    if (!samplesController.configureSourceForSink(channel)) {
      log.info("potential control channel " + channel.getSpec() + " is out of tunable range");
      return false;
    }

    Future<Void>             channelFuture      = pool.submit(channel);
    SpectrumFrame            spectrumFrame      = new SpectrumFrame();
    ConstellationFrame       constellationFrame = new ConstellationFrame();
    SampleToSamplesConverter hack               = new SampleToSamplesConverter();
    LoggingDataUnitSink      debugSink          = new LoggingDataUnitSink();
    ImbeAudioOutput          audioOutput        = null;

    hack.addSink(spectrumFrame);
    channel.addFilterSpy(P25Channel.FilterType.BASEBAND, hack);
    channel.addFilterSpy(P25Channel.FilterType.DEMODULATION, constellationFrame);
    channel.addSink(debugSink);

    try {

      audioOutput = new ImbeAudioOutput();
      channel.addSink(audioOutput);

    } catch (ReflectiveOperationException e) {
      log.warn("unable to load jmbe library, audio playback will not work", e);
    } catch (LineUnavailableException e) {
      log.warn("unable to open audio output, audio playback will not work", e);
    }

    spectrumFrame.setTitle("channel: " + channel.getSpec().getCenterFrequency());
    spectrumFrame.setSize(400, 300);
    spectrumFrame.setVisible(true);

    constellationFrame.setTitle("channel: " + channel.getSpec().getCenterFrequency());
    constellationFrame.setSize(300, 300);
    constellationFrame.setLocationRelativeTo(null);
    constellationFrame.setVisible(true);

    try {

      Thread.sleep(30000);

    } finally {
      if (audioOutput != null) {
        channel.removeSink(audioOutput);
        audioOutput.stop();
      }

      channel.removeSink(debugSink);
      channel.removeFilterSpy(P25Channel.FilterType.BASEBAND, hack);
      channel.removeFilterSpy(P25Channel.FilterType.DEMODULATION, constellationFrame);
      channelFuture.cancel(true);
      samplesController.removeSink(channel);
      constellationFrame.dispatchEvent(new WindowEvent(constellationFrame, WindowEvent.WINDOW_CLOSING));
      spectrumFrame.dispatchEvent(new WindowEvent(spectrumFrame, WindowEvent.WINDOW_CLOSING));
    }

    return false;
  }

}
