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

public class TalkGroup {

  private String  alias;
  private Integer id;

  public TalkGroup() { }

  public TalkGroup(String alias, int id) {
    this.alias = alias;
    this.id    = id;
  }

  public String getAlias() {
    return alias;
  }

  public Integer getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TalkGroup talkGroup = (TalkGroup) o;
    return alias.equals(talkGroup.alias) && id.equals(talkGroup.id);
  }

  @Override
  public int hashCode() {
    return alias.hashCode() ^ id.hashCode();
  }

}
