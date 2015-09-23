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
import org.anhonesteffort.p25.dft.DftFrame;
import org.anhonesteffort.p25.dft.DftToDecibelConverter;
import org.anhonesteffort.p25.dft.FftWidth;
import org.anhonesteffort.p25.dft.SamplesToDftConverter;
import org.anhonesteffort.p25.sample.Samples;
import org.anhonesteffort.p25.util.CircularFloatAveragingQueue;

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
import java.awt.geom.GeneralPath;
import java.util.Optional;

public class SpectrumPanel extends JPanel implements Sink<Samples>, ActionListener, WindowListener {

  private static final FftWidth       FFT_WIDTH        = FftWidth.FFT_4096;
  private static final int            FFT_AVERAGING    = 30;
  private static final int            FRAME_RATE       = 15;
  private static final Color          BACKGROUND_COLOR = Color.BLACK;
  private static final Color          SPECTRUM_COLOR   = Color.RED;
  private static final RenderingHints RENDERING_HINTS  = new RenderingHints(null);

  static {
    RENDERING_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    RENDERING_HINTS.put(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);
  }

  private final SamplesToDftConverter dftConverter;
  private final DftToDecibelConverter decibelConverter;
  private final Sink<DftFrame>        decibelSink;
  private final Timer                 timer;

  private final CircularFloatAveragingQueue dftQueue  = new CircularFloatAveragingQueue(FFT_AVERAGING);
  private final Object                      queueLock = new Object();
  private int dftWidth  = -1;

  public SpectrumPanel() {
    dftConverter     = new SamplesToDftConverter(FFT_WIDTH);
    decibelConverter = new DftToDecibelConverter();
    decibelSink      = new DecibelSink();
    timer            = new Timer(1000 / FRAME_RATE, this);

    dftConverter.addSink(decibelConverter);
    decibelConverter.addSink(decibelSink);
    timer.start();
  }

  @Override
  public void consume(Samples element) {
    dftConverter.consume(element);
  }

  private class DecibelSink implements Sink<DftFrame> {

    @Override
    public void consume(DftFrame nextFrame) {
      synchronized (queueLock) {
        if (dftWidth != nextFrame.getFrame().length) {
          dftQueue.clear();
          dftWidth = nextFrame.getFrame().length;
        }

        dftQueue.add(nextFrame.getFrame());
      }
    }

  }

  private void handlePaintBackground(Graphics2D graphics, Dimension size) {
    Rectangle background = new Rectangle(0, 0, size.width, size.height);
    graphics.setColor(BACKGROUND_COLOR);
    graphics.draw(background);
    graphics.fill(background);
  }

  private void handlePaintSpectrum(Graphics2D graphics, Dimension size, float[] currentDft) {
    float       scalar       = 3.5f;
    float       binWidth     = (float) size.getWidth() / currentDft.length;
    GeneralPath spectrumPath = new GeneralPath();

    spectrumPath.moveTo(size.getWidth(), size.getHeight());
    spectrumPath.lineTo(0, size.getHeight());

    for(int binIndex = 0; binIndex < currentDft.length; binIndex++) {
      float height;

      if(currentDft[binIndex] >= 0f) {
        height = 0;
      } else {
        height = Math.abs(currentDft[binIndex] * scalar);
        if(height > size.height)
          height = size.height;
      }

      spectrumPath.lineTo((binIndex * binWidth), height);
    }

    spectrumPath.lineTo(size.getWidth(), size.getHeight());

    graphics.setPaint(SPECTRUM_COLOR);
    graphics.draw(spectrumPath);
    graphics.fill(spectrumPath);
  }

  private Optional<float[]> getCurrentDft() {
    synchronized (queueLock) {
      if (!dftQueue.isEmpty())
        return Optional.of(dftQueue.remove());
    }

    return Optional.empty();
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D graphics = (Graphics2D) g;
    Dimension  size     = getSize();

    graphics.setRenderingHints(RENDERING_HINTS);
    handlePaintBackground(graphics, size);

    Optional<float[]> currentDft = getCurrentDft();
    if (currentDft.isPresent())
      handlePaintSpectrum(graphics, size, currentDft.get());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    synchronized (queueLock) {
      if (!dftQueue.isEmpty())
        repaint();
    }
  }

  @Override
  public void windowClosing(WindowEvent e) {
    timer.stop();
    dftConverter.removeSink(decibelConverter);
    decibelConverter.removeSink(decibelSink);
    dftQueue.clear();
  }

  @Override
  public void windowOpened(WindowEvent e) { }

  @Override
  public void windowClosed(WindowEvent e) { }

  @Override
  public void windowIconified(WindowEvent e) { }

  @Override
  public void windowDeiconified(WindowEvent e) { }

  @Override
  public void windowActivated(WindowEvent e) { }

  @Override
  public void windowDeactivated(WindowEvent e) { }

}
