/*
 * Copyright (C) 2014 Will Hedgecock
 * This file is part of RegTrack: A Relative GPS Tracking Solution
 * 
 * RegTrack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * RegTrack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with RegTrack.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.vu.isis.regtrack.modules.ubxmanager;

import edu.vu.isis.regtrack.common.EphemerisDatum;
import edu.vu.isis.regtrack.common.GpsConstants;
import edu.vu.isis.regtrack.common.GpsTime;

public final class EphDecoder
{
	private final EphemerisDatum[] ephData = new EphemerisDatum[GpsConstants.MAX_PRN+1];
	private final EphemerisDatum[] ephDataTemporary = new EphemerisDatum[GpsConstants.MAX_PRN+1];
	private final byte[] decoded = new byte[40];
	
	public EphDecoder()
	{
		for (int i = 0; i <= GpsConstants.MAX_PRN; ++i)
		{
			ephData[i] = new EphemerisDatum();
			ephDataTemporary[i] = new EphemerisDatum();
		}
	}
	
	private long BitsToUnsigned(final byte[] buff, int offset, int length)
	{
		long bits = 0;

		for (int i = offset; i < (offset + length); ++i)
			bits = (bits << 1) + ((buff[i/8] >> (7 - i%8)) & 0x01);

		return bits;
	}
	
	private long BitsToSigned(final byte[] buff, int offset, int length)
	{
		long bits = (int)BitsToUnsigned(buff, offset, length);

		if ((bits & (0x01 << (length-1))) == 0)
			return bits;

	    return (bits | (0xFFFFFFFFFFFFFFFFl << length));
	}
	
	public EphemerisDatum decode(final byte[] dataPacket)
	{
		double toc;
		int N = 100, iode;
		
		int satID = (dataPacket[5] & 0x000000FF);
		if (satID <= GpsConstants.MAX_GPS)
		{
			for (int i = 0, j = 0; j < 40; j += 4)
			{
				decoded[i++] = dataPacket[j+8];
				decoded[i++] = dataPacket[j+7];
				decoded[i++] = dataPacket[j+6];
			}
		}
		else
		{
			for (int i = 0; i < 28; i += 4)
			{
				decoded[i] = dataPacket[i+9];
				decoded[i+1] = dataPacket[i+8];
				decoded[i+2] = dataPacket[i+7];
				decoded[i+3] = dataPacket[i+6];
			}
			decoded[28] = (byte)((dataPacket[36] & 0x03) << 6);
			satID -= 87;
		}
		if (satID > GpsConstants.MAX_PRN)
			return null;
		ephDataTemporary[satID].PRN = satID;
		
		if (satID <= GpsConstants.MAX_GPS)
		{
			// Decode subframe
			switch ((int)BitsToUnsigned(decoded, 43, 3))
			{
				case 1:
					ephDataTemporary[satID].TOW = (int)BitsToUnsigned(decoded, 24, 17) * 6000;
					ephDataTemporary[satID].week = GpsTime.UTCWeek2GpsWeek((int)BitsToUnsigned(decoded, 48, 10));
					ephDataTemporary[satID].L2Code = (int)BitsToUnsigned(decoded, 58, 2);
					N = (int)BitsToUnsigned(decoded, 60, 4);
					if ((N < 0) || (N >= 15))
						ephDataTemporary[satID].svAccur = -1.0;
					else
						ephDataTemporary[satID].svAccur = GpsConstants.EPH_SVA_VALS[N];
					ephDataTemporary[satID].svHealth = (int)BitsToUnsigned(decoded, 64, 6);
					ephDataTemporary[satID].iodc = (int)((BitsToUnsigned(decoded, 70, 2) << 8) + (int)BitsToUnsigned(decoded, 168, 8));
					ephDataTemporary[satID].tgd = BitsToSigned(decoded, 160, 8) * 0.0000000004656612873077392578125;
					ephDataTemporary[satID].toc = GpsTime.WeekAndMillisecond2GpsTime(ephDataTemporary[satID].week, BitsToUnsigned(decoded, 176, 16) * 16000.0);
					ephDataTemporary[satID].af2 = BitsToSigned(decoded, 192, 8) * 2.7755575615628913510590791702271e-17;
					ephDataTemporary[satID].af1 = BitsToSigned(decoded, 200, 16) * 1.136868377216160297393798828125e-13;
					ephDataTemporary[satID].af0 = BitsToSigned(decoded, 216, 22) * 0.0000000004656612873077392578125;
					ephDataTemporary[satID].setHasFrame(1);
					break;
				case 2:
					if (!ephDataTemporary[satID].hasFrame1)
						break;
					
					ephDataTemporary[satID].iode = (int)BitsToUnsigned(decoded, 48, 8);
					ephDataTemporary[satID].crs = BitsToSigned(decoded, 56, 16) * 0.03125;
					ephDataTemporary[satID].deltaN = BitsToSigned(decoded, 72, 16) * 3.5715773419608467520447447896004e-13;
					ephDataTemporary[satID].M0 = BitsToSigned(decoded, 88, 32) * 1.4629180792671628296375274658203e-9;
					ephDataTemporary[satID].cuc = BitsToSigned(decoded, 120, 16) * 0.00000000186264514923095703125;
					ephDataTemporary[satID].e = BitsToUnsigned(decoded, 136, 32) * 1.16415321826934814453125e-10;
					ephDataTemporary[satID].cus = BitsToSigned(decoded, 168, 16) * 0.00000000186264514923095703125;
					ephDataTemporary[satID].rootA = BitsToUnsigned(decoded, 184, 32) * 0.0000019073486328125;
					ephDataTemporary[satID].toes = BitsToUnsigned(decoded, 216, 16) * 16000.0;
					ephDataTemporary[satID].fitInt = BitsToUnsigned(decoded, 232, 1);
					ephDataTemporary[satID].A = ephDataTemporary[satID].rootA * ephDataTemporary[satID].rootA;
					ephDataTemporary[satID].setHasFrame(2);
					break;
				case 3:
					if (!ephDataTemporary[satID].hasFrame2)
						break;
					
					ephDataTemporary[satID].cic = BitsToSigned(decoded, 48, 16) * 0.00000000186264514923095703125;
					ephDataTemporary[satID].omega0 = BitsToSigned(decoded, 64, 32) * 1.4629180792671628296375274658203e-9;
					ephDataTemporary[satID].cis = BitsToSigned(decoded, 96, 16) * 0.00000000186264514923095703125;
					ephDataTemporary[satID].i0 = BitsToSigned(decoded, 112, 32) * 1.4629180792671628296375274658203e-9;
					ephDataTemporary[satID].crc = BitsToSigned(decoded, 144, 16) * 0.03125;
					ephDataTemporary[satID].omega = BitsToSigned(decoded, 160, 32) * 1.4629180792671628296375274658203e-9;
					ephDataTemporary[satID].omegaDot = BitsToSigned(decoded, 192, 24) * 3.5715773419608467520447447896004e-13;
					iode = (int)BitsToUnsigned(decoded, 216, 8);
					ephDataTemporary[satID].iDot = BitsToSigned(decoded, 224, 14) * 3.5715773419608467520447447896004e-13;
	
					if ((iode != (ephDataTemporary[satID].iodc & 0x000000FF)) || (iode != ephDataTemporary[satID].iode))
					{
						ephDataTemporary[satID].setHasFrame(0);
						break;
					}
	
					toc = GpsTime.GpsTime2MSTimeOfWeek(ephDataTemporary[satID].toc, null);
					if (ephDataTemporary[satID].toes < (ephDataTemporary[satID].TOW - GpsConstants.MILLISEC_IN_HALF_WEEK))
					{
						++ephDataTemporary[satID].week;
						ephDataTemporary[satID].TOW -= 604800000;
						ephDataTemporary[satID].toc = GpsTime.WeekAndMillisecond2GpsTime(ephDataTemporary[satID].week, toc);
					}
					else if (ephDataTemporary[satID].toes > (ephDataTemporary[satID].TOW + GpsConstants.MILLISEC_IN_HALF_WEEK))
					{
						--ephDataTemporary[satID].week;
						ephDataTemporary[satID].TOW += 604800000;
						ephDataTemporary[satID].toc = GpsTime.WeekAndMillisecond2GpsTime(ephDataTemporary[satID].week, toc);
					}
					ephDataTemporary[satID].toe = GpsTime.WeekAndMillisecond2GpsTime(ephDataTemporary[satID].week, ephDataTemporary[satID].toes);
					ephDataTemporary[satID].setHasFrame(3);
					break;
				default:
					break;
			}
		}
		else if ((satID <= GpsConstants.MAX_PRN) && (BitsToUnsigned(decoded, 8, 6) == 9))		// Preamble:  01010011 10011010 11000110
		{
			ephDataTemporary[satID].isSBAS = true;
			ephDataTemporary[satID].iode = (int)BitsToUnsigned(decoded, 14, 8);
			ephDataTemporary[satID].toes = BitsToUnsigned(decoded, 22, 13) * 16000.0;
			N = (int)BitsToUnsigned(decoded, 35, 4);
			if ((N < 0) || (N >= 15))
				ephDataTemporary[satID].svAccur = -1.0;
			else
				ephDataTemporary[satID].svAccur = GpsConstants.EPH_SVA_VALS[N];
			ephDataTemporary[satID].omega = BitsToSigned(decoded, 39, 30) * 0.08;
			ephDataTemporary[satID].omega0 = BitsToSigned(decoded, 69, 30) * 0.08;
			ephDataTemporary[satID].omegaDot = BitsToSigned(decoded, 99, 25) * 0.4;
			ephDataTemporary[satID].velX = BitsToSigned(decoded, 124, 17) * 0.000625;
			ephDataTemporary[satID].velY = BitsToSigned(decoded, 141, 17) * 0.000625;
			ephDataTemporary[satID].velZ = BitsToSigned(decoded, 158, 18) * 0.004;
			ephDataTemporary[satID].accelX = BitsToSigned(decoded, 176, 10) * 0.0000125;
			ephDataTemporary[satID].accelY = BitsToSigned(decoded, 186, 10) * 0.0000125;
			ephDataTemporary[satID].accelZ = BitsToSigned(decoded, 196, 10) * 0.0000625;
			ephDataTemporary[satID].af0 = BitsToSigned(decoded, 206, 12) * 0.0000000004656612873077392578125;
			ephDataTemporary[satID].af1 = BitsToSigned(decoded, 218, 8) * 9.094947017729282379150390625e-13;
			ephDataTemporary[satID].isValid = true;
		}
		else
			return null;
		
		if (ephDataTemporary[satID].isValid && (ephDataTemporary[satID].iode != ephData[satID].iode))
		{
			EphemerisDatum tempData = ephData[satID];
			ephData[satID] = ephDataTemporary[satID];
			ephDataTemporary[satID] = tempData;
			
			return ephData[satID];
		}
		
		return null;
	}
}
