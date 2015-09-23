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

package org.anhonesteffort.p25.filter.gate;

import org.anhonesteffort.p25.primitive.DiBit;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class DiBitSyncGateTest {

  @Test
  public void test() {
    final long            SYNC        = 0x5575F5FF77FFl;
    final int             SYNC_LENGTH = 48;
    final int             DISTANCE    =  0;
    final SyncGate<DiBit> GATE        = new DiBitSyncGate(SYNC, SYNC_LENGTH, DISTANCE);
    final List<DiBit>     OUTPUT      = new LinkedList<>();

    GATE.addSink(new SyncGateSink<DiBit>() {
      @Override
      public void onSyncConsumed() {
        assert OUTPUT.isEmpty();
      }

      @Override
      public void consume(DiBit element) {
        OUTPUT.add(element);
      }
    });

    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(0));
    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(0));


    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(1));

    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(1));

    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(1));

    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(3));

    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(3));

    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(3));


    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(1));

    assert OUTPUT.size() == 3;
    OUTPUT.forEach(out -> {assert out.getValue() == 1;});
  }

  @Test
  public void testWithDistance() {
    final long            SYNC        = 0x5575F5FF77FFl;
    final int             SYNC_LENGTH = 48;
    final int             DISTANCE    =  3;
    final SyncGate<DiBit> GATE        = new DiBitSyncGate(SYNC, SYNC_LENGTH, DISTANCE);
    final List<DiBit>     OUTPUT      = new LinkedList<>();

    GATE.addSink(new SyncGateSink<DiBit>() {
      @Override
      public void onSyncConsumed() {
        assert OUTPUT.isEmpty();
      }

      @Override
      public void consume(DiBit element) {
        OUTPUT.add(element);
      }
    });

    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(0));
    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(0));


    GATE.consume(new DiBit(0));
    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(1));

    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(1));

    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(1));

    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(3));

    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(3));

    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(3));
    GATE.consume(new DiBit(0));


    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(1));
    GATE.consume(new DiBit(1));

    assert OUTPUT.size() == 3;
    OUTPUT.forEach(out -> {assert out.getValue() == 1;});
  }

}
