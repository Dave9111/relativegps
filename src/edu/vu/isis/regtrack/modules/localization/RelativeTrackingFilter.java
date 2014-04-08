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

package edu.vu.isis.regtrack.modules.localization;

import java.util.ArrayList;

import edu.vu.isis.regtrack.common.Coordinate;
import edu.vu.isis.regtrack.common.GpsConstants;
import edu.vu.isis.regtrack.common.Matrix;
import edu.vu.isis.regtrack.common.PairwiseData;
import edu.vu.isis.regtrack.common.PairwiseData.ManipulatedData;
import edu.vu.isis.regtrack.common.SatelliteObservations.SatelliteObservation;

public final class RelativeTrackingFilter
{
	private final Coordinate relativeBaseline = new Coordinate();
	private final Coordinate relativeVelocity = new Coordinate();
	private final Coordinate relativeAcceleration = new Coordinate();
	private final Coordinate deltaBaseline = new Coordinate();
	private long previousEpoch = 0l;
	
	public void updateEstimatedBaseline(final Coordinate estimatedBaseline) { relativeBaseline.setXYZ(estimatedBaseline.X, estimatedBaseline.Y, estimatedBaseline.Z); }
	
	// Returns the confidence index for the tracking update
	//   0 = Good (Successful tracking with 5+ satellites)
	//   1 = Fair (Successful tracking with 4 satellites and no clock bias estimation)
	//   2... = Extrapolated (where 'value-1' = number of epochs extrapolated over)
	//  -1 = Bad (Could not track)
	public int trackReceiver(final PairwiseData observation, final Coordinate baselineResult, final Coordinate trackingResult, final ArrayList<Integer> ignoredSatellites)
	{
		int confidenceIndex = -1;
		if (observation.receiveEpoch - previousEpoch == 1l)		// Standard tracking update
		{
			// Perform tracking using temporal double difference
			ignoredSatellites.clear();
			confidenceIndex = trackUsingTemporalDoubleDifference(observation, ignoredSatellites);
			
			// If could not successfully track
			if (confidenceIndex == 2)
			{
				// Extrapolate current velocity
				relativeVelocity.setXYZ(relativeVelocity.X + relativeAcceleration.X,
						relativeVelocity.Y + relativeAcceleration.Y,
						relativeVelocity.Z + relativeAcceleration.Z);
			}
			
			// Update relative baseline estimate
			relativeBaseline.setXYZ(relativeBaseline.X + relativeVelocity.X,
					relativeBaseline.Y + relativeVelocity.Y,
					relativeBaseline.Z + relativeVelocity.Z);
		}
		else if (observation.receiveEpoch - previousEpoch > GpsConstants.LONGEST_TOLERABLE_TRACKING_OUTAGE)		// Long loss of all locks
		{
			// Set relative baseline from absolute position estimates of each receiver
			relativeBaseline.setXYZ(observation.remote.absoluteLocation.X - observation.local.absoluteLocation.X,
					observation.remote.absoluteLocation.Y - observation.local.absoluteLocation.Y,
					observation.remote.absoluteLocation.Z - observation.local.absoluteLocation.Z);
			relativeVelocity.setXYZ(0.0, 0.0, 0.0);
			relativeAcceleration.setXYZ(0.0, 0.0, 0.0);
		}
		else																// Loss of lock less than 5 seconds long
		{
			confidenceIndex = 1;
			
			// Extrapolate through missing epochs
			while (observation.receiveEpoch > previousEpoch)
			{
				relativeVelocity.setXYZ(relativeVelocity.X + relativeAcceleration.X,
						relativeVelocity.Y + relativeAcceleration.Y,
						relativeVelocity.Z + relativeAcceleration.Z);
				relativeBaseline.setXYZ(relativeBaseline.X + relativeVelocity.X,
						relativeBaseline.Y + relativeVelocity.Y,
						relativeBaseline.Z + relativeVelocity.Z);
				++previousEpoch;
				++confidenceIndex;
			}
		}
		
		// Update epoch based on success of relative tracking procedure
		previousEpoch = (Math.sqrt(relativeBaseline.X*relativeBaseline.X + relativeBaseline.Y*relativeBaseline.Y + relativeBaseline.Z*relativeBaseline.Z) <= GpsConstants.MAX_BASELINE_LENGTH) ? 
				observation.receiveEpoch : 0l;
		
		// Set resulting tracking coordinates
		trackingResult.setCoordinates(relativeVelocity);
		baselineResult.setCoordinates(relativeBaseline);
		
		return (previousEpoch == 0l) ? -1 : confidenceIndex;
	}
	
