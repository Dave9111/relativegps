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

public final class RawClockData extends RawUbxDataType
{
	public double clockBias = 0.0;
	public double clockDrift = 0.0;
	public double timeAccuracy = 0.0;
	public double freqAccuracy = 0.0;
	
	public RawClockData(long receiveEpoch)
	{
		super(RLMessageType.RAW_CLOCK_DATA, DataType.RAW_CLOCK, receiveEpoch);
	}
	
	public RawClockData(final RawClockData other)
	{
		super(RLMessageType.RAW_CLOCK_DATA, DataType.RAW_CLOCK, other.receiveEpoch);
		
		clockBias = other.clockBias;
		clockDrift = other.clockDrift;
		timeAccuracy = other.timeAccuracy;
		freqAccuracy = other.freqAccuracy;
	}
}
