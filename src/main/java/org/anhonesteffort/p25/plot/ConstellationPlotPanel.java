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

import org.anhonesteffort.p25.Sink;

import org.anhonesteffort.p25.primitive.ComplexNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConstellationPlotPanel extends JPanel implements Sink<ComplexNumber>, ActionListener, WindowListener {

  private static final Logger log = LoggerFactory.getLogger(ConstellationPlotPanel.class);

  private static final int            FRAME_RATE          = 25;
  private static final Color          BACKGROUND_COLOR    = Color.BLACK;
  private static final Color          CONSTELLATION_COLOR = Color.RED;
  private static final int            POINT_DIAMETER      = 10;
  private static final int            PADDING             = 10;
  private static final RenderingHints RENDERING_HINTS     = new RenderingHints(null);

  static {
    RENDERING_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    RENDERING_HINTS.put(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);
  }

  private final BlockingQueue<ComplexNumber> queue = new LinkedBlockingQueue<>(2000);

  private final Object queueLock = new Object();
  private final Timer  timer     = new Timer(1000 / FRAME_RATE, this);

  public ConstellationPlotPanel() {
    timer.start();
  }

  @Override
  public void consume(ComplexNumber element) {
    synchronized (queueLock) {
      if (!queue.offer(element)) {
        queue.clear();
        log.warn("sample receive queue has overflown");
      }
    }
  }

  private List<ComplexNumber> getCurrentSamples() {
    List<ComplexNumber> samples = new LinkedList<>();
    synchronized (queueLock) {
      if (!queue.isEmpty())
        queue.drainTo(samples);
    }

    return samples;
  }

  private void handlePaintBackground(Graphics2D graphics, Dimension size) {
    Rectangle background = new Rectangle(0, 0, size.width, size.height);
    graphics.setColor(BACKGROUND_COLOR);
    graphics.draw(background);
    graphics.fill(background);
  }

  private int getXCoord(Dimension size, ComplexNumber number) {
    float xCoord = number.getInPhase() + 1.0f;
    float width  = (float)size.width - PADDING;

    return (int) ((width / 2.0f) * xCoord);
  }

  private int getYCoord(Dimension size, ComplexNumber number) {
    float yCoord = number.getQuadrature() + 1.0f;
    float height = (float)size.height - PADDING;

    return (int) (height - ((height / 2.0f) * (yCoord)));
  }

  private void handlePaintConstellation(Graphics2D graphics, Dimension size, List<ComplexNumber> samples) {
    graphics.setPaint(CONSTELLATION_COLOR);
    samples.forEach(sample ->
            graphics.drawOval(getXCoord(size, sample),
                              getYCoord(size, sample),
                              POINT_DIAMETER,
                              POINT_DIAMETER)
    );
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D graphics = (Graphics2D) g;
    Dimension  size     = getSize();

    graphics.setRenderingHints(RENDERING_HINTS);
    handlePaintBackground(graphics, size);

    List<ComplexNumber> samples = getCurrentSamples();
    if (!samples.isEmpty())
      handlePaintConstellation(graphics, size, samples);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!queue.isEmpty())
      repaint();
  }

  @Override
  public void windowClosing(WindowEvent e) {
    timer.stop();
  }

  @Override
  public void windowOpened(WindowEvent e) { }

  @Override
  public void windowClosed(WindowEvent e) {  }

  @Override
  public void windowIconified(WindowEvent e) { }

  @Override
  public void windowDeiconified(WindowEvent e) { }

  @Override
  public void windowActivated(WindowEvent e) { }

  @Override
  public void windowDeactivated(WindowEvent e) { }
}
