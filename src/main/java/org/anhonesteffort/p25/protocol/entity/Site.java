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

package org.anhonesteffort.p25.protocol.entity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Site {

  private String           name;
  private Integer          id;
  private Integer          rfSubSystem;
  private List<Double>     controlChannels;
  private Optional<Double> activeControlChannel = Optional.empty();

  public Site() { }

  public Site(String name, Integer id, Integer rfSubSystem, List<Double> controlChannels) {
    this.name            = name;
    this.id              = id;
    this.rfSubSystem     = rfSubSystem;
    this.controlChannels = controlChannels;
  }

  public String getName() {
    return name;
  }

  public Integer getId() {
    return id;
  }

  public Integer getRfSubSystem() {
    return rfSubSystem;
  }

  public List<Double> getControlChannels() {
    return controlChannels.stream()
                          .map(freq -> (freq * 1000000))
                          .collect(Collectors.<Double>toList());
  }

  public void setActiveControlChannel(Optional<Double> activeControlChannel) {
    this.activeControlChannel = activeControlChannel;
  }

  public Optional<Double> getActiveControlChannel() {
    return activeControlChannel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Site site = (Site) o;

    if (activeControlChannel.isPresent() && site.activeControlChannel.isPresent()) {
      if (!activeControlChannel.get().equals(site.activeControlChannel.get()))
        return false;
    } else if (activeControlChannel.isPresent() || site.activeControlChannel.isPresent()) {
      return false;
    }

    return name.equals(site.name) && id.equals(site.id) &&
           rfSubSystem.equals(site.rfSubSystem) && controlChannels.equals(site.controlChannels);
  }

  @Override
  public int hashCode() {
    return name.hashCode() ^ id.hashCode() ^ rfSubSystem.hashCode() ^
           controlChannels.hashCode() ^ activeControlChannel.hashCode();
  }

}
