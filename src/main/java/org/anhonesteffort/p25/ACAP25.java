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

package org.anhonesteffort.p25;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.anhonesteffort.p25.protocol.ControlChannelCapture;
import org.anhonesteffort.p25.protocol.ControlChannelQualifier;
import org.anhonesteffort.p25.protocol.P25Channel;
import org.anhonesteffort.p25.protocol.P25ChannelSpec;
import org.anhonesteffort.p25.protocol.entity.System;
import org.anhonesteffort.p25.protocol.entity.Site;
import org.anhonesteffort.p25.sample.SamplesSourceController;
import org.anhonesteffort.p25.sample.SamplesSourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.icm.jlargearrays.ConcurrencyUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ACAP25 implements Runnable {

  private static final double DC_OFFSET_HZ = 100000d;

  private static final Logger          log                = LoggerFactory.getLogger(ACAP25.class);
  private static final ExecutorService controlChannelPool = Executors.newFixedThreadPool(2);

  private final List<System>            systems;
  private final SamplesSourceController samplesController;

  public ACAP25(List<System> systems) throws SamplesSourceException {
    this.systems      = systems;
    samplesController = new SamplesSourceController(DC_OFFSET_HZ);
  }

  private Optional<Double> findActiveControlChannel(Integer systemId, Integer systemWacn, Site site) {
    for (Double channelFreq : site.getControlChannels()) {
      P25ChannelSpec          channelSpec      = new P25ChannelSpec(channelFreq);
      P25Channel              channel          = new P25Channel(channelSpec);
      ControlChannelQualifier channelQualifier = new ControlChannelQualifier(controlChannelPool, samplesController, channel, systemId, systemWacn);

      try {

        Future<Optional<Double>> channelQualified = controlChannelPool.submit(channelQualifier);
        Optional<Double>         controlFreq      = channelQualified.get();

        if (controlFreq.isPresent()) {
          log.info("found active control channel at " + controlFreq.get() +
                   " for site " + site.getName());
          return Optional.of(controlFreq.get());
        }

      } catch (InterruptedException | ExecutionException e) {
        log.error("error while identifying active control channels, exiting", e);
        java.lang.System.exit(1);
      }
    }

    return Optional.empty();
  }

  private void handleStartCapture(System system) {
    log.info("starting capture of system " + system.getName());

    Double controlFreq = system.getSites()
                               .stream().filter(site -> site.getActiveControlChannel().isPresent())
                               .mapToDouble(site -> site.getActiveControlChannel().get())
                               .findFirst().getAsDouble();

    P25ChannelSpec        controlSpec    = new P25ChannelSpec(controlFreq);
    P25Channel            controlChannel = new P25Channel(controlSpec);
    ControlChannelCapture capture        = new ControlChannelCapture(samplesController, controlChannel);

    controlChannelPool.submit(capture);
  }

  @Override
  public void run() {
    log.info("parsed " + systems.size() + " P25 trunked systems from config file");

    systems.forEach(
        system -> system.getSites().forEach(
            site -> site.setActiveControlChannel(
                findActiveControlChannel(system.getId(), system.getWideAreaCommNet(), site)
            )
        )
    );
    systems.stream().filter(
        system -> system.getSites().stream().anyMatch(
            site -> site.getActiveControlChannel().isPresent()
        )
    ).forEach(this::handleStartCapture);

    try {

      controlChannelPool.shutdown();
      controlChannelPool.awaitTermination(120, TimeUnit.SECONDS);

    } catch (InterruptedException e) {
      log.warn("interrupted while awaiting shutdown", e);
    } finally {
      controlChannelPool.shutdownNow();
      ConcurrencyUtils.shutdownThreadPoolAndAwaitTermination();
    }

    log.info("control channel pool shutdown, should exit now");
  }

  public static void main(String[] args) {
    try {

      File         systemsFile = new File("test.yml");
      ObjectMapper mapper      = new ObjectMapper(new YAMLFactory());
      List<System> systems     = Arrays.asList(mapper.readValue(systemsFile, System[].class));

      new ACAP25(systems).run();

    } catch (IOException | SamplesSourceException e) {
      e.printStackTrace();
    }
  }

}
