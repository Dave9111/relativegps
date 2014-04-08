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

public final class Coordinate extends RLPassableDataType
{
	// Helper constants for XYZ-to-LLH conversion
	private static final double A1 = GpsConstants.EARTH_EQUATORIAL_RADIUS * GpsConstants.EARTH_ECCENTRICITY_2;
	private static final double A2 = A1*A1;
	private static final double A3 = 0.5 * GpsConstants.EARTH_EQUATORIAL_RADIUS * GpsConstants.EARTH_ECCENTRICITY_2 * GpsConstants.EARTH_ECCENTRICITY_2;
	private static final double A4 = (5.0/2.0) * A2;
	private static final double A5 = A1 + A3;
	private static final double A6 = 1.0 - GpsConstants.EARTH_ECCENTRICITY_2;
	
	public double X, Y, Z;
	public double latitude, longitude, height;
	
	public Coordinate()
	{
		super(RLMessageType.GPS_COORDINATE);
		
		X = 0.0;
		Y = 0.0;
		Z = 0.0;
		latitude = Double.NaN;
		longitude = Double.NaN;
		height = Double.NaN;
	}
	
	public Coordinate(Coordinate other)
	{
		super(RLMessageType.GPS_COORDINATE);
		
		X = other.X;
		Y = other.Y;
		Z = other.Z;
		latitude = other.latitude;
		longitude = other.longitude;
		height = other.height;
	}
	
	public Coordinate(double x, double y, double z)
	{
		super (RLMessageType.GPS_COORDINATE);
		
		X = x;
		Y = y;
		Z = z;
		latitude = Double.NaN;
		longitude = Double.NaN;
		height = Double.NaN;
	}
	
	public boolean isEqual(final Coordinate otherPoint) { return ((X == otherPoint.X) && (Y == otherPoint.Y) && (Z == otherPoint.Z)); }
	
	public void setCoordinates(final Coordinate otherPoint)
	{
		X = otherPoint.X;
		Y = otherPoint.Y;
		Z = otherPoint.Z;
		latitude = otherPoint.latitude;
		longitude = otherPoint.longitude;
		height = otherPoint.height;
	}
	
	public void setXYZ(double x, double y, double z)
	{
		X = x;
		Y = y;
		Z = z;
	}
	
	public void setLLH(double lat, double lon, double ht)
	{
		latitude = lat;
		longitude = lon;
		height = ht;
	}
	
	public void setXYZLLH(double x, double y, double z)
	{
		X = x;
		Y = y;
		Z = z;
		
		XYZ2LatLonH(this);
	}
	
	public void setLLHXYZ(double lat, double lon, double ht)
	{
		latitude = lat;
		longitude = lon;
		height = ht;
		
		LatLonH2XYZ(this);
	}
	
	public static void LatLonH2XYZ(final Coordinate llhPos)
	{
		double cosLat = Math.cos(Math.toRadians(llhPos.latitude)), sinLat = Math.sin(Math.toRadians(llhPos.latitude));
		double N = GpsConstants.EARTH_EQUATORIAL_RADIUS / Math.sqrt(1.0 - (GpsConstants.EARTH_ECCENTRICITY_2 * sinLat * sinLat));
		double commonParam = (N + llhPos.height) * cosLat;
		
		llhPos.X = commonParam * Math.cos(Math.toRadians(llhPos.longitude));
		llhPos.Y = commonParam * Math.sin(Math.toRadians(llhPos.longitude));
		llhPos.Z = ((N * (1.0 - GpsConstants.EARTH_ECCENTRICITY_2)) + llhPos.height) * sinLat;
	}
	
	public static void XYZ2LatLonH(final Coordinate ecefPos)
	{
		double positiveZ = Math.abs(ecefPos.Z), W2 = (ecefPos.X*ecefPos.X + ecefPos.Y*ecefPos.Y), Z2 = ecefPos.Z*ecefPos.Z;
		double W = Math.sqrt(W2), R2 = W2 + Z2;
		double R = Math.sqrt(R2);
		double S = positiveZ / R, C = W / R, U = A2 / R, V = A3 - (A4 / R);
		double S2 = S*S, C2 = C*C;
		
		// Compute longitude
		ecefPos.longitude = Math.atan2(ecefPos.Y, ecefPos.X);

		// Compute latitude differently depending on its nearness to the Earth's poles
		if (C2 > 0.3)
		{
			S *= (1.0 + C2*(A1 + U + S2*V)/R);
			ecefPos.latitude = Math.asin(S);
			S2 = S*S;
			C = Math.sqrt(1.0 - S2);
		}
		else
		{
			C *= (1.0 - S2*(A5 - U - C2*V)/R);
			ecefPos.latitude = Math.acos(C);
			S2 = 1.0 - C*C;
			S = Math.sqrt(S2);
		}
		
		// Compute height
		double G = 1.0 - GpsConstants.EARTH_ECCENTRICITY_2*S2;
		double R1 = GpsConstants.EARTH_EQUATORIAL_RADIUS / Math.sqrt(G);
		double Rf = A6*R1;
		U = W - R1*C; V = positiveZ - Rf*S;
		double F = C*U + S*V, M = C*V - S*U;
		double P = M / (Rf/G + F);
		ecefPos.latitude += P;
		ecefPos.height = F + 0.5*M*P;
		
		if (ecefPos.Z < 0.0)
			ecefPos.latitude = -ecefPos.latitude;
		ecefPos.latitude = Math.toDegrees(ecefPos.latitude);
		ecefPos.longitude = Math.toDegrees(ecefPos.longitude);
	}
	
	// Find the distance between two coordinates using Bowring formulas
	public static double computeENUDistance(final Coordinate referencePoint, final Coordinate remotePoint, boolean is3D)
	{
		double refLatRotation = Math.toRadians(referencePoint.latitude) - 0.5*Math.PI, refLonRotation = Math.toRadians(referencePoint.longitude) - Math.PI;
		double cosRefLatRotation = Math.cos(refLatRotation), sinRefLatRotation = Math.sin(refLatRotation);
		double cosRefLonRotation = Math.cos(refLonRotation), sinRefLonRotation = Math.sin(refLonRotation);

		// ECEF distance vector
		double xDiff = remotePoint.X - referencePoint.X;
		double yDiff = remotePoint.Y - referencePoint.Y;
		double zDiff = remotePoint.Z - referencePoint.Z;

		// Translate distance vector into ENU coordinates
		double x =                   sinRefLonRotation*xDiff -                   cosRefLonRotation*yDiff;
		double y = cosRefLatRotation*cosRefLonRotation*xDiff + cosRefLatRotation*sinRefLonRotation*yDiff - sinRefLatRotation*zDiff;
		double z = sinRefLatRotation*cosRefLonRotation*xDiff + sinRefLatRotation*sinRefLonRotation*yDiff + cosRefLatRotation*zDiff;

		// Return 2/3-D distance
		return is3D ? Math.sqrt(x*x + y*y + z*z) : Math.sqrt(x*x + y*y);
	}
}
