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

package edu.vu.isis.regtrack.framework;

import edu.vu.isis.messaging.RLFramework;
import edu.vu.isis.messaging.RLInterface;
import edu.vu.isis.messaging.RLModule;
import edu.vu.isis.regtrack.modules.dataaggregator.DataAggregator;
import edu.vu.isis.regtrack.modules.localization.RelativeLocalizer;
import edu.vu.isis.regtrack.modules.networkmanager.NetworkManager;
import edu.vu.isis.regtrack.modules.preprocessor.PreProcessor;
import edu.vu.isis.regtrack.modules.ubxmanager.UbxManager;

public final class RegLocFramework extends RLFramework
{
	private final String receiverID;
	private final UbxManager ubxManagerModule;
	
	public RegLocFramework(String rcvrID)
	{
		super();
		
		receiverID = rcvrID;
		
		// Fetch desired modules
		ubxManagerModule = new UbxManager("UbxManager", receiverID);
		RLModule ubxManager = createModule("UbxManager", ubxManagerModule);
		RLModule preProcessor = createModule("PreProcessor", new PreProcessor("PreProcessor", receiverID));
		RLModule dataAggregator = createModule("DataAggregator", new DataAggregator("DataAggregator", receiverID));
		RLModule networkManager = createModule("NetworkManager", new NetworkManager("NetworkManager", receiverID));
		RLModule relativeLocalizer = createModule("LocalizationManager", new RelativeLocalizer("RelativeLocalizer"));
		RLInterface networkInterface = createInterface("NetworkInterface");
		RLInterface serialInterface = createInterface("SerialInterface");
		RLInterface outputInterface = createInterface("OutputInterface");

		// Make connections
		serialInterface.addOutgoingModule(ubxManager);
		ubxManager.addOutgoingModule(preProcessor);
		preProcessor.addOutgoingModule(dataAggregator);
		networkInterface.addOutgoingModule(networkManager);
		networkManager.addOutgoingModule(dataAggregator);
		dataAggregator.addOutgoingModule(networkManager);
		networkManager.addOutgoingModule(networkInterface);
		dataAggregator.addOutgoingModule(relativeLocalizer);
		relativeLocalizer.addOutgoingModule(outputInterface);
	}
	
	public void enableLogging(boolean isLoggingEnabled) { ubxManagerModule.enableLogging(isLoggingEnabled); }
}
