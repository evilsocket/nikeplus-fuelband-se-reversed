/*
 * Copyleft of Simone Margaritelli aka evilsocket <evilsocket@gmail.com>
 * http://www.evilsocket.net/
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */
package com.evilsocket.blehacks;

import android.os.Bundle;
import android.os.ParcelUuid;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class AdvertisementData {
	public static final String KEY_COMPANY_CODE = "COMPANYCODE";
	public static final String KEY_LOCAL_NAME = "LOCALNAME";
	public static final String KEY_MANUFACTURER_DATA = "MANUDATA";
	public static final String KEY_SERVICE_LIST = "SERVICES";

	public static Bundle parse(byte[] scanRecord) {
		Bundle parsed = new Bundle();
		ByteBuffer buffer = ByteBuffer.wrap(scanRecord);
		
		while (true) 
		{
			int i = buffer.remaining() > 0 ? buffer.get() : 0;

			
			if( i == 0 || i > buffer.remaining() )
				break;
			
			int j = buffer.get();
			byte[] arrayOfByte = new byte[i - 1];
			buffer.get(arrayOfByte);
			
			switch (j) {
			default:
				break;
			case -1:
				parsed.putByteArray("COMPANYCODE",Arrays.copyOfRange(arrayOfByte, 0, 2));
				parsed.putByteArray("MANUDATA",Arrays.copyOfRange(arrayOfByte, 2, arrayOfByte.length));
				break;
			case 6:
			case 7:
				parsed.putParcelableArrayList("SERVICES",parseUuids(arrayOfByte));
				break;
			case 9:
				parsed.putString("LOCALNAME", new String(arrayOfByte));
			}
		}
		
		return parsed;
	}

	private static ArrayList<ParcelUuid> parseUuids(byte[] paramArrayOfByte) {
		ByteBuffer localByteBuffer = ByteBuffer.wrap(paramArrayOfByte);
		localByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		ArrayList localArrayList = new ArrayList();
		while (localByteBuffer.remaining() >= 16) {
			long l = localByteBuffer.getLong();
			localArrayList.add(new ParcelUuid(new UUID(localByteBuffer
					.getLong(), l)));
		}
		return localArrayList;
	}
}
