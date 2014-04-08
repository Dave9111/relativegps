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

import edu.vu.isis.regtrack.common.RawUbxDataType;

public final class UbxDecoder
{
	private final RxmDecoder rxmDecoder = new RxmDecoder();
	private final NavDecoder navDecoder = new NavDecoder();
	private final EphDecoder ephDecoder = new EphDecoder();
	
	public UbxDecoder() {}
	
	public RawUbxDataType decode(final byte[] dataPacket)
	{
		switch (dataPacket[0])
		{
			case 0x01:	// NAV Class
				switch (dataPacket[1])
				{
					case 0x06:		// NAV-SOL ID
						return navDecoder.decodeSolution(dataPacket);
					case 0x22:		// NAV-CLOCK ID
						return navDecoder.decodeClock(dataPacket);
					default:
						break;
				}
				break;
			case 0x02:	// RXM Class
				switch (dataPacket[1])
				{
					case 0x10:		// RXM-RAW ID
						return rxmDecoder.decode(dataPacket);
					case 0x11:		// RXM-SFRB ID
						return ephDecoder.decode(dataPacket);
					default:
						break;
				}
				break;
			default:
				break;
		}

		return null;
	}
	
	// Checksum
	public static boolean checksum(final byte[] dataPacket, int length, byte CK_A_RESULT, byte CK_B_RESULT)
	{
		// If no checksum present, simply return true
		if ((CK_A_RESULT == 0) && (CK_B_RESULT == 0))
			return true;
		
		byte CK_A = 0, CK_B = 0;

		for (int i = 0; i < length; ++i)
		{
			CK_A += dataPacket[i];
			CK_B += CK_A;
		}

		return ((CK_A == CK_A_RESULT) && (CK_B == CK_B_RESULT));
	}
}
