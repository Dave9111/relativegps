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

package edu.vu.isis.regtrack.modules.preprocessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.vu.isis.regtrack.common.Coordinate;
import edu.vu.isis.regtrack.common.DecodedData;
import edu.vu.isis.regtrack.common.EphemerisDatum;
import edu.vu.isis.regtrack.common.GpsConstants;
import edu.vu.isis.regtrack.common.GpsTime;
import edu.vu.isis.regtrack.common.ProcessedData;
import edu.vu.isis.regtrack.common.RawClockData;
import edu.vu.isis.regtrack.common.RawNavData;
import edu.vu.isis.regtrack.common.RawObservations;
import edu.vu.isis.regtrack.common.RawRxmData;
import edu.vu.isis.regtrack.common.SatelliteObservations.SatelliteObservation;
import edu.vu.isis.messaging.RLMessage;
import edu.vu.isis.messaging.RLMessageType;
import edu.vu.isis.messaging.RLModule;

public final class PreProcessor extends RLModule
{
	// Member variables
	private final String receiverID;
	private volatile double previousClockBias = 0.0, previousReceiveEpoch = 0.0;
	private volatile ProcessedData previousLocalData = new ProcessedData("", 0l, new Coordinate());
	private final EphemerisDatum[] ephData = new EphemerisDatum[GpsConstants.MAX_PRN+1];
	private final Lock preprocessingLock = new ReentrantLock(false);
	
	public PreProcessor(String moduleID, String rcvrID)
	{
		super(moduleID);
		
		addIncomingMessageType(RLMessageType.DECODED_DATA);
		addIncomingMessageType(RLMessageType.EPHEMERIS_DATA);
		
		receiverID = rcvrID;
	}
	
	@Override
	protected void incomingMessageHandler(final RLMessage message)
	{
		preprocessingLock.lock();
		try
		{
			switch (message.messageType)
			{
				case DECODED_DATA:
					DecodedData decodedData = ((DecodedData)message.data);
					performPreProcessing(decodedData.rawObservations, decodedData.rawClock, decodedData.rawNav);
					break;
				case EPHEMERIS_DATA:
					{
						EphemerisDatum ephDatum = ((EphemerisDatum)message.data);
						ephData[ephDatum.PRN] = ephDatum;
					}
					break;
				default:
					break;
			}
		}
		finally { preprocessingLock.unlock(); }
	}
	
