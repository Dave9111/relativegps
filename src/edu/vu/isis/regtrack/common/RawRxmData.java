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

public final class RawRxmData
{
	public int PRN = 0;
	public double pseudorange = Double.NaN;
	public double carrierPhase = Double.NaN;
	public float dopplerShift = 0.0f;
	public int signalStrength = -1;
	public int quality = -1;
	public int lossOfLock = 0;
	
	public RawRxmData() {}
	
	public RawRxmData(final RawRxmData other)
	{
		PRN = other.PRN;
		pseudorange = other.pseudorange;
		carrierPhase = other.carrierPhase;
		dopplerShift = other.dopplerShift;
		signalStrength = other.signalStrength;
		quality = other.quality;
		lossOfLock = other.lossOfLock;
	}
}
