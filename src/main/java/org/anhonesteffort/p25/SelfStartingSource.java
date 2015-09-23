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

import org.anhonesteffort.p25.util.Copyable;

public abstract class SelfStartingSource<T extends Copyable<T>, L extends Sink<T>> extends Source<T, L> {

  protected abstract void startProducing();

  protected abstract void stopProducing();

  @Override
  public void addSink(L sink) {
    synchronized (sinks) {
      if (sinks.isEmpty())
        startProducing();
      sinks.add(sink);
    }
  }

  @Override
  public void removeSink(L sink) {
    synchronized (sinks) {
      sinks.remove(sink);
      if (sinks.isEmpty())
        stopProducing();
    }
  }

}
