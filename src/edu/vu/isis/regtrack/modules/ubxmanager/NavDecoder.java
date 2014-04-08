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

import edu.vu.isis.regtrack.common.GpsTime;
import edu.vu.isis.regtrack.common.RawClockData;
import edu.vu.isis.regtrack.common.RawNavData;

public final class NavDecoder
{
	public NavDecoder() {}
	
	public RawClockData decodeClock(final byte[] dataPacket)
	{
		// Length should always be 20 bytes
		if ((((dataPacket[3] & 0x000000FF) << 8) | (dataPacket[2] & 0x000000FF)) != 20)
			return null;
		
		long iTOW = ((dataPacket[7] & 0x000000FF) << 24) | ((dataPacket[6] & 0x000000FF) << 16) |
				((dataPacket[5] & 0x000000FF) << 8) | (dataPacket[4] & 0x000000FF);
        RawClockData clockStruct = new RawClockData(Math.round(iTOW*0.001));
        clockStruct.clockBias = (((dataPacket[11] & 0x000000FF) << 24) | ((dataPacket[10] & 0x000000FF) << 16) |
				((dataPacket[9] & 0x000000FF) << 8) | (dataPacket[8] & 0x000000FF))  * 1.0e-9;
        clockStruct.clockDrift = (((dataPacket[15] & 0x000000FF) << 24) | ((dataPacket[14] & 0x000000FF) << 16) |
				((dataPacket[13] & 0x000000FF) << 8) | (dataPacket[12] & 0x000000FF)) * 1.0e-9;;
        clockStruct.timeAccuracy = (((dataPacket[19] & 0x000000FF) << 24) | ((dataPacket[18] & 0x000000FF) << 16) |
				((dataPacket[17] & 0x000000FF) << 8) | (dataPacket[16] & 0x000000FF)) * 1.0e-9;;
        clockStruct.freqAccuracy = (((dataPacket[23] & 0x000000FF) << 24) | ((dataPacket[22] & 0x000000FF) << 16) |
				((dataPacket[21] & 0x000000FF) << 8) | (dataPacket[20] & 0x000000FF)) * 1.0e-12;;
        
		// Verify checksum
		if (!UbxDecoder.checksum(dataPacket, 24, dataPacket[24], dataPacket[25]))
			return null;
		
		// Checksum correct, return new RawClock structure
		return clockStruct;
	}
	
	public RawNavData decodeSolution(final byte[] dataPacket)
	{
		// Length should always be 52 bytes
		if ((((dataPacket[3] & 0x000000FF) << 8) | (dataPacket[2] & 0x000000FF)) != 52)
			return null;
			
		long iTOW = ((dataPacket[7] & 0x000000FF) << 24) | ((dataPacket[6] & 0x000000FF) << 16) |
				((dataPacket[5] & 0x000000FF) << 8) | (dataPacket[4] & 0x000000FF);
		int fracTOW = ((dataPacket[11] & 0x000000FF) << 24) | ((dataPacket[10] & 0x000000FF) << 16) |
				((dataPacket[9] & 0x000000FF) << 8) | (dataPacket[8] & 0x000000FF);
		int week = ((dataPacket[13] & 0x000000FF) << 8) | (dataPacket[12] & 0x000000FF);
        RawNavData navStruct = new RawNavData(GpsTime.WeekAndMillisecond2GpsTime(week, iTOW + (fracTOW*0.000001)), Math.round(iTOW*0.001));
        navStruct.gpsFix = (dataPacket[14] & 0x000000FF);
        navStruct.X = (((dataPacket[19] & 0x000000FF) << 24) | ((dataPacket[18] & 0x000000FF) << 16) |
				((dataPacket[17] & 0x000000FF) << 8) | (dataPacket[16] & 0x000000FF)) * 0.01;
        navStruct.Y = (((dataPacket[23] & 0x000000FF) << 24) | ((dataPacket[22] & 0x000000FF) << 16) |
				((dataPacket[21] & 0x000000FF) << 8) | (dataPacket[20] & 0x000000FF)) * 0.01;
        navStruct.Z = (((dataPacket[27] & 0x000000FF) << 24) | ((dataPacket[26] & 0x000000FF) << 16) |
				((dataPacket[25] & 0x000000FF) << 8) | (dataPacket[24] & 0x000000FF)) * 0.01;
        navStruct.posAccuracy = (((dataPacket[31] & 0x000000FF) << 24) | ((dataPacket[30] & 0x000000FF) << 16) |
				((dataPacket[29] & 0x000000FF) << 8) | (dataPacket[28] & 0x000000FF)) * 0.01;
        navStruct.velX = (((dataPacket[35] & 0x000000FF) << 24) | ((dataPacket[34] & 0x000000FF) << 16) |
				((dataPacket[33] & 0x000000FF) << 8) | (dataPacket[32] & 0x000000FF)) * 0.01;
        navStruct.velY = (((dataPacket[39] & 0x000000FF) << 24) | ((dataPacket[38] & 0x000000FF) << 16) |
				((dataPacket[37] & 0x000000FF) << 8) | (dataPacket[36] & 0x000000FF)) * 0.01;
        navStruct.velZ = (((dataPacket[43] & 0x000000FF) << 24) | ((dataPacket[42] & 0x000000FF) << 16) |
				((dataPacket[41] & 0x000000FF) << 8) | (dataPacket[40] & 0x000000FF)) * 0.01;
        navStruct.speedAccuracy = (((dataPacket[47] & 0x000000FF) << 24) | ((dataPacket[46] & 0x000000FF) << 16) |
				((dataPacket[45] & 0x000000FF) << 8) | (dataPacket[44] & 0x000000FF)) * 0.01;
        navStruct.PDOP = (((dataPacket[49] & 0x000000FF) << 8) | (dataPacket[48] & 0x000000FF)) * 0.01;
        
		// Verify checksum
		if (!UbxDecoder.checksum(dataPacket, 56, dataPacket[56], dataPacket[57]))
			return null;
		
		// Checksum correct, return new RawNavData structure
		return navStruct;
	}
}
