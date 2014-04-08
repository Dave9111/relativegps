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

package edu.vu.isis.regtrack.common;

import edu.vu.isis.messaging.RLMessageType;

public final class RawNavData extends RawUbxDataType
{
	public final GpsTime receiveTime;
	public int gpsFix = 0;
	public double X = 0.0, Y = 0.0, Z = 0.0, posAccuracy = 0.0;
	public double velX = 0.0, velY = 0.0, velZ = 0.0, speedAccuracy = 0.0, PDOP = 0.0;
	
	public RawNavData(final GpsTime rcvTime, long receiveEpoch)
	{
		super(RLMessageType.RAW_NAV_DATA, DataType.RAW_NAV, receiveEpoch);
		
		receiveTime = new GpsTime(rcvTime);
	}
	
	public RawNavData(final RawNavData other)
	{
		super(RLMessageType.RAW_NAV_DATA, DataType.RAW_NAV, other.receiveEpoch);
		
		receiveTime = new GpsTime(other.receiveTime);
		gpsFix = other.gpsFix;
		X = other.X;
		Y = other.Y;
		Z = other.Z;
		posAccuracy = other.posAccuracy;
		velX = other.velX;
		velY = other.velY;
		velZ = other.velZ;
		speedAccuracy = other.speedAccuracy;
		PDOP = other.PDOP;
	}
}
