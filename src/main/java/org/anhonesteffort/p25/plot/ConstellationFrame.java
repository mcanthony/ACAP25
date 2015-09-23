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

package org.anhonesteffort.p25.plot;

import org.anhonesteffort.p25.primitive.ComplexNumber;
import org.anhonesteffort.p25.sample.DynamicSink;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class ConstellationFrame extends JFrame implements DynamicSink<ComplexNumber>, ComponentListener {

  private final ConstellationPlotPanel constellationPanel;

  public ConstellationFrame() {
    super("DFT Plot");

    setLayout(new BorderLayout());
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    constellationPanel = new ConstellationPlotPanel();

    getLayeredPane().add(constellationPanel, 0, 0);
    getLayeredPane().addComponentListener(this);
    addWindowListener(constellationPanel);
  }

  @Override
  public void consume(ComplexNumber element) {
    constellationPanel.consume(element);
  }

  @Override
  public void componentResized(ComponentEvent e) {
    constellationPanel.setBounds(0, 0, e.getComponent().getWidth(), e.getComponent().getHeight());
  }

  @Override
  public void onSourceStateChange(Long sampleRate, Double frequency) { }

  @Override
  public void componentMoved(ComponentEvent e) { }

  @Override
  public void componentShown(ComponentEvent e) { }

  @Override
  public void componentHidden(ComponentEvent e) { }
}
