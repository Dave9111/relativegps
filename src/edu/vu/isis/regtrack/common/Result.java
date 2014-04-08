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

public final class Result extends RLPassableDataType
{
	public final String ID;
	public final long gpsEpoch;
	public final Coordinate relativeResult;
	public final double accuracy;
	
	public Result()
	{
		super(RLMessageType.RESULT_DATA);
		
		ID = "null";
		gpsEpoch = 0l;
		relativeResult = new Coordinate();
		accuracy = Double.NaN;
	}
	
	public Result(final String receiverID, final Coordinate result, long epoch, double confidence)
	{
		super(RLMessageType.RESULT_DATA);
		
		ID = receiverID;
		gpsEpoch = epoch;
		relativeResult = new Coordinate(result);
		accuracy = confidence;
	}
	
	public Result(final Result other)
	{
		super(RLMessageType.RESULT_DATA);
		
		ID = other.ID;
		gpsEpoch = other.gpsEpoch;
		relativeResult = new Coordinate(other.relativeResult);
		accuracy = other.accuracy;
	}
}
