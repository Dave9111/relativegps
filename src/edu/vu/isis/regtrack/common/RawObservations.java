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

import java.util.ArrayList;

import edu.vu.isis.messaging.RLMessageType;

public final class RawObservations extends RawUbxDataType
{
	public final GpsTime receiveTime;
	public final ArrayList<RawRxmData> observations = new ArrayList<RawRxmData>(20);
	
	public RawObservations(final GpsTime rcvTime, long receiveEpoch)
	{
		super(RLMessageType.RAW_RXM_DATA, DataType.RAW_DATA, receiveEpoch);
		receiveTime = new GpsTime(rcvTime);
	}
	
	public RawObservations(final RawObservations other)
	{
		super(RLMessageType.RAW_RXM_DATA, DataType.RAW_DATA, other.receiveEpoch);
		
		receiveTime = new GpsTime(other.receiveTime);
		for (int i = 0; i < other.observations.size(); ++i)
			observations.add(new RawRxmData(other.observations.get(i)));
	}
}
