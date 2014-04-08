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

import edu.vu.isis.regtrack.common.GpsConstants;
import edu.vu.isis.regtrack.common.GpsTime;
import edu.vu.isis.regtrack.common.RawObservations;
import edu.vu.isis.regtrack.common.RawRxmData;

public final class RxmDecoder
{
	private RawObservations rawObservations;
	
	public RxmDecoder() {}
	
	public RawObservations decode(final byte[] dataPacket)
	{
		RawRxmData rawData;
		int length = ((dataPacket[3] & 0x000000FF) << 8) | (dataPacket[2] & 0x000000FF);
        int tow = ((dataPacket[7] & 0x000000FF) << 24) | ((dataPacket[6] & 0x000000FF) << 16) |
				((dataPacket[5] & 0x000000FF) << 8) | (dataPacket[4] & 0x000000FF);
        int week = ((dataPacket[9] & 0x000000FF) << 8) | (dataPacket[8] & 0x000000FF);
		int numSatellites = (dataPacket[10] & 0x000000FF);
        
        rawObservations = new RawObservations(GpsTime.WeekAndMillisecond2GpsTime(week, tow), Math.round(tow*0.001));
        for (int k = 12; k < length; k += 24)
        {
        	rawData = new RawRxmData();
        	
        	rawData.carrierPhase = Double.longBitsToDouble(((dataPacket[k+7] & 0x000000FFl) << 56l) | ((dataPacket[k+6] & 0x000000FFl) << 48l) |
    				((dataPacket[k+5] & 0x000000FFl) << 40l) | ((dataPacket[k+4] & 0x000000FFl) << 32l) |
    				((dataPacket[k+3] & 0x000000FFl) << 24l) | ((dataPacket[k+2] & 0x000000FFl) << 16l) |
    				((dataPacket[k+1] & 0x000000FFl) << 8l) | (dataPacket[k] & 0x000000FFl));
        	rawData.pseudorange = Double.longBitsToDouble(((dataPacket[k+15] & 0x000000FFl) << 56l) | ((dataPacket[k+14] & 0x000000FFl) << 48l) |
    				((dataPacket[k+13] & 0x000000FFl) << 40l) | ((dataPacket[k+12] & 0x000000FFl) << 32l) |
    				((dataPacket[k+11] & 0x000000FFl) << 24l) | ((dataPacket[k+10] & 0x000000FFl) << 16l) |
    				((dataPacket[k+9] & 0x000000FFl) << 8l) | (dataPacket[k+8] & 0x000000FFl));
        	rawData.dopplerShift = Float.intBitsToFloat(((dataPacket[k+19] & 0x000000FF) << 24) | ((dataPacket[k+18] & 0x000000FF) << 16) |
    				((dataPacket[k+17] & 0x000000FF) << 8) | (dataPacket[k+16] & 0x000000FF));
        	rawData.PRN = (dataPacket[k+20] & 0x000000FF);
			if (rawData.PRN > GpsConstants.MAX_GPS)
				rawData.PRN -= 87;
        	rawData.quality = (dataPacket[k+21] & 0x000000FF);
        	rawData.signalStrength = (dataPacket[k+22] & 0x000000FF);
        	rawData.lossOfLock = (dataPacket[k+23] & 0x000000FF);

        	rawObservations.observations.add(rawData);
        }
		
		// Verify checksum
		if (!UbxDecoder.checksum(dataPacket, length+4, dataPacket[numSatellites*24 + 12], dataPacket[numSatellites*24 + 13]))
			return null;
		
		// Checksum correct, return new RawNavData structure
		return rawObservations;
	}
}
