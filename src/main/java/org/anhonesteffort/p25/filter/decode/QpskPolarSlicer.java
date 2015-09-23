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

package org.anhonesteffort.p25.filter.decode;

import org.anhonesteffort.p25.Sink;
import org.anhonesteffort.p25.Source;
import org.anhonesteffort.p25.primitive.ComplexNumber;
import org.anhonesteffort.p25.primitive.DiBit;

/*
       Q
       |
       00
       |
 --01-----10-- I
       |
       11
       |
 */
public class QpskPolarSlicer extends Source<DiBit, Sink<DiBit>> implements Sink<ComplexNumber> {

  @Override
  public void consume(ComplexNumber element) {
    DiBit output;

    if (Math.abs(element.getInPhase()) > Math.abs(element.getQuadrature()))
      output = (element.getInPhase() > 0) ? DiBit.D2 : DiBit.D1;
    else
      output = (element.getQuadrature() > 0) ? DiBit.D0 : DiBit.D3;

    broadcast(output);
  }

}
