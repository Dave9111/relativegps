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

package edu.vu.isis.regtrack.modules.networkmanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import edu.vu.isis.regtrack.common.ByteStream;
import edu.vu.isis.regtrack.common.Coordinate;
import edu.vu.isis.regtrack.common.EphemerisDatum;
import edu.vu.isis.regtrack.common.ProcessedData;
import edu.vu.isis.regtrack.common.SatelliteObservations.SatelliteObservation;
import edu.vu.isis.messaging.RLMessage;
import edu.vu.isis.messaging.RLMessageType;
import edu.vu.isis.messaging.RLModule;

public final class NetworkManager extends RLModule
{
	// Member variables
	private final String receiverID;
	
	// Constructor
	public NetworkManager(final String moduleID, final String rcvrID)
	{
		super(moduleID);
		
		receiverID = rcvrID;
		
		addIncomingMessageType(RLMessageType.BYTE_STREAM);
		addIncomingMessageType(RLMessageType.PROCESSED_DATA);
	}
	
	@Override
	protected void incomingMessageHandler(final RLMessage message)
	{
		switch (message.messageType)
		{
			case BYTE_STREAM:
				{
					try
					{
						// Get byte stream packet from network
						ByteArrayInputStream byteStreamIn = new ByteArrayInputStream(((ByteStream)message.data).bytes);
						DataInputStream dataStreamIn = new DataInputStream(byteStreamIn);
						String rcvrID = "";
						
						// Get receiver name
						int numChars = dataStreamIn.readInt();
						for (int i = 0; i < numChars; ++i)
							rcvrID += dataStreamIn.readChar();
						if (receiverID.equals(rcvrID))
							return;
						
						// Extract data from packet
						ProcessedData rawData = new ProcessedData(rcvrID, dataStreamIn.readLong(), new Coordinate());
						rawData.receiverClockBias = dataStreamIn.readDouble();
						rawData.receiverClockDrift = dataStreamIn.readDouble();
						rawData.absoluteLocation.setXYZ(dataStreamIn.readDouble(), dataStreamIn.readDouble(), dataStreamIn.readDouble());
						
						int numSatellites = dataStreamIn.readInt();
						for (int i = 0; i < numSatellites; ++i)
						{
							int PRN = dataStreamIn.readInt();
							SatelliteObservation satDatum = new SatelliteObservation(new EphemerisDatum(), dataStreamIn.readDouble(),
									dataStreamIn.readDouble(), dataStreamIn.readDouble(), dataStreamIn.readDouble(), dataStreamIn.readBoolean());
							satDatum.ephemerisDatum.X = dataStreamIn.readDouble();
							satDatum.ephemerisDatum.Y = dataStreamIn.readDouble();
							satDatum.ephemerisDatum.Z = dataStreamIn.readDouble();
							satDatum.ephemerisDatum.elevation = dataStreamIn.readDouble();
							satDatum.cycleSlips = dataStreamIn.readBoolean();
							rawData.observations.addData(PRN, satDatum);
						}
						
						// Send to next module
						sendMessageToNextModule(new RLMessage(rawData));
					}
					catch (IOException e) { System.err.println("Error reading network packet from input stream. Check packet parameters and try again."); }
				}
				break;
			case PROCESSED_DATA:
				{
					try
					{
						ProcessedData processedData = (ProcessedData)message.data;
						ByteArrayOutputStream byteStreamOut = new ByteArrayOutputStream();
						DataOutputStream dataStreamOut = new DataOutputStream(byteStreamOut);
						
						// Fill up buffer
						dataStreamOut.writeInt(processedData.ID.length());
						dataStreamOut.writeChars(processedData.ID);
						dataStreamOut.writeLong(processedData.receiveEpoch);
						dataStreamOut.writeDouble(processedData.receiverClockBias);
						dataStreamOut.writeDouble(processedData.receiverClockDrift);
						dataStreamOut.writeDouble(processedData.absoluteLocation.X);
						dataStreamOut.writeDouble(processedData.absoluteLocation.Y);
						dataStreamOut.writeDouble(processedData.absoluteLocation.Z);
						
						dataStreamOut.writeInt(processedData.observations.getValidSatelliteNumbers().size());
						for (final Entry<Integer, SatelliteObservation> satEntry : processedData.observations.getFullSatelliteCollection())
						{
							SatelliteObservation satDatum = satEntry.getValue();
							dataStreamOut.writeInt(satEntry.getKey());
							dataStreamOut.writeDouble(satDatum.pseudorange);
							dataStreamOut.writeDouble(satDatum.carrierRange);
							dataStreamOut.writeDouble(satDatum.dopplerShift);
							dataStreamOut.writeDouble(satDatum.signalStrength);
							dataStreamOut.writeBoolean(satDatum.potentialHalfCycleSlip);
							dataStreamOut.writeDouble(satDatum.ephemerisDatum.X);
							dataStreamOut.writeDouble(satDatum.ephemerisDatum.Y);
							dataStreamOut.writeDouble(satDatum.ephemerisDatum.Z);
							dataStreamOut.writeDouble(satDatum.ephemerisDatum.elevation);
							dataStreamOut.writeBoolean(satDatum.cycleSlips);
						}
						dataStreamOut.flush();
						
						// Send to next module
						sendMessageToNextModule(new RLMessage(new ByteStream(byteStreamOut.toByteArray())));
					}
					catch (IOException e) { System.err.println("Error writing network packet to output stream. Check packet parameters and try again."); }
				}
				break;
			default:
				break;
		}
	}
}
