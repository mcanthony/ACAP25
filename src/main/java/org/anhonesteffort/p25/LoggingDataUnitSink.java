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

import org.anhonesteffort.p25.protocol.frame.DataUnit;
import org.anhonesteffort.p25.protocol.frame.Duid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingDataUnitSink implements Sink<DataUnit> {

  private final static Logger log = LoggerFactory.getLogger(LoggingDataUnitSink.class);

  @Override
  public void consume(DataUnit element) {
    switch (element.getNid().getDuid().getId()) {
      case Duid.ID_HEADER:
        log.debug("decoded hdu: " + element);
        break;

      case Duid.ID_LLDU1:
        log.debug("decoded lldu1: " + element);
        break;

      case Duid.ID_TRUNK_SIGNALING:
        log.debug("decoded tsdu: " + element);
        break;

      case Duid.ID_LLDU2:
        log.debug("decoded lldu2: " + element);
        break;

      case Duid.ID_TERMINATOR_W_LINK:
        log.debug("decoded terminator w/ link control");
        break;

      case Duid.ID_TERMINATOR_WO_LINK:
        log.debug("decoded terminator w/o link control");
        break;

      case Duid.ID_PACKET:
        log.debug("decoded a packet 0.o");
        break;
    }
  }

}
