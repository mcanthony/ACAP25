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
import org.anhonesteffort.p25.protocol.frame.DataUnit;
import org.anhonesteffort.p25.protocol.frame.Duid;
import org.anhonesteffort.p25.protocol.frame.TrunkingSignalingDataUnit;
import org.anhonesteffort.p25.protocol.frame.tsbk.IdUpdateBlock;
import org.anhonesteffort.p25.protocol.frame.tsbk.NetworkStatusBroadcastMessage;
import org.anhonesteffort.p25.protocol.frame.tsbk.TrunkingSignalingBlock;
import org.anhonesteffort.p25.sample.SamplesSourceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ControlChannelQualifier implements Callable<Optional<Double>>, Sink<DataUnit> {

  private static final Logger log = LoggerFactory.getLogger(ControlChannelQualifier.class);

  private static final long QUALIFY_TIMEOUT_MS = 10000;

  private final ChannelIdUpdateBlockMap channelIdMap = new ChannelIdUpdateBlockMap();

  private final ExecutorService         pool;
  private final SamplesSourceController samples;
  private final P25Channel              channel;
  private final Integer                 systemId;
  private final Integer                 systemWacn;

  private Future<Void>     channelFuture = null;
  private Optional<Double> controlFreq   = Optional.empty();

  public ControlChannelQualifier(ExecutorService         pool,
                                 SamplesSourceController samples,
                                 P25Channel              channel,
                                 Integer                 systemId,
                                 Integer                 systemWacn)
  {
    this.pool       = pool;
    this.samples    = samples;
    this.channel    = channel;
    this.systemId   = systemId;
    this.systemWacn = systemWacn;
  }

  private void processTrunkStatus(NetworkStatusBroadcastMessage status) {
    if (status.getWacn() == systemWacn && status.getSystemId() == systemId) {
      Optional<IdUpdateBlock> idUpdate = channelIdMap.getBlockForId(status.getChannelId());

      if (idUpdate.isPresent()) {
        controlFreq = Optional.of(status.getDownlinkFreq(idUpdate.get()));
        channelFuture.cancel(true);
      } else {
        controlFreq = Optional.of(channel.getSpec().getCenterFrequency());
      }
    }
  }

  @Override
  public void consume(DataUnit element) {
    if (!element.isIntact())
      return;

    if (element.getNid().getDuid().getId() == Duid.ID_TRUNK_SIGNALING) {
      TrunkingSignalingDataUnit        trunkSignaling = (TrunkingSignalingDataUnit) element;
      Optional<TrunkingSignalingBlock> trunkStatus    = trunkSignaling.getFirstOf(P25.OPCODE_NETWORK_STATUS);

      trunkSignaling.getBlocks().forEach(channelIdMap::consume);
      if (trunkStatus.isPresent())
        processTrunkStatus((NetworkStatusBroadcastMessage) trunkStatus.get());
    }
  }

  @Override
  public Optional<Double> call() throws Exception {
    if (!samples.configureSourceForSink(channel)) {
      log.warn("potential control channel " + channel.getSpec() + " is out of tunable range");
      return Optional.empty();
    }

    channel.addSink(this);
    channelFuture = pool.submit(channel);

    try {

      channelFuture.get(QUALIFY_TIMEOUT_MS, TimeUnit.MILLISECONDS);

    } catch (CancellationException | TimeoutException e) { }
    finally {
      channel.removeSink(this);
      channelFuture.cancel(true);
      samples.removeSink(channel);
    }

    return controlFreq;
  }

}
