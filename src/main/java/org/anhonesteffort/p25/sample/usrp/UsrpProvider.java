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

import org.anhonesteffort.p25.sample.TunableSamplesSource;
import org.anhonesteffort.p25.sample.SamplesSourceException;
import org.anhonesteffort.p25.sample.TunableSamplesSourceProvider;
import guard.banana.uhd.Device;
import guard.banana.uhd.types.DeviceAddress;
import guard.banana.uhd.types.DeviceAddresses;
import guard.banana.uhd.usrp.MultiUsrp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

// todo: should work for all usrp devices
public class UsrpProvider implements TunableSamplesSourceProvider {

  private static final Logger log = LoggerFactory.getLogger(UsrpProvider.class);

  @Override
  public Optional<TunableSamplesSource> get() {
    try {

      DeviceAddresses addresses = Device.find(new DeviceAddress(""));
      if (addresses.size() < 1) {
        throw new SamplesSourceException("no uhd devices found");
      }

      DeviceAddress address   = new DeviceAddress(addresses.get(0).to_string());
      MultiUsrp     multiUsrp = MultiUsrp.build(address);

      if (multiUsrp.get_num_mboards() != 1 || multiUsrp.get_rx_num_channels() != 1)
        throw new SamplesSourceException("I don't know how to use that hardware");

      return Optional.of(
          new UsrpB100(address, multiUsrp, new UsrpB100Config())
      );

    } catch (SamplesSourceException e) {
      log.warn("error building usrp", e);
      return Optional.empty();
    }
  }

}
