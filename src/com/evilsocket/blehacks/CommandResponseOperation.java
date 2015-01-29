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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CommandResponseOperation
{
	public static enum ProtocolLayer {
		COMMAND, SESSION
	}

	private static final int MAX_BUFFER_SIZE = 64;
	private static final String TAG = CommandResponseOperation.class.getSimpleName();
	private static final String ZONE = "LogicalLink";
	protected byte[] mCommandBuffer;
	protected ProtocolLayer mLayer = ProtocolLayer.COMMAND;
	private final String mMarker;
	private ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream(64);

	public CommandResponseOperation(String paramString) {
		if ((paramString == null) || (paramString.trim().length() == 0))
			throw new IllegalArgumentException("Marker cannot be null or empty");
		this.mMarker = paramString;
	}

	public byte[] getCommandData() {
		return (byte[]) this.mCommandBuffer.clone();
	}

	public ProtocolLayer getProtocolLayer() {
		return this.mLayer;
	}

	public byte[] getResponseData() {
		return this.responseBuffer.toByteArray();
	}

	public void setCommandData(byte[] paramArrayOfByte) {
		this.mCommandBuffer = paramArrayOfByte;
	}

	public void setProtocolLayer(ProtocolLayer paramProtocolLayer) {
		this.mLayer = paramProtocolLayer;
	}

	public String toString() {
		StringBuilder localStringBuilder = new StringBuilder();
		localStringBuilder.append("CommandResponseOperation ");
		localStringBuilder.append(this.mMarker);
		localStringBuilder.append(" protocol = ");
		if (getProtocolLayer() == ProtocolLayer.COMMAND)
			;
		for (String str = "COMMAND";; str = "SESSION") {
			localStringBuilder.append(str);
			return localStringBuilder.toString();
		}
	}

	public void writeResponseData(ByteArrayOutputStream paramByteArrayOutputStream) throws IOException {
		paramByteArrayOutputStream.writeTo(this.responseBuffer);
	}

	public void writeResponseData(byte[] paramArrayOfByte) throws IOException {
		this.responseBuffer.write(paramArrayOfByte);
	}

}