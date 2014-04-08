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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public final class SatelliteObservations
{
	private final HashMap<Integer, SatelliteObservation> collection = new HashMap<Integer, SatelliteObservation>();
	
	public static final class SatelliteObservation
	{
		public final EphemerisDatum ephemerisDatum;
		public double pseudorange, carrierRange, dopplerShift, signalStrength;
		public boolean potentialHalfCycleSlip, cycleSlips;
		
		public SatelliteObservation(final EphemerisDatum ephDatum, double PR, double CR, double doppler, double SNR, boolean halfCycleSlip)
		{
			ephemerisDatum = ephDatum;
			pseudorange = PR;
			carrierRange = CR;
			dopplerShift = doppler;
			signalStrength = SNR;
			potentialHalfCycleSlip = halfCycleSlip;
			cycleSlips = false;
		}
		
		public SatelliteObservation(final SatelliteObservation other)
		{
			ephemerisDatum = new EphemerisDatum(other.ephemerisDatum);
			pseudorange = other.pseudorange;
			carrierRange = other.carrierRange;
			dopplerShift = other.dopplerShift;
			signalStrength = other.signalStrength;
			potentialHalfCycleSlip = other.potentialHalfCycleSlip;
			cycleSlips = other.cycleSlips;
		}
	};

	public SatelliteObservations() {}
	public SatelliteObservations(final SatelliteObservations other)
	{
		for (final Entry<Integer, SatelliteObservation> entry : other.collection.entrySet())
			collection.put(entry.getKey(), new SatelliteObservation(entry.getValue()));
	}
	
	public void addData(int PRN, final SatelliteObservation member) { collection.put(PRN, member); }
	public Set<Integer> getValidSatelliteNumbers() { return collection.keySet(); }
	public Set<Entry<Integer, SatelliteObservation>> getFullSatelliteCollection() { return collection.entrySet(); }
	public Collection<SatelliteObservation> getSatelliteObservations() { return collection.values(); }
	public SatelliteObservation getSatelliteData(int PRN) { return collection.get(PRN); }
	public void removeSatelliteData(int PRN) { collection.remove(PRN); }
}
