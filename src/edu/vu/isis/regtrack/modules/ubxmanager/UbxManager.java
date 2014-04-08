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

package edu.vu.isis.regtrack.modules.ubxmanager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.vu.isis.regtrack.common.ByteStream;
import edu.vu.isis.regtrack.common.DecodedData;
import edu.vu.isis.regtrack.common.EphemerisDatum;
import edu.vu.isis.regtrack.common.GpsConstants;
import edu.vu.isis.regtrack.common.RawClockData;
import edu.vu.isis.regtrack.common.RawNavData;
import edu.vu.isis.regtrack.common.RawObservations;
import edu.vu.isis.regtrack.common.RawUbxDataType;
import edu.vu.isis.messaging.RLMessage;
import edu.vu.isis.messaging.RLMessageType;
import edu.vu.isis.messaging.RLModule;

public final class UbxManager extends RLModule
{
	// Member variables
	private final String receiverID;
	private volatile RawObservations rawRxmData;
	private volatile RawClockData rawClock;
	private volatile RawNavData rawNav;
	private final UbxDecoder ubxDecoder;
	private volatile boolean newRawData = false, newClockData = false, newNavData = false;
	public volatile boolean isRunning = false, stopRunning = false;
	private volatile long currentEpoch = 0l;
	private volatile boolean loggingEnabled = false;
	private volatile FileOutputStream ubxWriter = null;
	private final Lock lock;
	
	// Constructor
	public UbxManager(String moduleID, final String rcvrID)
	{
		super(moduleID);
		
		receiverID = rcvrID;
		ubxDecoder = new UbxDecoder();
		lock = new ReentrantLock(false);
		
		addIncomingMessageType(RLMessageType.BYTE_STREAM);
	}
	
	@Override
	protected void incomingMessageHandler(final RLMessage message)
	{
		switch (message.messageType)
		{
			case BYTE_STREAM:
				{
					// Locked so processing doesn't get interleaved if another packet comes in
					lock.lock();
					try
					{
						byte[] dataPacket = ((ByteStream)message.data).bytes;
						
						// Log packet, if enabled
						if (loggingEnabled)
							writeUbxPacket(dataPacket, ubxWriter);
						
						// Decode packet and process it
						RawUbxDataType rawUBXData = ubxDecoder.decode(dataPacket);
						if (rawUBXData != null)
							processRawUbxData(rawUBXData);
					}
					finally { lock.unlock(); }
				}
				break;
			default:
				break;
		}
	}
	
	public void enableLogging(boolean isLoggingEnabled)
	{
		if (isLoggingEnabled)
		{
			// Open log file, if enabled
			if (ubxWriter == null)
				try { ubxWriter = new FileOutputStream("UBX-LogFile-" + receiverID + ".log"); } catch (Exception e) {}
		}
		else
		{
			// Close log file, if opened
			if (ubxWriter != null)
			{
				try { ubxWriter.close(); } catch (Exception e) {}
				ubxWriter = null;
			}
		}
	
		// Set global logging flag
		loggingEnabled = isLoggingEnabled;
	}
	
	private void processRawUbxData(final RawUbxDataType rawUBXData)
	{
		// If this is new ephemeris data, simply forward to the next module
		if (rawUBXData.dataType == RawUbxDataType.DataType.EPH_DATA)
		{
			sendMessageToNextModule(new RLMessage((EphemerisDatum)rawUBXData), false);
			return;
		}
		
		// Make sure data is coming from the same epoch
		if (rawUBXData.receiveEpoch == 0l)
			return;
		else if ((rawUBXData.receiveEpoch > currentEpoch) ||
			     (currentEpoch - rawUBXData.receiveEpoch > GpsConstants.SEC_IN_HALF_WEEK))
		{
			currentEpoch = rawUBXData.receiveEpoch;
			newRawData = newClockData = newNavData = false;
		}
		
		// See what kind of data this is
		switch (rawUBXData.dataType)
		{
			case RAW_DATA:
				rawRxmData = (RawObservations)rawUBXData;
				newRawData = true;
				break;
			case RAW_CLOCK:
				rawClock = (RawClockData)rawUBXData;
				newClockData = true;
				break;
			case RAW_NAV:
				rawNav = (RawNavData)rawUBXData;
				newNavData = true;
				break;
			default:
				break;
		}
		
		if (newRawData && newClockData && newNavData)
		{
			newRawData = newClockData = newNavData = false;
			
			// Combine into one DecodedData packet and send to next module
			sendMessageToNextModule(new RLMessage(new DecodedData(rawClock, rawNav, rawRxmData)), false);
		}
	}
	
	private void writeUbxPacket(final byte[] ubxPacket, final FileOutputStream logFile)
	{
		try
		{
			logFile.write(ubxPacket);
			logFile.flush();
		}
		catch (IOException e) { e.printStackTrace(); }
	}
}
