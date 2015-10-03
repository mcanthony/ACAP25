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

package org.anhonesteffort.p25.protocol;

public class P25 {

  public static final Long   SAMPLE_RATE    = 48_000l;
  public static final Long   SYMBOL_RATE    =  4_800l;
  public static final Double CHANNEL_WIDTH  = 12_500d;
  public static final Long   PASSBAND_STOP  =   6250l;
  public static final Long   STOPBAND_START =   7500l;

  public static final long SYNC0DEG        = 0x5575F5FF77FFl;
  public static final long SYNC90DEG       = 0x001050551155l;
  public static final long SYNC180DEG      = 0xAA8A0A008800l;
  public static final long SYNC270DEG      = 0xFFEFAFAAEEAAl;
  public static final int  SYNC_BIT_LENGTH = 48;

  public static final int NID_LENGTH = 64;

  public static final int LCO_GROUP_VOICE_USER  = 0X00;
  public static final int LCO_UNIT_TO_UNIT_USER = 0x03;

  public static final int TSBK_GROUP_VOICE_CHAN_GRANT = 0x00;
  public static final int TSBK_ID_UPDATE_VUHF         = 0x34;
  public static final int TSBK_NETWORK_STATUS         = 0x3B;
  public static final int TSBK_ID_UPDATE_NO_VUHF      = 0x3D;

}
