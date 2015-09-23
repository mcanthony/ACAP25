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

package org.anhonesteffort.p25.primitive;

import org.junit.Test;

public class ChannelSpecTest {

  @Test
  public void testConstructFromCenterFreqBwSampleRate() {
    final Double      CENTER_FREQ  = 1_000_000d;
    final Double      BANDWIDTH    =    20_000d;
    final Long        SAMPLE_RATE  =    40_000l;
    final ChannelSpec CHANNEL_SPEC = new ChannelSpec(CENTER_FREQ, BANDWIDTH, SAMPLE_RATE);

    assert CHANNEL_SPEC.getCenterFrequency().equals(CENTER_FREQ);
    assert CHANNEL_SPEC.getBandwidth().equals(BANDWIDTH);
    assert CHANNEL_SPEC.getSampleRate().equals(SAMPLE_RATE);
  }

  @Test
  public void testConstructFromCenterFreqAndBw() {
    final Double      CENTER_FREQ  = 1_000_000d;
    final Double      BANDWIDTH    =    20_000d;
    final ChannelSpec CHANNEL_SPEC = new ChannelSpec(CENTER_FREQ, BANDWIDTH);

    assert CHANNEL_SPEC.getCenterFrequency().equals(CENTER_FREQ);
    assert CHANNEL_SPEC.getBandwidth().equals(BANDWIDTH);
    assert CHANNEL_SPEC.getSampleRate() == (BANDWIDTH * 2);
  }

  @Test
  public void testConstructFromMinMaxFreqRate() {
    final Double      MIN_FREQ     =   980_000d;
    final Double      MAX_FREQ     = 1_020_000d;
    final Long        SAMPLE_RATE  =     40000l;
    final ChannelSpec CHANNEL_SPEC = ChannelSpec.fromMinMax(MIN_FREQ, MAX_FREQ, SAMPLE_RATE);

    assert CHANNEL_SPEC.getMinFreq().equals(MIN_FREQ);
    assert CHANNEL_SPEC.getMaxFreq().equals(MAX_FREQ);
    assert CHANNEL_SPEC.getSampleRate().equals(SAMPLE_RATE);
  }

  @Test
  public void testConstructFromMinMaxFreq() {
    final Double      MIN_FREQ     =   980_000d;
    final Double      MAX_FREQ     = 1_020_000d;
    final ChannelSpec CHANNEL_SPEC = ChannelSpec.fromMinMax(MIN_FREQ, MAX_FREQ);

    assert CHANNEL_SPEC.getMinFreq().equals(MIN_FREQ);
    assert CHANNEL_SPEC.getMaxFreq().equals(MAX_FREQ);
  }

  @Test
  public void testChannelContainsChannel() {
    assert new ChannelSpec(100d, 50d).containsChannel(
           new ChannelSpec(100d, 25d)
    );
    assert new ChannelSpec(100d, 50d).containsChannel(
           new ChannelSpec(100d, 50d)
    );
    assert new ChannelSpec(100d, 50d, 200l).containsChannel(
           new ChannelSpec(100d, 50d, 100l)
    );

    assert !(new ChannelSpec(100d, 50d).containsChannel(
             new ChannelSpec(100d, 75d)
    ));
    assert !(new ChannelSpec(100d, 50d).containsChannel(
             new ChannelSpec(125d, 50d)
    ));
    assert !(new ChannelSpec(100d, 50d, 100l).containsChannel(
             new ChannelSpec(100d, 50d, 200l)
    ));
  }

}
