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
import java.util.Optional;

import static org.anhonesteffort.p25.util.TestingYamlUtil.asYaml;
import static org.anhonesteffort.p25.util.TestingYamlUtil.fromYaml;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SiteTest {

  public static Site buildSite() {
    return new Site(
        "site00", 0, 1, Arrays.asList(222.22222, 333.33333)
    );
  }

  @Test
  public void testSerializeToYml() throws Exception {
    assertThat("Site can be serialized to YAML",
        asYaml(buildSite()),
        is(equalTo(asYaml("site.yml", Site.class))));
  }

  @Test
  public void testDeserializeFromYml() throws Exception {
    assertThat("Site can be deserialized from YAML",
        fromYaml("site.yml", Site.class),
        is(buildSite()));
  }

  @Test
  public void testActiveControlChannelsEqualEmpty() throws Exception {
    final Site site0 = buildSite();
    final Site site1 = buildSite();

    site0.setActiveControlChannel(Optional.<Double>empty());
    site1.setActiveControlChannel(Optional.<Double>empty());

    assert site0.equals(site1);
  }

  @Test
  public void testActiveControlChannelsEqualPresent() throws Exception {
    final Site site0 = buildSite();
    final Site site1 = buildSite();

    site0.setActiveControlChannel(Optional.of(1111d));
    site1.setActiveControlChannel(Optional.of(1111d));

    assert site0.equals(site1);
  }

  @Test
  public void testActiveControlChannelsNotEqual() throws Exception {
    final Site site0 = buildSite();
    final Site site1 = buildSite();
    final Site site2 = buildSite();
    final Site site3 = buildSite();

    site0.setActiveControlChannel(Optional.<Double>empty());
    site1.setActiveControlChannel(Optional.of(2222d));
    assert !site0.equals(site1);

    site2.setActiveControlChannel(Optional.of(3333d));
    site3.setActiveControlChannel(Optional.of(4444d));
    assert !site2.equals(site3);
  }

}
