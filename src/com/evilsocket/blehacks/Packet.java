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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Packet 
{
	private byte[] _buffer;
	
	public Packet( int size ){
		_buffer = new byte[size];
		_buffer[0] = 0x00;
	}
	
	public Packet( byte[] buffer ){
		_buffer = buffer;
	}
	
	public static Packet wrap( CopperheadPacket cpacket ) {
		byte[] payload = cpacket.getData();
		
		Packet packet = new Packet( payload.length + 1 );
		
		packet.setPayload( payload );
		
		return packet;
	}
	
	public byte[] getBuffer() {
		return _buffer;
	}
	
	public ByteBuffer getBuffered( ByteOrder order ) {
		ByteBuffer b = ByteBuffer.wrap(_buffer);
		b.order( order );
		return b;
	}
	
	public String toString(){
		String s = "";
		
		s += "[" + getProtocolLayer() + "] ";
		s += "COUNT=" + getPacketCount() + " ";
		s += "INDEX=" + getPacketIndex() + " ";
		s += "SEQN=" + getSequenceNumber() + " : ";
		
		char[] HEX = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[( _buffer.length - 1 ) * 2];
        for ( int j = 1; j < _buffer.length; j++ ) {
            int v = _buffer[j] & 0xFF;
            hexChars[(j - 1) * 2] = HEX[v >>> 4];
            hexChars[(j - 1) * 2 + 1] = HEX[v & 0x0F];
        }
        
        s += new String(hexChars);
        
		return s;
	}
	
    public int getPacketCount() {
        return 0x3 & _buffer[0];
    }
    
    public int getPacketIndex() {
        return 0x3 & _buffer[0] >>> 2;
    }
    
    public CommandResponseOperation.ProtocolLayer getProtocolLayer() {
        if( (0x80 & _buffer[0]) != 0x0 ) {
            return CommandResponseOperation.ProtocolLayer.SESSION;
        }
        else {
        	return CommandResponseOperation.ProtocolLayer.COMMAND;
        }
    }
    
    public int getSequenceNumber() {
        return 0x7 & _buffer[0] >>> 4;
    }
    
    public boolean isFinalPacket() {
        return this.getPacketIndex() == this.getPacketCount();
    }
	
    public void setProtocolLayer( CommandResponseOperation.ProtocolLayer layer ) {
        if( layer == CommandResponseOperation.ProtocolLayer.SESSION) 
        {
        	_buffer[0] |= (byte)128;           
        }
        else
        {
        	_buffer[0] &= (byte)(-129);    
        }        
    }
    
    public void setCommandBytes( byte a, byte b ) {
    	_buffer[1] = a;
    	_buffer[2] = b;
    }
    
    public void setSequenceNumber(int n) {
        _buffer[0] |= (byte)((n & 0x7) << 4);
    }
    
    public void setPacketIndex(int n) {
        _buffer[0] &= 0xFFFFFFF3;
        _buffer[0] |= (byte)((n & 0x3) << 2);
    }
    
    public void setPacketCount(int n) {
        _buffer[0] &= 0xFFFFFFFC;
        _buffer[0] |= (byte)(n & 0x3);
    }
    
    public void setPayload( byte[] payload ) {
        System.arraycopy( payload, 0, _buffer, 1,  payload.length );
    }
}
