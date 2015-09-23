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

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.anhonesteffort.p25.util.TestingYamlUtil.asYaml;
import static org.anhonesteffort.p25.util.TestingYamlUtil.fromYaml;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SystemTest {

  public static System buildSystem() {
    return new System(
        "sys00", 0, 111111, Arrays.asList(
          new Site("site00", 0, 1, Arrays.asList(222.22222, 333.33333)),
          new Site("site01", 1, 2, Arrays.asList(333.33333, 444.44444))
    ), Arrays.asList(
        new TalkGroup("tg00", 0), new TalkGroup("tg01", 1)
    ));
  }

  public static List<System> buildSystems() {
    return Arrays.asList(
        new System(
            "sys00", 0, 111111, Arrays.asList(
              new Site("site00", 0, 1, Arrays.asList(222.22222, 333.33333)),
              new Site("site01", 1, 2, Arrays.asList(333.33333, 444.44444))
            ),
            Arrays.asList(new TalkGroup("tg00", 0), new TalkGroup("tg01", 1))
        ),
        new System(
            "sys01", 1, 222222, Arrays.asList(
              new Site("site02", 2, 3, Arrays.asList(444.44444, 555.55555))
            ),
            Arrays.asList(new TalkGroup("tg02", 2))
        )
    );
  }

  @Test
  public void testSerializeToYml() throws Exception {
    assertThat("System can be serialized to YAML",
        asYaml(buildSystem()),
        is(equalTo(asYaml("one_system.yml", System.class))));
  }

  @Test
  public void testDeserializeFromYml() throws Exception {
    assertThat("System can be deserialized from YAML",
        fromYaml("one_system.yml", System.class),
        is(buildSystem()));
  }

  @Test
  public void testDeserializeListFromYml() throws Exception {
    assertThat("a List of Systems can be deserialized from YAML",
        Arrays.asList(fromYaml("two_systems.yml", System[].class)),
        is(buildSystems()));
  }

}
