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
import edu.vu.isis.messaging.RLPassableDataType;

public final class DecodedData extends RLPassableDataType
{
	public final GpsTime receiveTime;
	public final RawObservations rawObservations;
	public final RawClockData rawClock;
	public final RawNavData rawNav;
	
	public DecodedData(final RawClockData rawClockData, final RawNavData rawNavData, final RawObservations rawObsData)
	{
		super(RLMessageType.DECODED_DATA);
		
		receiveTime = new GpsTime(rawObsData.receiveTime);
		rawClock = new RawClockData(rawClockData);
		rawNav = new RawNavData(rawNavData);
		rawObservations = new RawObservations(rawObsData);
	}
	
	public DecodedData(final DecodedData other)
	{
		super(RLMessageType.DECODED_DATA);
		
		receiveTime = new GpsTime(other.receiveTime);
		rawObservations = new RawObservations(other.rawObservations);
		rawClock = new RawClockData(other.rawClock);
		rawNav = new RawNavData(other.rawNav);
	}
}
