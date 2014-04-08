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

package edu.vu.isis.regtrack.modules.dataaggregator;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.vu.isis.regtrack.common.GpsConstants;
import edu.vu.isis.regtrack.common.PairwiseData;
import edu.vu.isis.regtrack.common.ProcessedData;
import edu.vu.isis.messaging.RLMessage;
import edu.vu.isis.messaging.RLMessageType;
import edu.vu.isis.messaging.RLModule;

public final class DataAggregator extends RLModule
{
	private final String receiverID;
	private final Lock lock = new ReentrantLock(false);
	private final ArrayBlockingQueue<ProcessedData> localDataQueue = new ArrayBlockingQueue<ProcessedData>(5, true);
	private final HashMap<String, ArrayBlockingQueue<ProcessedData>> remoteData = new HashMap<String, ArrayBlockingQueue<ProcessedData>>();
	
	// Constructor
	public DataAggregator(String moduleID, String rcvrID)
	{
		super(moduleID);
		
		receiverID = rcvrID;
		
		addIncomingMessageType(RLMessageType.PROCESSED_DATA);
	}
	
	@Override
	protected void incomingMessageHandler(final RLMessage message)
	{
		switch (message.messageType)
		{
			case PROCESSED_DATA:
				{
					ProcessedData processedData = (ProcessedData)message.data;
					if (processedData.ID.equals(receiverID))
						processLocalData(processedData);
					else
						processRemoteData(processedData);
				}
				break;
			default:
				break;
		}
	}
	
	private void processLocalData(final ProcessedData localObservation)
	{
		// Forward to next modules
		sendMessageToNextModule(new RLMessage(localObservation), false);

		// Add to local data store for future use
		lock.lock();
		try
		{
			// Store for later use
			if (localDataQueue.remainingCapacity() == 0)
				localDataQueue.poll();
			localDataQueue.offer(localObservation);
		}
		finally { lock.unlock(); }
	}

	private void processRemoteData(final ProcessedData remoteObservation)
	{
		ProcessedData localObservation = null;
		ProcessedData previousLocalObservation = null, previousRemoteObservation = null;
		ArrayBlockingQueue<ProcessedData> remoteDataQueue = null;
		PairwiseData pairwiseData = null;
		boolean tooNew = true;
		long timeDiff = 0;
		
		// Find local data for corresponding epoch and previous epoch
		while (tooNew)
		{
			lock.lock();
			try
			{
				if ((localDataQueue.peek() != null) && (remoteObservation.receiveEpoch - localDataQueue.peek().receiveEpoch < 0l))
					return;
				else
				{
					for (ProcessedData datum : localDataQueue)
					{
						timeDiff = remoteObservation.receiveEpoch - datum.receiveEpoch;
						if (timeDiff == 0l)
							localObservation = datum;
						else if ((timeDiff - 1l) == 0l)
							previousLocalObservation = datum;
					}
				}
			}
			finally { lock.unlock(); }
			
			if ((timeDiff <= 0l) || (timeDiff > GpsConstants.LONGEST_TOLERABLE_TRACKING_OUTAGE))
				tooNew = false;
			else
				try { Thread.sleep(1); } catch (InterruptedException e) { return; }
		}
		if (localObservation == null)
			return;
		
		// Find remote data for previous epoch
		remoteDataQueue = remoteData.get(remoteObservation.ID);
		if (remoteDataQueue == null)
		{
			remoteDataQueue = new ArrayBlockingQueue<ProcessedData>(5, true);
			remoteData.put(remoteObservation.ID, remoteDataQueue);
		}
		for (ProcessedData datum : remoteDataQueue)
		{
			timeDiff = remoteObservation.receiveEpoch - datum.receiveEpoch;
			if ((timeDiff - 1l) == 0l)
			{
				previousRemoteObservation = datum;
				break;
			}
		}
		
		// Store remote observation for later use
		if (remoteDataQueue.remainingCapacity() == 0)
			remoteDataQueue.poll();
		remoteDataQueue.offer(remoteObservation);
		
		// Create pairwise data and send to localization modules
		pairwiseData = new PairwiseData(localObservation.receiveEpoch, localObservation, remoteObservation, previousLocalObservation, previousRemoteObservation);
		sendMessageToNextModule(new RLMessage(pairwiseData));
	}
}
