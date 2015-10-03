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

import org.anhonesteffort.p25.audio.ImbeAudioOutput;
import org.anhonesteffort.p25.plot.ConstellationFrame;
import org.anhonesteffort.p25.protocol.frame.DataUnit;
import org.anhonesteffort.p25.protocol.frame.Duid;
import org.anhonesteffort.p25.sample.SamplesSourceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class VoiceChannelCapture implements Runnable, ActionListener {

  private static final Logger log = LoggerFactory.getLogger(VoiceChannelCapture.class);

  private static final int MIN_DATA_UNIT_RATE = 2;

  private final ExecutorService         pool;
  private final SamplesSourceController samples;
  private final P25Channel              channel;
  private final Timer                   timer;

  private Future<Void> channelFuture = null;
  private boolean      dataProcessed = false;

  public VoiceChannelCapture(ExecutorService pool, SamplesSourceController samples, P25Channel channel) {
    this.pool    = pool;
    this.samples = samples;
    this.channel = channel;
         timer   = new Timer(1000 / MIN_DATA_UNIT_RATE, this);
  }

  private void processDataUnit(DataUnit dataUnit) {
    dataProcessed = true;

    switch (dataUnit.getNid().getDuid().getId()) {
      case Duid.ID_TERMINATOR_W_LINK:
      case Duid.ID_TERMINATOR_WO_LINK:
        channelFuture.cancel(true);
        break;
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!dataProcessed)
      channelFuture.cancel(true);

    dataProcessed = false;
  }

  @Override
  public void run() {
    if (!samples.configureSourceForSink(channel)) {
      log.error("voice channel " + channel.getSpec() + " is out of tunable range");
      return;
    }

    ImbeAudioOutput audioOutput = null;

    try {

      audioOutput = new ImbeAudioOutput();
      channel.addSink(audioOutput);

    } catch (ReflectiveOperationException e) {
      log.warn("unable to load jmbe library, audio playback will not work", e);
    } catch (LineUnavailableException e) {
      log.warn("unable to open audio output, audio playback will not work", e);
    }

    ConstellationFrame constellationFrame = new ConstellationFrame();
    constellationFrame.setTitle("voice channel: " + channel.getSpec().getCenterFrequency());
    constellationFrame.setSize(300, 300);
    constellationFrame.setVisible(true);

    channel.addFilterSpy(P25Channel.FilterType.DEMODULATION, constellationFrame);
    channel.addSink(this::processDataUnit);

    try {

      channelFuture = pool.submit(channel);
      timer.start();
      channelFuture.get();

    } catch (CancellationException | InterruptedException e){ }
      catch (Exception e) {
      log.error("exception thrown from voice channel, stopping capture", e);
    } finally {
      timer.stop();
      channelFuture.cancel(true);

      if (audioOutput != null) {
        channel.removeSink(audioOutput);
        audioOutput.stop();
      }

      channel.removeFilterSpy(P25Channel.FilterType.DEMODULATION, constellationFrame);
      constellationFrame.dispatchEvent(new WindowEvent(constellationFrame, WindowEvent.WINDOW_CLOSING));
      channel.removeSink(this::processDataUnit);
      samples.removeSink(channel);
    }
  }

}
