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

import org.anhonesteffort.p25.plot.ConstellationFrame;
import org.anhonesteffort.p25.protocol.frame.DataUnit;
import org.anhonesteffort.p25.protocol.frame.Duid;
import org.anhonesteffort.p25.protocol.frame.TrunkingSignalingDataUnit;
import org.anhonesteffort.p25.protocol.frame.tsbk.GroupVoiceChannelGrant;
import org.anhonesteffort.p25.protocol.frame.tsbk.IdUpdateBlock;
import org.anhonesteffort.p25.sample.SamplesSourceController;
import org.anhonesteffort.p25.sample.SamplesSourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ControlChannelCapture implements Callable<Void> {

  private static final Logger log = LoggerFactory.getLogger(ControlChannelCapture.class);

  private static final int CONCURRENT_VOICE_CHANNELS = 1;

  private final ExecutorService         voicePool    = Executors.newFixedThreadPool(CONCURRENT_VOICE_CHANNELS * 2);
  private final List<Future<?>>         futures      = new LinkedList<>();
  private final ChannelIdUpdateBlockMap channelIdMap = new ChannelIdUpdateBlockMap();

  private final SamplesSourceController samples;
  private final P25Channel              channel;

  public ControlChannelCapture(SamplesSourceController samples, P25Channel channel) {
    this.samples = samples;
    this.channel = channel;
  }

  private void processVoiceChannelGrant(GroupVoiceChannelGrant channelGrant) {
    boolean poolFull = futures.stream().filter(future -> !future.isDone()).count() >= CONCURRENT_VOICE_CHANNELS;
    if (poolFull) {
      log.warn("unable to process voice channel grant because voice pool is full");
      return;
    }

    Integer                 channelId = channelGrant.getChannelId();
    Optional<IdUpdateBlock> idBlock   = channelIdMap.getBlockForId(channelId);
    if (!idBlock.isPresent()) {
      log.warn("unable to process voice channel grant because id map is missing channel id " + channelId);
      return;
    }

    Double              voiceRxFreq  = channelGrant.getDownlinkFreq(idBlock.get());
    P25ChannelSpec      voiceSpec    = new P25ChannelSpec(voiceRxFreq);
    P25Channel          voiceChannel = new P25Channel(voiceSpec);
    VoiceChannelCapture voiceCapture = new VoiceChannelCapture(voicePool, samples, voiceChannel);

    futures.add(voicePool.submit(voiceCapture));
  }

  private void processDataUnit(DataUnit dataUnit) {
    if (!dataUnit.isIntact())
      return;

    switch (dataUnit.getNid().getDuid().getId()) {
      case Duid.ID_TRUNK_SIGNALING:
        TrunkingSignalingDataUnit trunkingDataUnit = (TrunkingSignalingDataUnit) dataUnit;
        trunkingDataUnit.getBlocks().forEach(trunkingBlock -> {
          channelIdMap.consume(trunkingBlock);

          switch (trunkingBlock.getOpCode()) {
            case P25.TSBK_GROUP_VOICE_CHAN_GRANT:
              processVoiceChannelGrant((GroupVoiceChannelGrant) trunkingBlock);
              break;
          }
        });
    }
  }

  @Override
  public Void call() throws Exception {
    if (!samples.configureSourceForSink(channel))
      throw new SamplesSourceException("control channel " + channel.getSpec() + " is out of tunable range");

    ConstellationFrame constellationFrame = new ConstellationFrame();
    constellationFrame.setTitle("control channel: " + channel.getSpec().getCenterFrequency());
    constellationFrame.setSize(300, 300);
    constellationFrame.setLocationRelativeTo(null);
    constellationFrame.setVisible(true);

    channel.addSink(this::processDataUnit);
    channel.addFilterSpy(P25Channel.FilterType.DEMODULATION, constellationFrame);

    try {

      channel.call();

    } catch (Exception e) {
      log.error("exception thrown from control channel, stopping capture", e);
    } finally {
      voicePool.shutdownNow();
      channel.removeSink(this::processDataUnit);
      channel.removeFilterSpy(P25Channel.FilterType.DEMODULATION, constellationFrame);
      constellationFrame.dispatchEvent(new WindowEvent(constellationFrame, WindowEvent.WINDOW_CLOSING));
      samples.removeSink(channel);
    }

    return null;
  }

}
