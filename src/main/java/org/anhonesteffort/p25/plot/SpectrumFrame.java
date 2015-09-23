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

import org.anhonesteffort.p25.primitive.ChannelSpec;
import org.anhonesteffort.p25.sample.Samples;
import org.anhonesteffort.p25.sample.SamplesSink;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Optional;

public class SpectrumFrame extends JFrame implements SamplesSink, ComponentListener {

  private final SpectrumPanel            spectrumPanel;
  private final SpectrumGridOverlayPanel gridPanel;

  public SpectrumFrame() {
    super("DFT Plot");

    setLayout(new BorderLayout());
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    spectrumPanel = new SpectrumPanel();
    gridPanel     = new SpectrumGridOverlayPanel();

    getLayeredPane().add(spectrumPanel, 0, 0);
    getLayeredPane().add(gridPanel,     1, 0);
    getLayeredPane().addComponentListener(this);

    addWindowListener(spectrumPanel);
  }

  @Override
  public void componentResized(ComponentEvent e) {
    int width  = e.getComponent().getWidth();
    int height = e.getComponent().getHeight();

    spectrumPanel.setBounds(0, 0, width, height);
    gridPanel.setBounds(0, 0, width, height);
  }

  @Override
  public Optional<ChannelSpec> getTargetChannel() {
    return Optional.empty();
  }

  @Override
  public void onSourceStateChange(Long sampleRate, Double frequency) {
    gridPanel.onSourceStateChange(sampleRate, frequency);
  }

  @Override
  public void consume(Samples element) {
    spectrumPanel.consume(element);
  }

  @Override
  public void componentMoved(ComponentEvent e) { }

  @Override
  public void componentShown(ComponentEvent e) { }

  @Override
  public void componentHidden(ComponentEvent e) { }
}
