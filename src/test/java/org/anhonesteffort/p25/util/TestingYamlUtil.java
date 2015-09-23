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

package org.anhonesteffort.p25.util;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.File;
import java.io.IOException;

public class TestingYamlUtil extends YamlUtil {

  private static final String FILEPATH_PREFIX = "src/test/resources/fixtures/";

  public static <T> T fromYaml(String filename, Class<T> clazz) throws IOException {
    return fromYaml(new File(FILEPATH_PREFIX + filename), clazz);
  }

  public static String asYaml(Object object) throws JsonProcessingException {
    return objectMapper.writeValueAsString(object);
  }

  public static String asYaml(String filename, Class clazz) throws IOException {
    return asYaml(fromYaml(filename, clazz));
  }

}
