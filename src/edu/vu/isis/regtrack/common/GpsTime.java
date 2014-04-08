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

import java.util.Calendar;
import java.util.TimeZone;

public final class GpsTime
{
	public static int monthStartingDates[] = {1, 32, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335};
	public static final GpsTime GpsRefTime = Epoch2Time(1980, 1, 6, 0, 0, 0);
	
	public long timeMS;			// time (in milliseconds)
	public double fracMS;		// fraction of time under 1 ms
	
	public GpsTime()
	{
		timeMS = 0l;
		fracMS = 0.0;
	}
	
	public GpsTime(double gpsTime)
	{
		timeMS = (long)Math.floor(gpsTime);
		fracMS = gpsTime - (double)timeMS;
	}
	
	public GpsTime(final GpsTime other)
	{
		timeMS = other.timeMS;
		fracMS = other.fracMS;
	}
	
	public double getTime() { return (double)timeMS + fracMS; }
	
	public static GpsTime Epoch2Time(long year, long month, long date, long hour, long minute, long second)
	{
		GpsTime time = new GpsTime();
		long days = (year-1970)*365 + (year-1969)/4 + monthStartingDates[(int)month-1] + date-2 + (((year%4 == 0) && (month >= 3)) ? 1 : 0);
	    time.timeMS = days*86400000l + hour*3600000l + minute*60000l + second*1000l;
	    return time;
	}
	
	public static int UTCWeek2GpsWeek(int utcWeek)
	{
		Calendar t = Calendar.getInstance();
		t.setTimeZone(TimeZone.getTimeZone("GMT"));
		GpsTime currEpoch = Epoch2Time(t.get(Calendar.YEAR), t.get(Calendar.MONTH)+1, t.get(Calendar.DAY_OF_MONTH), t.get(Calendar.HOUR_OF_DAY), t.get(Calendar.MINUTE), t.get(Calendar.SECOND));
		int gpsWeekWithoutRollover = (int)((currEpoch.timeMS - GpsRefTime.timeMS) / 604800000l) / 1024;
		return (gpsWeekWithoutRollover * 1024) + utcWeek;
	}
	
	public static GpsTime WeekAndMillisecond2GpsTime(int week, double millisec)
	{
		// Get the start of GPS time
	    GpsTime t = new GpsTime(GpsRefTime);
	    
	    t.timeMS += 604800000l*week + (long)millisec;
		t.fracMS = millisec - (long)millisec;

	    return t;
	}
	
	public static double GpsTime2MSTimeOfWeek(final GpsTime t, final EphemerisDatum ephStruct)
	{
		// Find the number of milliseconds elapsed since the beginning 
		//   of GPS time, and the number of weeks
		long millisec = t.timeMS - GpsRefTime.timeMS;
		int w = (int)(millisec / 604800000l);
	    
		// If not a NULL pointer, set number of weeks
		if (ephStruct != null)
			ephStruct.week = w;

		return (millisec - w*604800000l) + t.fracMS;
	}
	
	public static GpsTime GpsTimeAdd(final GpsTime t, double millisec)
	{
		GpsTime newTime = new GpsTime(t);

		// Add time to the number of elapsed seconds
		newTime.fracMS += millisec;

		// Determine the new number of whole seconds and
		//   add this to the GPS time
		long tt = (long)Math.floor(newTime.fracMS);
		newTime.timeMS += tt;

		// Remove the number of whole seconds from the
		//   fractional part, leaving only the time under 1s
		newTime.fracMS -= (double)tt;

		return newTime;
	}
	
	public static void GpsTimeAddInPlace(final GpsTime newTime, double millisec)
	{
		// Add time to the number of elapsed milliseconds
		newTime.fracMS += millisec;

		// Determine the new number of whole milliseconds and
		//   add this to the GPS time
		long tt = (long)Math.floor(newTime.fracMS);
		newTime.timeMS += tt;

		// Remove the number of whole milliseconds from the
		//   fractional part, leaving only the time under 1ms
		newTime.fracMS -= (double)tt;
	}

	// Find the difference (in seconds) between two GPS times
	//  Takes end of week crossover into account
	public static double GpsTimeSub(final GpsTime t1, final GpsTime t2)
	{
		double timeDiff = t1.timeMS - t2.timeMS + t1.fracMS - t2.fracMS;
		
		if (timeDiff > GpsConstants.MILLISEC_IN_HALF_WEEK)
			timeDiff -= (2.0 * GpsConstants.MILLISEC_IN_HALF_WEEK);
		else if (timeDiff < -GpsConstants.MILLISEC_IN_HALF_WEEK)
			timeDiff += (2.0 * GpsConstants.MILLISEC_IN_HALF_WEEK);
		
		return timeDiff * 0.001;
	}
}
