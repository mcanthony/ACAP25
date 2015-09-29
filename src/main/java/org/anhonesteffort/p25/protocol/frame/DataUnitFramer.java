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

package org.anhonesteffort.p25.protocol.frame;

import org.anhonesteffort.p25.Sink;
import org.anhonesteffort.p25.Source;
import org.anhonesteffort.p25.filter.gate.DiBitSyncGate;
import org.anhonesteffort.p25.filter.demod.ComplexNumberCqpskDemodulator;
import org.anhonesteffort.p25.filter.gate.SyncGateSink;
import org.anhonesteffort.p25.primitive.DiBit;
import org.anhonesteffort.p25.protocol.P25;
import org.anhonesteffort.p25.util.DiBitByteBufferSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class DataUnitFramer extends Source<DataUnit, Sink<DataUnit>> implements Sink<DiBit> {

  private static final Logger log = LoggerFactory.getLogger(DataUnitFramer.class);

  private static final int SYNC_HAMMING_DISTANCE = 2;

  private final List<DiBitSyncGate> syncGates      = new LinkedList<>();
  private final List<DiBitSyncGate> cqpskSyncGates = new LinkedList<>();
  private final Optional<ComplexNumberCqpskDemodulator> cqpsk;

  private int syncCount    = 0;
  private int duCount      = 0;
  private int corruptCount = 0;

  public DataUnitFramer(Optional<ComplexNumberCqpskDemodulator> cqpsk) {
    this.cqpsk = cqpsk;

    if (!cqpsk.isPresent()) {
      DiBitSyncGate syncGate = new DiBitSyncGate(P25.SYNC0DEG, P25.SYNC_BIT_LENGTH, 0);
      Framer        framer   = new Framer(syncGate);

      syncGate.addSink(framer);
      syncGates.add(syncGate);
    } else {
      DiBitSyncGate syncGate0   = new DiBitSyncGate(P25.SYNC0DEG,   P25.SYNC_BIT_LENGTH, 0);
      DiBitSyncGate syncGate90  = new DiBitSyncGate(P25.SYNC90DEG,  P25.SYNC_BIT_LENGTH, 0);
      DiBitSyncGate syncGate180 = new DiBitSyncGate(P25.SYNC180DEG, P25.SYNC_BIT_LENGTH, 0);
      DiBitSyncGate syncGate270 = new DiBitSyncGate(P25.SYNC270DEG, P25.SYNC_BIT_LENGTH, 0);
      Framer        framer0     = new Framer(syncGate0);
      Framer        framer90    = new Framer(syncGate90);
      Framer        framer180   = new Framer(syncGate180);
      Framer        framer270   = new Framer(syncGate270);

      syncGate0.addSink(framer0);
      syncGate90.addSink(framer90);
      syncGate180.addSink(framer180);
      syncGate270.addSink(framer270);

      cqpskSyncGates.add(syncGate0);
      cqpskSyncGates.add(syncGate90);
      cqpskSyncGates.add(syncGate180);
      cqpskSyncGates.add(syncGate270);
    }
  }

  @Override
  public void consume(DiBit element) {
    List<DiBitSyncGate> syncGatesCopy = new LinkedList<>(syncGates);
    syncGatesCopy.forEach(gate -> gate.consume(element));

    if (!cqpskSyncGates.isEmpty()) {
      List<DiBitSyncGate> cqpskSyncGatesCopy = new LinkedList<>(cqpskSyncGates);
      cqpskSyncGatesCopy.forEach(gate -> gate.consume(element));
    }
  }

  private void onSyncConsumed(DiBitSyncGate sourceGate) {
    log.debug("on sync consumed: " + (++syncCount));

    if (!cqpskSyncGates.isEmpty()) {
      if (sourceGate.getSync() == P25.SYNC90DEG)
        cqpsk.get().correctPhaseError(90f);
      else if (sourceGate.getSync() == P25.SYNC180DEG)
        cqpsk.get().correctPhaseError(180f);
      else if (sourceGate.getSync() == P25.SYNC270DEG)
        cqpsk.get().correctPhaseError(270f);

      cqpskSyncGates.clear();
      syncGates.add(sourceGate);
    }

    DiBitSyncGate syncGate = new DiBitSyncGate(P25.SYNC0DEG, P25.SYNC_BIT_LENGTH, SYNC_HAMMING_DISTANCE);
    Framer        framer   = new Framer(syncGate);

    syncGate.addSink(framer);
    syncGates.add(syncGate);
  }

  private void onNidCorrupt(DiBitSyncGate sourceGate) {
    log.warn("on nid corrupt: " + (++corruptCount));
    syncGates.remove(sourceGate);
  }

  private void onDataUnitComplete(DiBitSyncGate sourceGate, DataUnit dataUnit) {
    log.debug("on data unit complete: " + (++duCount) + ", " + dataUnit.getNid());
    syncGates.remove(sourceGate);

    switch (dataUnit.getNid().getDuid().getId()) {
      case Duid.ID_HEADER:
        broadcast(new HeaderDataUnit(dataUnit.getNid(), dataUnit.sink));
        break;

      case Duid.ID_TERMINATOR_WO_LINK:
        broadcast(new SimpleTerminatorDataUnit(dataUnit.getNid(), dataUnit.sink));
        break;

      case Duid.ID_LLDU1:
        broadcast(new LogicalLinkDataUnit1(dataUnit.getNid(), dataUnit.sink));
        break;

      case Duid.ID_LLDU2:
        broadcast(new LogicalLinkDataUnit2(dataUnit.getNid(), dataUnit.sink));
        break;

      case Duid.ID_TERMINATOR_W_LINK:
        broadcast(new LinkControlWordTerminatorDataUnit(dataUnit.getNid(), dataUnit.sink));
        break;

      default:
        broadcast(dataUnit);
    }
  }

  private class Framer implements SyncGateSink<DiBit> {

    private final DiBitByteBufferSink nidSink = new DiBitByteBufferSink(P25.NID_LENGTH);
    private final DiBitSyncGate       syncGate;

    private DataUnit dataUnit;
    private int statusSymbolCounter = 24;

    public Framer(DiBitSyncGate syncGate) {
      this.syncGate = syncGate;
    }

    @Override
    public void onSyncConsumed() {
      DataUnitFramer.this.onSyncConsumed(syncGate);
    }

    @Override
    public void consume(DiBit element) {
      if (statusSymbolCounter == 35) {
        statusSymbolCounter = 0;
        return;
      }

      if (!nidSink.isFull()) {
        nidSink.consume(element);
        if (nidSink.isFull()) {
          Nid nid = new Nid(nidSink.getBytes().array());
          if (nid.isIntact())
            dataUnit = new DataUnit(nid);
          else
            DataUnitFramer.this.onNidCorrupt(syncGate);
        }
      }

      else if (!dataUnit.isFull()) {
        dataUnit.consume(element);
        if (dataUnit.isFull())
          DataUnitFramer.this.onDataUnitComplete(syncGate, dataUnit);
      }

      statusSymbolCounter++;
    }

  }
}
