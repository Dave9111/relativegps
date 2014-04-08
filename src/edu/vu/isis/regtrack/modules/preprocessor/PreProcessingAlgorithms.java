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
import java.util.Map.Entry;

import edu.vu.isis.regtrack.common.Coordinate;
import edu.vu.isis.regtrack.common.EphemerisDatum;
import edu.vu.isis.regtrack.common.GpsConstants;
import edu.vu.isis.regtrack.common.GpsTime;
import edu.vu.isis.regtrack.common.Matrix;
import edu.vu.isis.regtrack.common.ProcessedData;
import edu.vu.isis.regtrack.common.SatelliteObservations.SatelliteObservation;

public final class PreProcessingAlgorithms
{
	public static void calculateSatellitePosition(final GpsTime receiveTime, final GpsTime transmitTime, final EphemerisDatum ephDatum)
	{
		double tk, u, r, i, O, cosE, sin2u, cos2u, M, E, sinE, EOld;
		double x, y, sinO, cosO, cosi, travelTime = GpsTime.GpsTimeSub(receiveTime, transmitTime);
		int iter;
		
		// Standard algorithm for finding satellite position
		if (!ephDatum.isSBAS)		// Standard GPS satellite
		{
			tk = GpsTime.GpsTimeSub(transmitTime, ephDatum.toe);
			M = ephDatum.M0 + (Math.sqrt(GpsConstants.EARTH_GRAVITATION / (ephDatum.A * ephDatum.A * ephDatum.A)) + ephDatum.deltaN) * tk;
			for (E = M, sinE = EOld = 0.0, iter = 0; (Math.abs(E - EOld) > 1E-15) && (iter < 11); ++iter)
			{
				EOld = E;
				sinE = Math.sin(EOld);
				E = M + ephDatum.e * sinE;
			}
			cosE = Math.cos(E);
			u = Math.atan2(Math.sqrt(1.0 - ephDatum.e * ephDatum.e) * Math.sin(E), cosE - ephDatum.e) + ephDatum.omega;
			sin2u = Math.sin(2.0*u);
			cos2u = Math.cos(2.0*u);
			u += ephDatum.cus * sin2u + ephDatum.cuc * cos2u;
			r = (ephDatum.A * (1.0 - ephDatum.e*cosE)) + ephDatum.crs * sin2u + ephDatum.crc * cos2u;
			i = (ephDatum.i0 + ephDatum.iDot*tk) + ephDatum.cis * sin2u + ephDatum.cic * cos2u;
			O = ephDatum.omega0 + (ephDatum.omegaDot - GpsConstants.EARTH_ANGULAR_VELOCITY)*tk - GpsConstants.EARTH_ANGULAR_VELOCITY*(ephDatum.toes*0.001 + travelTime);
			
			x = r*Math.cos(u);
			y = r*Math.sin(u);
			sinO = Math.sin(O);
			cosO = Math.cos(O);
			cosi = Math.cos(i);
			
			ephDatum.X = (x*cosO - y*cosi*sinO);
			ephDatum.Y = (x*sinO + y*cosi*cosO);
			ephDatum.Z = (y*Math.sin(i));
			ephDatum.satPosVariance = ephDatum.svAccur * ephDatum.svAccur;
		}
		else		// SBAS satellite
		{
			// Satellite position
			Matrix ephXYZ = new Matrix(3);
			ephXYZ.setValueAt(0, ephDatum.omega + (ephDatum.velX * ephDatum.i0) + (0.5 * ephDatum.accelX * ephDatum.i0 * ephDatum.i0));
			ephXYZ.setValueAt(1, ephDatum.omega0 + (ephDatum.velY * ephDatum.i0) + (0.5 * ephDatum.accelY * ephDatum.i0 * ephDatum.i0));
			ephXYZ.setValueAt(2, ephDatum.omegaDot + (ephDatum.velZ * ephDatum.i0) + (0.5 * ephDatum.accelZ * ephDatum.i0 * ephDatum.i0));
			
			// Correct satellite position due to the Earth's rotation
            double rotAngle = GpsConstants.EARTH_ANGULAR_VELOCITY * travelTime;
            Matrix rotMatrix = new Matrix(3, 3);
            rotMatrix.setValues(Math.cos(rotAngle), Math.sin(rotAngle), 0.0,
            		-Math.sin(rotAngle), Math.cos(rotAngle), 0.0,
            		0.0, 0.0, 1.0);
            ephXYZ = rotMatrix.matrixMultiply(ephXYZ);
            ephDatum.X = ephXYZ.valueAt(0);
            ephDatum.Y = ephXYZ.valueAt(1);
            ephDatum.Z = ephXYZ.valueAt(2);
			ephDatum.satPosVariance = ephDatum.svAccur * ephDatum.svAccur;
		}
	}
	
