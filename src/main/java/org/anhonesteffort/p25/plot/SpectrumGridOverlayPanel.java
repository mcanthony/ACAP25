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

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.Format;

public class SpectrumGridOverlayPanel extends JPanel {

  private static final Format         LABEL_FORMAT    = new DecimalFormat("0.0000");
  private static final int            GRID_LINES      = 5;
  private static final Color          GRID_COLOR      = Color.WHITE;
  private static final RenderingHints RENDERING_HINTS = new RenderingHints(null);

  static {
    RENDERING_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    RENDERING_HINTS.put(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);
  }

  private Long   sampleRate      = -1l;
  private Double centerFrequency = -1d;

  public SpectrumGridOverlayPanel() {
    setOpaque(false);
  }

  private double getXCoordinateForFrequency(double frequency, Dimension size) {
    ChannelSpec tunedChannel   = new ChannelSpec(centerFrequency, sampleRate, sampleRate);
    double      relativeFreqHz = frequency - tunedChannel.getMinFreq();
    double      dimenHzRatio   = size.getWidth() / tunedChannel.getBandwidth();

    return relativeFreqHz * dimenHzRatio;
  }

  private void handlePaintFrequencyGrid(Graphics2D graphics, Dimension size) {
    ChannelSpec tunedChannel   = new ChannelSpec(centerFrequency, sampleRate, sampleRate);
    double      gridIntervalHz = tunedChannel.getBandwidth() / (GRID_LINES + 1);

    for (int linesDrawn = 0; linesDrawn < GRID_LINES; linesDrawn++) {
      graphics.setColor(GRID_COLOR);
      double drawFreq        = tunedChannel.getMinFreq() + (gridIntervalHz * (linesDrawn + 1));
      double freqXCoordinate = getXCoordinateForFrequency(drawFreq, size);

      String      freqLabel       = LABEL_FORMAT.format(drawFreq / 1_000_000d);
      Rectangle2D freqLabelBounds = graphics.getFontMetrics().getStringBounds(freqLabel, graphics);
      float       labelXStart     = (float) freqXCoordinate - ((float) freqLabelBounds.getWidth() / 2f);
      float       labelYStart     = (float) getSize().getHeight() - 2.0f;

      graphics.draw(new Line2D.Double(
          freqXCoordinate, 0d, freqXCoordinate, (size.getHeight() - freqLabelBounds.getHeight())
      ));

      graphics.drawString(freqLabel, labelXStart, labelYStart);
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D graphics = (Graphics2D) g;
    Dimension  size     = getSize();

    graphics.setRenderingHints(RENDERING_HINTS);
    handlePaintFrequencyGrid(graphics, size);
  }

  protected void onSourceStateChange(Long sampleRate, Double frequency) {
    this.sampleRate      = sampleRate;
    this.centerFrequency = frequency;
    repaint();
  }

}