	private void performPreProcessing(final RawObservations satData, final RawClockData clockData, final RawNavData navData)
	{
		// Make sure the receiver has settled and formed a solution
		if ((navData.X == 0.0) || (navData.Y == 0.0) || (navData.Z == 0.0))
			return;
		
		// Initialize rawData variables
		int satelliteNumber;
		ProcessedData rawData = new ProcessedData(receiverID, Math.round(satData.receiveTime.timeMS * 0.001), new Coordinate(navData.X, navData.Y, navData.Z));
		GpsTime receiveTime = new GpsTime(satData.receiveTime);
		rawData.receiverClockBias = (satData.receiveTime.timeMS + satData.receiveTime.fracMS - (navData.receiveTime.timeMS + navData.receiveTime.fracMS)) * 0.001;
		rawData.receiverClockDrift = clockData.clockDrift;
		rawData.PDOP = navData.PDOP;
		rawData.positionAccuracy = navData.posAccuracy;
		double timeDiff = rawData.receiveEpoch - previousReceiveEpoch;
		
		// Get current receiver data
		for (final RawRxmData observation : satData.observations)
		{
			// Calculate satellite array number
			satelliteNumber = observation.PRN;
			if ((satelliteNumber > GpsConstants.MAX_PRN) || (ephData[satelliteNumber] == null))
				continue;
			
			// Only use satellite measurements with good health, high elevation angles, and valid data
			if ((ephData[satelliteNumber].svHealth > 0) || !ephData[satelliteNumber].isValid ||
				(ephData[satelliteNumber].svAccur >= 5.0) || (observation.signalStrength < GpsConstants.MIN_SIGNAL_STRENGTH))
				continue;

			// Get pseudoranges, carrier code ranges, and doppler shifts
			SatelliteObservation newSatData = new SatelliteObservation(new EphemerisDatum(ephData[satelliteNumber]),
					observation.pseudorange, observation.carrierPhase * GpsConstants.LAMBDA_L1, observation.dopplerShift,
					observation.signalStrength, (observation.lossOfLock & 0x02) > 0);
			
			// Only use satellite measurements with good health, high elevation angles, and valid data
			rawData.observations.addData(satelliteNumber, newSatData);
		}
		
		// Correct pseudoranges for code bias and calculate satellite elevations and azimuths with respect to reference receiver position
		GpsTime[] transmitTimes = new GpsTime[GpsConstants.MAX_PRN+1];
		PreProcessingAlgorithms.correctDataForSatelliteClockBiases(rawData, satData.receiveTime, transmitTimes);
		Iterator<Entry<Integer, SatelliteObservation>> iter = rawData.observations.getFullSatelliteCollection().iterator();
		while (iter.hasNext())
		{
			// Calculate satellite position, velocity, elevation, and azimuth
			Entry<Integer, SatelliteObservation> entry = iter.next();
			EphemerisDatum ephDatum = entry.getValue().ephemerisDatum;
			PreProcessingAlgorithms.calculateSatellitePosition(satData.receiveTime, transmitTimes[entry.getKey()], ephDatum);
			PreProcessingAlgorithms.calculateElevationAndAzimuth(rawData.absoluteLocation, ephDatum);
			
			// Don't use satellites lower than 15 degrees
			if (ephDatum.elevation < GpsConstants.MIN_SATELLITE_ELEVATION)
				iter.remove();
		}
		
		// Calculate receiver clock bias via location estimation and then correct data
		double clockBiasEst = 0.0;
		ArrayList<Integer> badSatellites = new ArrayList<Integer>();
		while (Math.abs(rawData.receiverClockBias - clockBiasEst) > 1.0e-12)
		{
			// Update satellite positions based on estimated clock bias (changes travel time/Earth rotation)
			receiveTime = GpsTime.GpsTimeAdd(satData.receiveTime, -rawData.receiverClockBias*1000.0);
			clockBiasEst = rawData.receiverClockBias;
			for (final Integer PRN : rawData.observations.getValidSatelliteNumbers())
				PreProcessingAlgorithms.calculateSatellitePosition(receiveTime, transmitTimes[PRN], rawData.observations.getSatelliteData(PRN).ephemerisDatum);
			
			// Re-estimate the clock bias with the new satellite positions
			badSatellites.clear();
			if (!PreProcessingAlgorithms.estimateClockBias(rawData, badSatellites, rawData.absoluteLocation))
			{
				rawData.receiverClockBias = (Math.abs(previousClockBias) < 0.000000001) || (Math.abs(previousClockBias - clockBiasEst) > 0.000001) ?
						rawData.receiverClockBias : (previousClockBias + timeDiff*rawData.receiverClockDrift);
				receiveTime = GpsTime.GpsTimeAdd(satData.receiveTime, -rawData.receiverClockBias*1000.0);
				break;
			}
			
			// Remove erroneous satellite observations
			for (Integer PRN : badSatellites)
				rawData.observations.removeSatelliteData(PRN);
		}
		previousClockBias = rawData.receiverClockBias;
		previousReceiveEpoch = rawData.receiveEpoch;
		
		// Check for cycle slips
		PreProcessingAlgorithms.checkForCycleSlips(rawData, previousLocalData);
		
		// Extrapolate data and satellite positions to the nearest epoch
		PreProcessingAlgorithms.extrapolateDataToNearestEpoch(receiveTime, transmitTimes, rawData);
		
		// Store for later use
		previousLocalData = new ProcessedData(rawData);
		
		// Combine into one DecodedData packet and send to next module
		sendMessageToNextModule(new RLMessage(rawData));
	}
}