	public static void calculateElevationAndAzimuth(final Coordinate absolutePosition, final EphemerisDatum ephDatum)
	{
		double p = Math.sqrt(absolutePosition.X*absolutePosition.X + absolutePosition.Y*absolutePosition.Y);
		double R = Math.sqrt(p*p + absolutePosition.Z*absolutePosition.Z);
		double RInv = 1.0 / R, pInv = 1.0 / p, range, eastComponent, northComponent;
		double[] unit = new double[3];
		double[] east = new double[3], north = new double[3], up = new double[3];

		range = Math.sqrt(((ephDatum.X - absolutePosition.X)*(ephDatum.X - absolutePosition.X)) + ((ephDatum.Y - absolutePosition.Y)*(ephDatum.Y - absolutePosition.Y)) + ((ephDatum.Z - absolutePosition.Z)*(ephDatum.Z - absolutePosition.Z)));
		east[0] = -absolutePosition.Y * pInv;
		east[1] = absolutePosition.X * pInv;
		east[2] = 0.0;
		north[0] = (-absolutePosition.X * absolutePosition.Z) * pInv * RInv;
		north[1] = (-absolutePosition.Y * absolutePosition.Z) * pInv * RInv;
		north[2] = p * RInv;
		up[0] = absolutePosition.X * RInv;
		up[1] = absolutePosition.Y * RInv;
		up[2] = absolutePosition.Z * RInv;
		unit[0] = ((ephDatum.X - absolutePosition.X)/ range);
		unit[1] = ((ephDatum.Y - absolutePosition.Y)/ range);
		unit[2] = ((ephDatum.Z - absolutePosition.Z)/ range);
		eastComponent = ((unit[0]*east[0]) + (unit[1]*east[1]) + (unit[2]*east[2]));
		northComponent = ((unit[0]*north[0]) + (unit[1]*north[1]) + (unit[2]*north[2]));
		
		ephDatum.elevation = Math.abs(Math.asin((unit[0]*up[0]) + (unit[1]*up[1]) + (unit[2]*up[2])));
		ephDatum.azimuth = Math.atan(eastComponent / northComponent);
		
		if(northComponent < 0.0)
			ephDatum.azimuth += GpsConstants.PI;
		else if ((northComponent > 0.0) && (eastComponent < 0.0))
			ephDatum.azimuth += 2.0 * GpsConstants.PI;
	}
	
