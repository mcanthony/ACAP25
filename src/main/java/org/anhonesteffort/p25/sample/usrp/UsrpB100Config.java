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

package org.anhonesteffort.p25.sample.usrp;

public class UsrpB100Config {

  // todo: read from command line or file

  protected static final Double MIN_FREQUENCY   =  400000000d;
  protected static final Double MAX_FREQUENCY   = 1000000000d;
  protected static final Long   MAX_SAMPLE_RATE =    8000000l;

  protected static final String DEFAULT_CLOCK_SOURCE    = "internal";
  protected static final long   DEFAULT_RX_RATE         =   1280000l;
  protected static final double DEFAULT_RX_GAIN_ADC_PGA =        0.0;
  protected static final double DEFAULT_RX_GAIN_PGA0    =       16.0;
  protected static final String DEFAULT_RX_ANTENNA      =    "TX/RX";
  protected static final long   DEFAULT_RX_BUFFER_SIZE  =     75000l;

  private String clockSource  = DEFAULT_CLOCK_SOURCE;
  private long   rxSampleRate = DEFAULT_RX_RATE;
  private double frequency    = MIN_FREQUENCY;
  private double rxGainAdcPga = DEFAULT_RX_GAIN_ADC_PGA;
  private double rxGainPga0   = DEFAULT_RX_GAIN_PGA0;
  private String rxAntenna    = DEFAULT_RX_ANTENNA;
  private long   rxBufferSize = DEFAULT_RX_BUFFER_SIZE;

  public String getClockSource() {
    return clockSource;
  }

  public void setClockSource(String clockSource) {
    this.clockSource = clockSource;
  }

  public double getFrequency() {
    return frequency;
  }

  public void setFrequency(double frequency) {
    this.frequency = frequency;
  }

  public long getRxSampleRate() {
    return rxSampleRate;
  }

  public void setRxSampleRate(long rxSampleRate) {
    this.rxSampleRate = rxSampleRate;
  }

  public double getRxGainAdcPga() {
    return rxGainAdcPga;
  }

  public void setRxGainAdcPga(double rxGainAdcPga) {
    this.rxGainAdcPga = rxGainAdcPga;
  }

  public double getRxGainPga0() {
    return rxGainPga0;
  }

  public void setRxGainPga0(double rxGainPga0) {
    this.rxGainPga0 = rxGainPga0;
  }

  public String getRxAntenna() {
    return rxAntenna;
  }

  public void setRxAntenna(String rxAntenna) {
    this.rxAntenna = rxAntenna;
  }

  public long getRxBufferSize() {
    return rxBufferSize;
  }

  public void setRxBufferSize(long rxBufferSize) {
    this.rxBufferSize = rxBufferSize;
  }

}
