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

public final class EphemerisDatum extends RawUbxDataType
{
	public int PRN = 0, TOW = 0, week = 0, L2Code = 0, svHealth = 0, iode = 0, iodc = 0;
	public double svAccur = 0.0, af0 = 0.0, af1 = 0.0, af2 = 0.0, tgd = 0.0;
	public double A = 0.0, toes = 0.0, rootA = 0.0, e = 0.0, i0 = 0.0;
	public double iDot = 0.0, omega = 0.0, omega0 = 0.0, omegaDot = 0.0, M0 = 0.0;
	public double deltaN = 0.0, crc = 0.0, crs = 0.0, cuc = 0.0, cus = 0.0, cic = 0.0;
	public double cis = 0.0, fitInt = 0.0, X = 0.0, Y = 0.0, Z = 0.0, velX = 0.0;
	public double velY = 0.0, velZ = 0.0, accelX = 0.0, accelY = 0.0, accelZ = 0.0;
	public double elevation = 1.0, azimuth = 0.0;
	public double satClockBias = 0.0, satPosVariance = 0.0;
	public boolean isSBAS = false, hasFrame1 = false, hasFrame2 = false;
	public boolean hasFrame3 = false, isValid = false;
	public GpsTime toe, toc;
    
	public EphemerisDatum()
	{
		super(RLMessageType.EPHEMERIS_DATA, DataType.EPH_DATA, 0l);
		
		toe = new GpsTime();
		toc = new GpsTime();
	}
	
	public EphemerisDatum(final EphemerisDatum other)
    {
    	super(RLMessageType.EPHEMERIS_DATA, DataType.EPH_DATA, 0l);
		
		PRN = other.PRN;
		TOW = other.TOW;
		week = other.week;
		L2Code = other.L2Code;
		svAccur = other.svAccur;
		svHealth = other.svHealth;
		iode = other.iode;
		iodc = other.iodc;
		toe = new GpsTime(other.toe);
		toc = new GpsTime(other.toc);
		af0 = other.af0;
		af1 = other.af1;
		af2 = other.af2;
		tgd = other.tgd;
		A = other.A;
		toes = other.toes;
		rootA = other.rootA;
		e = other.e;
		i0 = other.i0;
		iDot = other.iDot;
		omega = other.omega;
		omega0 = other.omega0;
		omegaDot = other.omegaDot;
		M0 = other.M0;
		deltaN = other.deltaN;
		crc = other.crc;
		crs = other.crs;
		cuc = other.cuc;
		cus = other.cus;
		cic = other.cic;
		cis = other.cis;
		fitInt = other.fitInt;
		X = other.X;
		Y = other.Y;
		Z = other.Z;
		velX = other.velX;
		velY = other.velY;
		velZ = other.velZ;
		accelX = other.accelX;
		accelY = other.accelY;
		accelZ = other.accelZ;
		elevation =  other.elevation;
		azimuth = other.azimuth;
		satClockBias = other.satClockBias;
		satPosVariance = other.satPosVariance;
		isSBAS = other.isSBAS;
		hasFrame1 = other.hasFrame1;
		hasFrame2 = other.hasFrame2;
		hasFrame3 = other.hasFrame3;
		isValid = other.isValid;
    }
    
    public void setHasFrame(int frameNumber)
    {
    	switch (frameNumber)
    	{
    		case 0:
    			hasFrame1 = false;
    			hasFrame2 = false;
    			hasFrame3 = false;
    		case 1:
    			hasFrame1 = true;
    			hasFrame2 = false;
    			hasFrame3 = false;
    			break;
    		case 2:
    			hasFrame2 = true;
    			hasFrame3 = false;
    			break;
    		case 3:
    			hasFrame3 = true;
    			break;
    		default:
    			break;
    	}
    	
    	isValid = (hasFrame1 && hasFrame2 && hasFrame3);
    }
}