	public static void correctDataForSatelliteClockBiases(final ProcessedData rawData, final GpsTime rcvTime, final GpsTime[] sendTimes)
	{
		double tk, tc, M, EOld, E, sinE, dtr, satClockDrift;
		int iter;
		
		for (final Entry<Integer, SatelliteObservation> satEntry : rawData.observations.getFullSatelliteCollection())
		{
			SatelliteObservation satDatum = satEntry.getValue();
			if (satEntry.getKey() <= GpsConstants.MAX_GPS)
			{
				// Calculate transmission time by satellite clock (free of satellite clock error and relativistic effects)
				//   This will give us the satellite's clock bias
				GpsTime estSendTime = new GpsTime((rcvTime.timeMS + rcvTime.fracMS) - (satDatum.pseudorange * 1000.0 / GpsConstants.SPEED_OF_LIGHT));
				tk = GpsTime.GpsTimeSub(estSendTime, satDatum.ephemerisDatum.toe);
				tc = GpsTime.GpsTimeSub(estSendTime, satDatum.ephemerisDatum.toc);
				M = satDatum.ephemerisDatum.M0 + (Math.sqrt(GpsConstants.EARTH_GRAVITATION / (satDatum.ephemerisDatum.A * satDatum.ephemerisDatum.A * satDatum.ephemerisDatum.A)) + satDatum.ephemerisDatum.deltaN) * tk;
				for (E = M, sinE = EOld = 0.0, iter = 0; (Math.abs(E - EOld) > 1E-15) && (iter < 11); ++iter)
				{
					EOld = E;
					sinE = Math.sin(EOld);
					E = M + satDatum.ephemerisDatum.e * sinE;
				}
				dtr = GpsConstants.RELATIVISTIC_ERROR * satDatum.ephemerisDatum.e * satDatum.ephemerisDatum.rootA * Math.sin(E);
				satDatum.ephemerisDatum.satClockBias = satDatum.ephemerisDatum.af0 + (tc*(satDatum.ephemerisDatum.af1 + tc*satDatum.ephemerisDatum.af2)) + dtr - satDatum.ephemerisDatum.tgd;
				sendTimes[satEntry.getKey()] = GpsTime.GpsTimeAdd(estSendTime, -satDatum.ephemerisDatum.satClockBias*1000.0);
				satClockDrift = satDatum.ephemerisDatum.af1 + (2.0*satDatum.ephemerisDatum.af2*tc);
				
				satDatum.pseudorange += (satDatum.ephemerisDatum.satClockBias * GpsConstants.SPEED_OF_LIGHT);
				satDatum.carrierRange += (satDatum.ephemerisDatum.satClockBias * GpsConstants.SPEED_OF_LIGHT);
				satDatum.dopplerShift -= (satClockDrift * GpsConstants.FREQ_L1);
			}
			else
			{
				// Calculate time difference between current time and ephemeris time
				double receiveTimeOfDay = (double)((long)GpsTime.GpsTime2MSTimeOfWeek(rcvTime, null) % 86400000l);
				double sendTimeOfDay = receiveTimeOfDay - (satDatum.pseudorange * 1000.0 / GpsConstants.SPEED_OF_LIGHT);
				satDatum.ephemerisDatum.i0 = ((double)sendTimeOfDay - satDatum.ephemerisDatum.toes) * 0.001;
				if (satDatum.ephemerisDatum.i0 < 0.0)
					satDatum.ephemerisDatum.i0 += 86400.0;
				else if (satDatum.ephemerisDatum.i0 > 86400.0)
					satDatum.ephemerisDatum.i0 -= 86400.0;
				satDatum.ephemerisDatum.satClockBias = satDatum.ephemerisDatum.af0 + (satDatum.ephemerisDatum.af1*satDatum.ephemerisDatum.i0);
				sendTimes[satEntry.getKey()] = new GpsTime((rcvTime.timeMS + rcvTime.fracMS) - (satDatum.pseudorange * 1000.0 / GpsConstants.SPEED_OF_LIGHT) - (1000.0*satDatum.ephemerisDatum.satClockBias));
				
				satDatum.pseudorange += (satDatum.ephemerisDatum.satClockBias * GpsConstants.SPEED_OF_LIGHT);
				satDatum.carrierRange += (satDatum.ephemerisDatum.satClockBias * GpsConstants.SPEED_OF_LIGHT);
				satDatum.dopplerShift -= (satDatum.ephemerisDatum.af1 * GpsConstants.FREQ_L1);
			}
		}
	}
	
	public static void correctDataForReceiverClockBiases(final ProcessedData rawData)
	{
		for (final SatelliteObservation satDatum : rawData.observations.getSatelliteObservations())
		{
			satDatum.pseudorange -= (rawData.receiverClockBias * GpsConstants.SPEED_OF_LIGHT);
			satDatum.carrierRange -= (rawData.receiverClockBias * GpsConstants.SPEED_OF_LIGHT);
			satDatum.dopplerShift += (rawData.receiverClockDrift * GpsConstants.FREQ_L1);
		}
		
		rawData.receiverClockBias = 0.0;
		rawData.receiverClockDrift = 0.0;
	}
	
