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

public class System {

  private String          name;
  private Integer         id;
  private Integer         wideAreaCommNet;
  private List<Site>      sites;
  private List<TalkGroup> talkGroups;

  public System() { }

  public System(String          name,
                Integer         id,
                Integer         wideAreaCommNet,
                List<Site>      sites,
                List<TalkGroup> talkGroups)
  {
    this.name            = name;
    this.id              = id;
    this.wideAreaCommNet = wideAreaCommNet;
    this.sites           = sites;
    this.talkGroups      = talkGroups;
  }

  public String getName() {
    return name;
  }

  public Integer getId() {
    return id;
  }

  public Integer getWideAreaCommNet() {
    return wideAreaCommNet;
  }

  public List<Site> getSites() {
    return sites;
  }

  public List<TalkGroup> getTalkGroups() {
    return talkGroups;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    System system = (System) o;

    return name.equals(system.name) && id.equals(system.id) &&
           wideAreaCommNet.equals(system.wideAreaCommNet) && sites.equals(system.sites) &&
           talkGroups.equals(system.talkGroups);
  }

  @Override
  public int hashCode() {
    return name.hashCode() ^ wideAreaCommNet.hashCode() ^ sites.hashCode() ^ talkGroups.hashCode();
  }

}
