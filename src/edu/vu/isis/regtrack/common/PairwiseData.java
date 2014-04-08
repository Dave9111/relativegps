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

import java.util.Map.Entry;
import java.util.TreeMap;

import edu.vu.isis.regtrack.common.SatelliteObservations.SatelliteObservation;
import edu.vu.isis.messaging.RLMessageType;
import edu.vu.isis.messaging.RLPassableDataType;

public final class PairwiseData extends RLPassableDataType
{
	public static final class ManipulatedData
	{
		public double singleDifferenceCarrierRange;
		public double doubleDifferenceCarrierRange;
		public double doubleDifferenceTemporalCarrierRange;
		public boolean isTemporallyValid = false, potentialCycleSlips = false;
		public boolean halfCycleAmbiguityResolutionRequired;
		
		public ManipulatedData(boolean potentialHalfCycleSlip, boolean potentialCycleSlip)
		{
			halfCycleAmbiguityResolutionRequired = potentialHalfCycleSlip;
			potentialCycleSlips = potentialCycleSlip;
		}
		
		public ManipulatedData(final ManipulatedData other)
		{
			singleDifferenceCarrierRange = other.singleDifferenceCarrierRange;
			doubleDifferenceCarrierRange = other.doubleDifferenceCarrierRange;
			doubleDifferenceTemporalCarrierRange = other.doubleDifferenceTemporalCarrierRange;
			isTemporallyValid = other.isTemporallyValid;
			potentialCycleSlips = other.potentialCycleSlips;
			halfCycleAmbiguityResolutionRequired = other.halfCycleAmbiguityResolutionRequired;
		}
	};
	
	public final long receiveEpoch;
	public final ProcessedData local, previousLocal;
	public final ProcessedData remote, previousRemote;
	public final boolean hasPreviousData;
	public final Coordinate referencePosition;
	public int referenceSatellite = 0;
	public final TreeMap<Integer, ManipulatedData> manipulatedData = new TreeMap<Integer, ManipulatedData>();
	
	public PairwiseData(long commonTime, final ProcessedData localData, final ProcessedData remoteData, final ProcessedData previousLocalData, final ProcessedData previousRemoteData)
	{
		super(RLMessageType.PAIRWISE_DATA);
		
		receiveEpoch = commonTime;
		referencePosition = new Coordinate(localData.absoluteLocation);
		hasPreviousData = (previousLocalData != null) && (previousRemoteData != null);
		local = new ProcessedData(localData);
		remote = new ProcessedData(remoteData);
		previousLocal = hasPreviousData ? new ProcessedData(previousLocalData) : null;
		previousRemote = hasPreviousData ? new ProcessedData(previousRemoteData) : null;
		
		// Find valid satellites and perform differencing operations
		double highestElevation = -1.0, highestElevationWithHalfCycleSlip = -1.0;
		int referenceSatelliteWithHalfCycleSlip = 0;
		for (Integer PRN : local.observations.getValidSatelliteNumbers())
		{
			SatelliteObservation remoteObs = remote.observations.getSatelliteData(PRN), localObs = local.observations.getSatelliteData(PRN);
			if (remoteObs == null)
				continue;
			
			// Created new manipulated data structure and take single-difference of the carrier ranges
			ManipulatedData newDatum = new ManipulatedData(localObs.potentialHalfCycleSlip || remoteObs.potentialHalfCycleSlip,
					localObs.cycleSlips || remoteObs.cycleSlips);
			manipulatedData.put(PRN, newDatum);
			newDatum.singleDifferenceCarrierRange = remoteObs.carrierRange - localObs.carrierRange;
			
			// Determine the highest satellite with no potential half-cycle slips
			if (!newDatum.halfCycleAmbiguityResolutionRequired && (localObs.ephemerisDatum.elevation > highestElevation))
			{
				highestElevation = localObs.ephemerisDatum.elevation;
				referenceSatellite = PRN;
			}
			if (localObs.ephemerisDatum.elevation > highestElevationWithHalfCycleSlip)
			{
				highestElevationWithHalfCycleSlip = localObs.ephemerisDatum.elevation;
				referenceSatelliteWithHalfCycleSlip = PRN;
			}
			
			// See if data is valid temporally and form temporal double difference
			SatelliteObservation previousRemoteObs = hasPreviousData ? previousRemote.observations.getSatelliteData(PRN) : null;
			SatelliteObservation previousLocalObs = hasPreviousData ? previousLocal.observations.getSatelliteData(PRN) : null;
			if (hasPreviousData && (previousLocalObs != null) && (previousRemoteObs != null) && !newDatum.potentialCycleSlips && 
					(newDatum.halfCycleAmbiguityResolutionRequired == (previousLocalObs.potentialHalfCycleSlip || previousRemoteObs.potentialHalfCycleSlip)))
			{
				newDatum.isTemporallyValid = true;
				newDatum.doubleDifferenceTemporalCarrierRange = newDatum.singleDifferenceCarrierRange - previousRemoteObs.carrierRange + previousLocalObs.carrierRange;
			}
		}
		
		// Perform double differencing operations using the newly identified reference satellite
		if (referenceSatellite == 0)
			referenceSatellite = referenceSatelliteWithHalfCycleSlip;
		ManipulatedData referenceDatum = manipulatedData.get(referenceSatellite);
		for (final ManipulatedData manipulatedDatum : manipulatedData.values())
		{
			manipulatedDatum.doubleDifferenceCarrierRange = manipulatedDatum.singleDifferenceCarrierRange - referenceDatum.singleDifferenceCarrierRange;
			manipulatedDatum.halfCycleAmbiguityResolutionRequired |= referenceDatum.halfCycleAmbiguityResolutionRequired;
		}
	}
	
	public PairwiseData(final PairwiseData other)
	{
		super(RLMessageType.PAIRWISE_DATA);
		
		receiveEpoch = other.receiveEpoch;
		local = new ProcessedData(other.local);
		previousLocal = new ProcessedData(other.previousLocal);
		remote = new ProcessedData(other.remote);
		previousRemote = new ProcessedData(other.previousRemote);
		hasPreviousData = other.hasPreviousData;
		referencePosition = new Coordinate(other.referencePosition);
		referenceSatellite = other.referenceSatellite;
		
		for (final Entry<Integer, ManipulatedData> entry : other.manipulatedData.entrySet())
			manipulatedData.put(entry.getKey(), new ManipulatedData(entry.getValue()));
	}
}
