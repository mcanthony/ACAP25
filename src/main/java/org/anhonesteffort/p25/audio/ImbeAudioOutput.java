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

package org.anhonesteffort.p25.audio;

import jmbe.iface.AudioConversionLibrary;
import jmbe.iface.AudioConverter;
import org.anhonesteffort.p25.Sink;
import org.anhonesteffort.p25.protocol.frame.DataUnit;
import org.anhonesteffort.p25.protocol.frame.Duid;
import org.anhonesteffort.p25.protocol.frame.LogicalLinkDataUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class ImbeAudioOutput implements Sink<DataUnit> {

  private final static Logger log = LoggerFactory.getLogger(ImbeAudioOutput.class);

  private static final String JMBE_LIBRARY_CLASS = "jmbe.JMBEAudioLibrary";
  private static final String JMBE_CODEC         = "IMBE";

  private static final int SAMPLE_RATE       = 48000;
  private static final int SAMPLE_BIT_LENGTH = 16;
  private static final int FRAME_RATE        = SAMPLE_RATE;
  private static final int FRAME_BYTE_LENGTH = SAMPLE_BIT_LENGTH / 8;
  private static final int CHANNEL_COUNT     = 1;

  private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
      AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, SAMPLE_BIT_LENGTH,
      CHANNEL_COUNT, FRAME_BYTE_LENGTH, FRAME_RATE, false
  );

  private final AudioConverter audioConverter;
  private final SourceDataLine output;

  public ImbeAudioOutput() throws ReflectiveOperationException, LineUnavailableException {
    Class                  libraryClass = Class.forName(JMBE_LIBRARY_CLASS);
    AudioConversionLibrary library      = (AudioConversionLibrary) libraryClass.newInstance();

    audioConverter = library.getAudioConverter(JMBE_CODEC, AUDIO_FORMAT);
    if (audioConverter == null)
      throw new ClassNotFoundException("unable to instantiate jmbe audio converter");

    output = AudioSystem.getSourceDataLine(AUDIO_FORMAT);
    output.open(AUDIO_FORMAT, FRAME_RATE * FRAME_BYTE_LENGTH);
    output.start();
  }

  @Override
  public void consume(DataUnit element) {
    switch (element.getNid().getDuid().getId()) {
      case Duid.ID_LLDU1:
      case Duid.ID_LLDU2:
        LogicalLinkDataUnit lldu = (LogicalLinkDataUnit) element;
        if (!lldu.isIntact()) {
          log.warn("skipping lldu audio playback, frames are corrupted");
          return;
        }

        for (byte[] codeWord : lldu.getVoiceCodeWords()) {
          byte[] audio = audioConverter.convert(codeWord);
          output.write(audio, 0, audio.length);
        }
    }
  }

  public void stop() {
    output.drain();
    output.stop();
    output.close();
  }

}