	public static void extrapolateDataToNearestEpoch(final GpsTime actualReceiveTime, final GpsTime[] sendTimes, final ProcessedData data)
	{
		double deltaRange, deltaRangePerSecond;
		
		// Extrapolate receiver clock bias to the nearest epoch and update what the local clock would have read at that point
		double timeDiff = (double)data.receiveEpoch - ((actualReceiveTime.timeMS + actualReceiveTime.fracMS) * 0.001);
		data.receiverClockBias += (data.receiverClockDrift * timeDiff);
		actualReceiveTime.timeMS = data.receiveEpoch * 1000l;
		actualReceiveTime.fracMS = 0.0;
		GpsTime extrapolatedReceiveTime = new GpsTime(actualReceiveTime.timeMS + data.receiverClockBias*1000.0);
		
		for (final SatelliteObservation satDatum : data.observations.getSatelliteObservations())
		{
			// Calculate total change in range when extrapolated to the nearest epoch
			deltaRangePerSecond = satDatum.dopplerShift * -GpsConstants.LAMBDA_L1;
			deltaRange = deltaRangePerSecond * timeDiff;
			
			// Update observables by the change in range
			satDatum.pseudorange += deltaRange;
			satDatum.carrierRange += deltaRange;

			// Update satellite send time corresponding to the received signal at the epoch
			sendTimes[satDatum.ephemerisDatum.PRN] = new GpsTime(extrapolatedReceiveTime.timeMS + extrapolatedReceiveTime.fracMS - (satDatum.pseudorange * 1000.0 / GpsConstants.SPEED_OF_LIGHT));
				
			// Calculate final satellite positions
			calculateSatellitePosition(actualReceiveTime, sendTimes[satDatum.ephemerisDatum.PRN], satDatum.ephemerisDatum);
		}
	}
	
	public static void checkForCycleSlips(final ProcessedData localData, final ProcessedData previousLocalData)
	{
		if (previousLocalData == null)
		{
			for (final SatelliteObservation satDatum : localData.observations.getSatelliteObservations())
				satDatum.cycleSlips = true;
			return;
		}
		
		// Find valid satellites and predict current carrier ranges based on Doppler shifts
		double predictedCarrierRange;
		for (final SatelliteObservation satDatum : localData.observations.getSatelliteObservations())
		{
			// Get satellite datum from previous epoch
			SatelliteObservation previousSatDatum = previousLocalData.observations.getSatelliteData(satDatum.ephemerisDatum.PRN);
			if (previousSatDatum == null)
			{
				satDatum.cycleSlips = true;
				continue;
			}
			
			// Predict what current carrier ranges should be based on previous Doppler shifts, and check if
			//   there is any possibility of a full slip
			predictedCarrierRange = previousSatDatum.carrierRange + ((satDatum.dopplerShift + previousSatDatum.dopplerShift) * 0.5 * -GpsConstants.LAMBDA_L1);
			if (Math.abs(satDatum.carrierRange - predictedCarrierRange) > (5.0 * GpsConstants.LAMBDA_L1))
				satDatum.cycleSlips = true;
		}
	}
	
