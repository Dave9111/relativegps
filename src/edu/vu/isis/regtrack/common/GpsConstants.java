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

public final class GpsConstants
{
	public static final int MAX_PRN = 71;
	public static final int MAX_GPS = 32;
	public static final double MIN_SIGNAL_STRENGTH = 28.0;
	public static final double MIN_SATELLITE_ELEVATION = Math.toRadians(15.0);
	public static final long LONGEST_TOLERABLE_TRACKING_OUTAGE = 5l;
	public static final double MAX_CLOCK_BIAS_ESTIMATE_RESIDUAL = 100.0;
	public static final double MAX_BASELINE_LENGTH = 100000.0;
	public static final double MAX_SINGLE_EPOCH_CHANGE_IN_BASELINE_LENGTH = 100.0;
	
	public static final double[] EPH_SVA_VALS = {2.4,3.4,4.85,6.85,9.65,13.65,24.0,48.0,96.0,192.0,384.0,768.0,1536.0,3072.0,6144.0};
	public static final double PI = 3.1415926535898;
	public static final double SPEED_OF_LIGHT = 299792458.0;
	public static final double FREQ_L1 = 1.57542e9;
	public static final double FREQ_L2 = 1.22760e9;
	public static final double LAMBDA_L1 = 0.19029367279836488047631742646405;
	public static final double LAMBDA_L2 = 0.24421021342456826327794069729554;
	public static final double HALF_LAMBDA_L1 = 0.09514683639918244023815871323203;
	public static final double WORST_CASE_MULTIPATH_CARRIER_ERROR = 0.05;
	public static final double RELATIVISTIC_ERROR = -4.442807633e-10;
	public static final double EARTH_GRAVITATION = 3.986005e14;
	public static final double EARTH_ANGULAR_VELOCITY = 7.2921151467e-5;
	public static final double EARTH_EQUATORIAL_RADIUS = 6378137.0;
	public static final double EARTH_SEMIMINOR_AXIS = 6356752.314245;
	public static final double EARTH_ECCENTRICITY_2 = 0.006694379990141317;
	public static final double EARTH_SECOND_ECCENTRICITY_2 = 0.00673949674228;
	public static final double EARTH_FLATTENING = (EARTH_EQUATORIAL_RADIUS - EARTH_SEMIMINOR_AXIS) / EARTH_EQUATORIAL_RADIUS;
	public static final double MILLISEC_IN_HALF_WEEK = 302400000.0;
	public static final double SEC_IN_HALF_WEEK = 302400.0;
}
