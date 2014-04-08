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

public abstract class RawUbxDataType extends RLPassableDataType
{
	public static enum DataType {RAW_DATA, RAW_CLOCK, RAW_NAV, EPH_DATA, RESULT_DATA};
	
	public final DataType dataType;
	public long receiveEpoch;
	
	public RawUbxDataType(final RLMessageType messageType, final DataType thisDataType, long thisReceiveEpoch)
	{
		super(messageType);
		
		dataType = thisDataType;
		receiveEpoch = thisReceiveEpoch;
	}
	
	public RawUbxDataType(final RawUbxDataType other)
	{
		super(other);
		
		dataType = other.dataType;
		receiveEpoch = other.receiveEpoch;
	}
}