	public static boolean estimateClockBias(final ProcessedData rawData, final ArrayList<Integer> ignoredSatellites, final Coordinate absoluteLocation)
	{
		Coordinate absolutePosition = null;
		double estimatedRange, clockBias = 0.0;
		boolean ignoreSatellites = true;
		
		while (ignoreSatellites)
		{
			// Count number of valid observations
			ignoreSatellites = false;
			int numValid = rawData.observations.getValidSatelliteNumbers().size() - ignoredSatellites.size(), iterations = 0;
			if (numValid < 4)
				return false;
			
			absolutePosition = new Coordinate(absoluteLocation);
			Matrix residuals = new Matrix(numValid, 1), cosCoeffs = new Matrix(numValid, 4), cosCoeffsTrans = new Matrix(4, numValid);
			Matrix intermediateMatrix1, intermediateMatrix2, deltaPos = new Matrix(4, 1);
			clockBias = rawData.receiverClockBias * GpsConstants.SPEED_OF_LIGHT;
					
			do
			{
				int index = 0;
				for (SatelliteObservation satDatum : rawData.observations.getSatelliteObservations())
				{
					// Ignore erroneous satellites
					if (ignoredSatellites.contains(satDatum.ephemerisDatum.PRN))
						continue;
					
					// Calculate direction cosines of unit vector from receiver to satellite
					estimatedRange = Math.sqrt((satDatum.ephemerisDatum.X - absolutePosition.X)*(satDatum.ephemerisDatum.X - absolutePosition.X) +
									  (satDatum.ephemerisDatum.Y - absolutePosition.Y)*(satDatum.ephemerisDatum.Y - absolutePosition.Y) + 
									  (satDatum.ephemerisDatum.Z - absolutePosition.Z)*(satDatum.ephemerisDatum.Z - absolutePosition.Z));
					cosCoeffs.setValueAt(index, 0, (satDatum.ephemerisDatum.X - absolutePosition.X) / estimatedRange);
					cosCoeffs.setValueAt(index, 1, (satDatum.ephemerisDatum.Y - absolutePosition.Y) / estimatedRange);
					cosCoeffs.setValueAt(index, 2, (satDatum.ephemerisDatum.Z - absolutePosition.Z) / estimatedRange);
					cosCoeffs.setValueAt(index, 3, -1.0);
				
					// Calculate difference between measured and estimated pseudorange
					residuals.setValueAt(index, 0, estimatedRange + clockBias - satDatum.pseudorange);
					++index;
				}
				
				// Solve for user position error using weighted least squares estimation
				//   (normal equation: deltaX = (H^T*H)^-1*H^T*deltaPR)
				cosCoeffs.matrixTranspose(cosCoeffsTrans);
				intermediateMatrix1 = cosCoeffsTrans.matrixMultiply(cosCoeffs);
				intermediateMatrix1.matrixInvertInPlace();
				intermediateMatrix2 = intermediateMatrix1.matrixMultiply(cosCoeffsTrans);
				intermediateMatrix2.matrixMultiply(deltaPos, residuals);
				
				// Use position errors to estimate receiver position
				absolutePosition.X += deltaPos.valueAt(0);
				absolutePosition.Y += deltaPos.valueAt(1);
				absolutePosition.Z += deltaPos.valueAt(2);
				clockBias += deltaPos.valueAt(3);
				
				// Make sure there is no obvious error based on a residual outlier
				if (iterations == 0)
				{
					index = 0;
					for (SatelliteObservation satDatum : rawData.observations.getSatelliteObservations())
					{
						if (ignoredSatellites.contains(satDatum.ephemerisDatum.PRN))
							continue;
						
						if (Math.abs(residuals.valueAt(index++, 0)) > GpsConstants.MAX_CLOCK_BIAS_ESTIMATE_RESIDUAL)
						{
							ignoredSatellites.add(satDatum.ephemerisDatum.PRN);
							ignoreSatellites = true;
							iterations = 10;
						}
					}
				}
			} while ((Math.sqrt((deltaPos.valueAt(0)*deltaPos.valueAt(0)) + (deltaPos.valueAt(1)*deltaPos.valueAt(1)) + (deltaPos.valueAt(2)*deltaPos.valueAt(2))) > 1E-5) && (iterations++ != 10));
		}
		
		if (Math.abs(clockBias - rawData.receiverClockBias*GpsConstants.SPEED_OF_LIGHT) < GpsConstants.MAX_CLOCK_BIAS_ESTIMATE_RESIDUAL)
		{
			rawData.receiverClockBias = clockBias / GpsConstants.SPEED_OF_LIGHT;
			return true;
		}
		
		return false;
	}
}