	// Returns the confidence value for the tracking update
	//   0 = Good (5+ satellites)
	//   1 = Fair (4 satellites, no estimation of clock drift SD)
	//   2 = Bad (could not track)
	private int trackUsingTemporalDoubleDifference(final PairwiseData observation, final ArrayList<Integer> ignoredSatellites)
	{
		if (!observation.hasPreviousData)
			return 2;
		int numValid = 0, iterations = 0;
		for (ManipulatedData datum : observation.manipulatedData.values())
			if (datum.isTemporallyValid)
				++numValid;
		
		Matrix residuals, cosCoeffs, cosCoeffsTrans, intermediateMatrix1, intermediateMatrix2, deltaDeltaPos = new Matrix(4, 1);
		double calcClockDriftSD = (observation.remote.receiverClockDrift - observation.local.receiverClockDrift) * GpsConstants.SPEED_OF_LIGHT;
		double estRange, estTDD, estClockDriftSD, residual, currentClockDriftSDError = 0.0, previousClockDriftSDError = Double.MAX_VALUE;
		boolean measurementError = true, estimateClockDriftSD = true;
		int lastSatelliteIgnored = 0, lastSatelliteAlmostIgnored = 0;
		
		while (measurementError)
		{
			if (numValid < 4)
				return 2;
			else if (numValid == 4)
				estimateClockDriftSD = false;
			
			deltaBaseline.setXYZ(0.0, 0.0, 0.0);
			estClockDriftSD = calcClockDriftSD;
			iterations = 0;
			residuals = new Matrix(numValid, 1);
			cosCoeffs = new Matrix(numValid, estimateClockDriftSD ? 4 : 3);
			cosCoeffsTrans = new Matrix(estimateClockDriftSD ? 4 : 3, numValid);
			
			do
			{
				int index = 0;
				for (Integer PRN : observation.manipulatedData.keySet())
				{
					if (!observation.manipulatedData.get(PRN).isTemporallyValid || ignoredSatellites.contains(PRN))
						continue;
					
					SatelliteObservation local = observation.local.observations.getSatelliteData(PRN), remote = observation.remote.observations.getSatelliteData(PRN);
					SatelliteObservation previousLocal = observation.previousLocal.observations.getSatelliteData(PRN), previousRemote = observation.previousRemote.observations.getSatelliteData(PRN);
					double weight = ((local.signalStrength-GpsConstants.MIN_SIGNAL_STRENGTH)/44.0) + ((remote.signalStrength-GpsConstants.MIN_SIGNAL_STRENGTH)/44.0);
					
					// Calculate direction cosines of unit vector from receiver to satellite
					estRange = Math.sqrt((remote.ephemerisDatum.X-observation.referencePosition.X-relativeBaseline.X-deltaBaseline.X)*(remote.ephemerisDatum.X-observation.referencePosition.X-relativeBaseline.X-deltaBaseline.X) +
										 (remote.ephemerisDatum.Y-observation.referencePosition.Y-relativeBaseline.Y-deltaBaseline.Y)*(remote.ephemerisDatum.Y-observation.referencePosition.Y-relativeBaseline.Y-deltaBaseline.Y) +
										 (remote.ephemerisDatum.Z-observation.referencePosition.Z-relativeBaseline.Z-deltaBaseline.Z)*(remote.ephemerisDatum.Z-observation.referencePosition.Z-relativeBaseline.Z-deltaBaseline.Z));
					estTDD = estRange -
							Math.sqrt((local.ephemerisDatum.X-observation.referencePosition.X)*(local.ephemerisDatum.X-observation.referencePosition.X) +
									 (local.ephemerisDatum.Y-observation.referencePosition.Y)*(local.ephemerisDatum.Y-observation.referencePosition.Y) +
									 (local.ephemerisDatum.Z-observation.referencePosition.Z)*(local.ephemerisDatum.Z-observation.referencePosition.Z)) -
							Math.sqrt((previousRemote.ephemerisDatum.X-observation.referencePosition.X-relativeBaseline.X)*(previousRemote.ephemerisDatum.X-observation.referencePosition.X-relativeBaseline.X) +
									 (previousRemote.ephemerisDatum.Y-observation.referencePosition.Y-relativeBaseline.Y)*(previousRemote.ephemerisDatum.Y-observation.referencePosition.Y-relativeBaseline.Y) +
									 (previousRemote.ephemerisDatum.Z-observation.referencePosition.Z-relativeBaseline.Z)*(previousRemote.ephemerisDatum.Z-observation.referencePosition.Z-relativeBaseline.Z)) +
							Math.sqrt((previousLocal.ephemerisDatum.X-observation.referencePosition.X)*(previousLocal.ephemerisDatum.X-observation.referencePosition.X) +
									 (previousLocal.ephemerisDatum.Y-observation.referencePosition.Y)*(previousLocal.ephemerisDatum.Y-observation.referencePosition.Y) +
									 (previousLocal.ephemerisDatum.Z-observation.referencePosition.Z)*(previousLocal.ephemerisDatum.Z-observation.referencePosition.Z)) + estClockDriftSD;
					cosCoeffs.setValueAt(index, 0, ((remote.ephemerisDatum.X - observation.referencePosition.X - relativeBaseline.X - deltaBaseline.X) / estRange) * weight);
					cosCoeffs.setValueAt(index, 1, ((remote.ephemerisDatum.Y - observation.referencePosition.Y - relativeBaseline.Y - deltaBaseline.Y) / estRange) * weight);
					cosCoeffs.setValueAt(index, 2, ((remote.ephemerisDatum.Z - observation.referencePosition.Z - relativeBaseline.Z - deltaBaseline.Z) / estRange) * weight);
					if (estimateClockDriftSD)
						cosCoeffs.setValueAt(index, 3, -1.0 * weight);
					residuals.setValueAt(index, 0, (estTDD - observation.manipulatedData.get(PRN).doubleDifferenceTemporalCarrierRange) * weight);
					++index;
				}
		
				// Solve for user position error using weighted least squares estimation
				//   (normal equation: deltaX = (H^T*H)^-1*H^T*deltaC R)
				cosCoeffs.matrixTranspose(cosCoeffsTrans);
				intermediateMatrix1 = cosCoeffsTrans.matrixMultiply(cosCoeffs);
				intermediateMatrix1.matrixInvertInPlace();
				intermediateMatrix2 = intermediateMatrix1.matrixMultiply(cosCoeffsTrans);
				intermediateMatrix2.matrixMultiply(deltaDeltaPos, residuals);
	
				// Make sure there isn't an obvious error as evinced by a wrong clock drift SD estimate
				if (iterations == 0)
					currentClockDriftSDError = estimateClockDriftSD ? Math.abs(deltaDeltaPos.valueAt(3)) : -Double.MAX_VALUE;
				
				// Use position errors to estimate receiver position
				deltaBaseline.X += deltaDeltaPos.valueAt(0);
				deltaBaseline.Y += deltaDeltaPos.valueAt(1);
				deltaBaseline.Z += deltaDeltaPos.valueAt(2);
				if (estimateClockDriftSD)
					estClockDriftSD += deltaDeltaPos.valueAt(3);
			} while ((Math.sqrt((deltaDeltaPos.valueAt(0)*deltaDeltaPos.valueAt(0)) + (deltaDeltaPos.valueAt(1)*deltaDeltaPos.valueAt(1)) + (deltaDeltaPos.valueAt(2)*deltaDeltaPos.valueAt(2))) > 0.001) && (iterations++ != 10));
			
			// Calculate unweighted residuals based on weighted solution
			double maxResidual = -Double.MAX_VALUE, secondMaxResidual = -Double.MAX_VALUE;
			int satWithMaxError = 0, satWithSecondMaxError = 0;
			for (Integer PRN : observation.manipulatedData.keySet())
			{
				if (!observation.manipulatedData.get(PRN).isTemporallyValid || ignoredSatellites.contains(PRN))
					continue;
				
				SatelliteObservation local = observation.local.observations.getSatelliteData(PRN), remote = observation.remote.observations.getSatelliteData(PRN);
				SatelliteObservation previousLocal = observation.previousLocal.observations.getSatelliteData(PRN), previousRemote = observation.previousRemote.observations.getSatelliteData(PRN);
				
				// Calculate direction cosines of unit vector from receiver to satellite
				estTDD = Math.sqrt((remote.ephemerisDatum.X-observation.referencePosition.X-relativeBaseline.X-deltaBaseline.X)*(remote.ephemerisDatum.X-observation.referencePosition.X-relativeBaseline.X-deltaBaseline.X) +
								   (remote.ephemerisDatum.Y-observation.referencePosition.Y-relativeBaseline.Y-deltaBaseline.Y)*(remote.ephemerisDatum.Y-observation.referencePosition.Y-relativeBaseline.Y-deltaBaseline.Y) +
								   (remote.ephemerisDatum.Z-observation.referencePosition.Z-relativeBaseline.Z-deltaBaseline.Z)*(remote.ephemerisDatum.Z-observation.referencePosition.Z-relativeBaseline.Z-deltaBaseline.Z)) -
						 Math.sqrt((local.ephemerisDatum.X-observation.referencePosition.X)*(local.ephemerisDatum.X-observation.referencePosition.X) +
								   (local.ephemerisDatum.Y-observation.referencePosition.Y)*(local.ephemerisDatum.Y-observation.referencePosition.Y) +
								   (local.ephemerisDatum.Z-observation.referencePosition.Z)*(local.ephemerisDatum.Z-observation.referencePosition.Z)) -
						 Math.sqrt((previousRemote.ephemerisDatum.X-observation.referencePosition.X-relativeBaseline.X)*(previousRemote.ephemerisDatum.X-observation.referencePosition.X-relativeBaseline.X) +
								   (previousRemote.ephemerisDatum.Y-observation.referencePosition.Y-relativeBaseline.Y)*(previousRemote.ephemerisDatum.Y-observation.referencePosition.Y-relativeBaseline.Y) +
								   (previousRemote.ephemerisDatum.Z-observation.referencePosition.Z-relativeBaseline.Z)*(previousRemote.ephemerisDatum.Z-observation.referencePosition.Z-relativeBaseline.Z)) +
						 Math.sqrt((previousLocal.ephemerisDatum.X-observation.referencePosition.X)*(previousLocal.ephemerisDatum.X-observation.referencePosition.X) +
								   (previousLocal.ephemerisDatum.Y-observation.referencePosition.Y)*(previousLocal.ephemerisDatum.Y-observation.referencePosition.Y) +
								   (previousLocal.ephemerisDatum.Z-observation.referencePosition.Z)*(previousLocal.ephemerisDatum.Z-observation.referencePosition.Z)) + estClockDriftSD;
				
				// Find satellite with first and second greatest residuals
				residual = Math.abs(estTDD - observation.manipulatedData.get(PRN).doubleDifferenceTemporalCarrierRange);
				if (residual > maxResidual)
				{
					secondMaxResidual = maxResidual;
					satWithSecondMaxError = satWithMaxError;
					maxResidual = residual;
					satWithMaxError = PRN;
				}
				else if (residual > secondMaxResidual)
				{
					secondMaxResidual = residual;
					satWithSecondMaxError = PRN;
				}
			}
			
			// Decide whether to accept solution or retry
			if (!estimateClockDriftSD && (maxResidual < (GpsConstants.LAMBDA_L1 * 0.5)))
				measurementError = false;
			else if ((maxResidual < (GpsConstants.LAMBDA_L1 * 0.2)) && (currentClockDriftSDError < 1.5))
				measurementError = false;
			else if (currentClockDriftSDError > previousClockDriftSDError)
			{
				ignoredSatellites.remove((Integer)lastSatelliteIgnored);
				ignoredSatellites.add(lastSatelliteAlmostIgnored);
				lastSatelliteIgnored = lastSatelliteAlmostIgnored;
				lastSatelliteAlmostIgnored = satWithMaxError;
				previousClockDriftSDError = currentClockDriftSDError;
			}
			else
			{
				ignoredSatellites.add(satWithMaxError);
				lastSatelliteIgnored = satWithMaxError;
				lastSatelliteAlmostIgnored = satWithSecondMaxError;
				previousClockDriftSDError = currentClockDriftSDError;
				--numValid;
			}
		}
		
		// Ensure we didn't pick up an invalid solution
		if (Math.sqrt(deltaBaseline.X*deltaBaseline.X + deltaBaseline.Y*deltaBaseline.Y + deltaBaseline.Z*deltaBaseline.Z) > GpsConstants.MAX_SINGLE_EPOCH_CHANGE_IN_BASELINE_LENGTH)
			return 2;
		
		// Update system dynamics
		relativeAcceleration.X = deltaBaseline.X - relativeVelocity.X;
		relativeAcceleration.Y = deltaBaseline.Y - relativeVelocity.Y;
		relativeAcceleration.Z = deltaBaseline.Z - relativeVelocity.Z;
		relativeVelocity.X = deltaBaseline.X;
		relativeVelocity.Y = deltaBaseline.Y;
		relativeVelocity.Z = deltaBaseline.Z;
		
		return (estimateClockDriftSD ? 0 : 1);
	}
}
