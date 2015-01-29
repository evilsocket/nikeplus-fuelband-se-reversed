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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class CopperheadPacket
{
  public static final int MAX_PACKET_SIZE = 63;
  private static final String MSG_INVALID_BUFFER_SIZE = "Invalid packet buffer size";
  private static final String TAG = "CopperheadPacket";
  private static final String ZONE = "LogicalLink";
  private static byte mTag = 32;
  private byte[] mBuffer;

  public CopperheadPacket(int size)
  {
    this.mBuffer = new byte[size];
    this.mBuffer[0] = ((byte)(size - 1));
    setTag(incrementTag());
  }

  public CopperheadPacket(byte[] buffer) throws Exception
  {
    this.mBuffer = buffer;
    if ((buffer.length < 3) || (getSize() > 63))
    {
      StringBuilder localStringBuilder = new StringBuilder().append("Invalid packet buffer size: length = ").append(buffer.length);
      if (buffer.length > 0);
      for (String str = " : size = " + getSize(); ; str = "")
        throw new Exception(str);
    }
    int i = getSize();
    if (i > -1 + buffer.length)
      throw new Exception("Invalid packet buffer size: length = " + buffer.length + " : size = " + i);
  }

  public byte[] getData()
  {
    return this.mBuffer;
  }

  public int getNotificationCode()
  {
    return this.mBuffer[2];
  }

  public byte[] getPayload(int paramInt) throws Exception
  {
    int i = getPayloadSize();
    int j = paramInt + 3;
    if (j > i + 3)
      throw new Exception("Invalid packet buffer size");
    return Arrays.copyOfRange(this.mBuffer, j, i + 3);
  }

  public ByteBuffer getPayloadBuffer()
  {
    ByteBuffer localByteBuffer = ByteBuffer.wrap(this.mBuffer, 3, -3 + this.mBuffer.length);
    localByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    return localByteBuffer;
  }

  public int getPayloadSize()
  {
    return -2 + (this.mBuffer[0] & 0xFF);
  }

  public int getResponseStatus()
  {
    return this.mBuffer[2];
  }

  public int getSize()
  {
    return (this.mBuffer[0] & 0xFF);
  }

  public int getTag()
  {
    return this.mBuffer[1];
  }

  protected byte incrementTag()
  {
    mTag = (byte)(1 + mTag);
    if (mTag == -1)
      mTag = 0;
    return mTag;
  }

  public void setOpcode(byte paramByte)
  {
    this.mBuffer[2] = paramByte;
  }

  public void setTag(byte paramByte)
  {
    this.mBuffer[1] = paramByte;
  }

  public void writePayloadTo(ByteArrayOutputStream paramByteArrayOutputStream, int paramInt) throws Exception
  {
    int i = getPayloadSize();
    int j = paramInt + 3;
    if ((j >= i + 3) || (j >= this.mBuffer.length))
      throw new Exception("Invalid packet buffer size");
    paramByteArrayOutputStream.write(this.mBuffer, j, i - paramInt);
  }
}