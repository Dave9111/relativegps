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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.vu.isis.regtrack.common.Coordinate;
import edu.vu.isis.regtrack.common.PairwiseData;
import edu.vu.isis.regtrack.common.Result;
import edu.vu.isis.messaging.RLMessage;
import edu.vu.isis.messaging.RLMessageType;
import edu.vu.isis.messaging.RLModule;

public final class RelativeLocalizer extends RLModule
{
	// Member variables
	private final ConcurrentHashMap<String, Lock> localizationLocks = new ConcurrentHashMap<String, Lock>();
	private final ConcurrentHashMap<String, RelativeTrackingFilter> relativeTrackingFilters = new ConcurrentHashMap<String, RelativeTrackingFilter>();
	
	// Constructor
	public RelativeLocalizer(String moduleID)
	{
		super(moduleID);
		
		addIncomingMessageType(RLMessageType.PAIRWISE_DATA);
	}
	
	@Override
	protected void incomingMessageHandler(final RLMessage message)
	{
		switch (message.messageType)
		{
			case PAIRWISE_DATA:
				{
					// Get pairwise data observations
					Coordinate trackingResult = new Coordinate(), relativeBaseline = new Coordinate();
					ArrayList<Integer> ignoredSatellites = new ArrayList<Integer>();
					PairwiseData observation = (PairwiseData)message.data;
					if (observation.referenceSatellite == 0)
						return;
					
					// Get filters associated with the remote receiver
					Lock localizationLock = localizationLocks.get(observation.remote.ID);
					RelativeTrackingFilter relativeTrackingFilter = relativeTrackingFilters.get(observation.remote.ID);
					if (localizationLock == null)
					{
						localizationLock = new ReentrantLock(true);
						relativeTrackingFilter = new RelativeTrackingFilter();
						localizationLocks.put(observation.remote.ID, localizationLock);
						relativeTrackingFilters.put(observation.remote.ID, relativeTrackingFilter);
					}
					
					// Since localization can take longer than a single epoch, a fair lock must be used to ensure data
					//   consistency and ordering through time
					localizationLock.lock();
					try
					{
						// Track relative position
						int confidence = relativeTrackingFilter.trackReceiver(observation, relativeBaseline, trackingResult, ignoredSatellites);
						
						// Update baseline solution
						relativeTrackingFilter.updateEstimatedBaseline(relativeBaseline);
						
						// Send result to next module
						sendMessageToNextModule(new RLMessage(new Result(observation.remote.ID, relativeBaseline, observation.receiveEpoch, (double)confidence)));
					}
					finally { localizationLock.unlock(); }
				}
				break;
			default:
				break;
		}
	}
}
