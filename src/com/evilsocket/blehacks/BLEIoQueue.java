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

import java.util.LinkedList;
import java.util.Queue;

import com.evilsocket.blehacks.BLEIoOperation.Type;
import com.evilsocket.blehacks.Utils.Logger;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;

public class BLEIoQueue extends BluetoothGattCallback
{
	public interface QueueCallbacks
	{
	    public void onServicesDiscovered( BLEIoQueue queue, BluetoothGatt gatt, final int status);
	};
	
	private Queue<BLEIoOperation> _ops;
	private BluetoothGatt		  _gatt;
	private QueueCallbacks		  _callbacks;
	private BLEIoOperation		  _current;
	
	public BLEIoQueue( QueueCallbacks callbacks ) {
		_ops = new LinkedList<BLEIoOperation>();
		_callbacks = callbacks;
		_current = null;
	}
	
	public void add( BLEIoOperation op ) {
		_ops.add(op);
	}
	
	private void next()
	{
		BLEIoOperation op = _ops.poll();
		if( op == null ){
			Logger.d( "No I/O operations to execute." );
			return;
		}
		
		_current = op;
		
		boolean ok = false;

		BluetoothGattDescriptor desc = op.get_descriptor();
		BluetoothGattCharacteristic charact = op.get_characteristic();
		byte[] data = op.get_data();
		
		if( op.get_type() == Type.NOTIFY_START )
		{
			ok = _gatt.setCharacteristicNotification(charact, true);
			if( ok ){
				desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				ok = _gatt.writeDescriptor(desc);
			}
		}
		else if( op.get_type() == Type.WRITE_DESCRIPTOR )
		{			
			desc.setValue(data);
						
			ok = _gatt.writeDescriptor(desc);
		}
		else if( op.get_type() == Type.WRITE_CHARACTERISTICS )
		{			
			charact.setValue(data);
			
			Packet p = new Packet(charact.getValue());
			
			Logger.d( ">> " + p.toString() );
			
			ok = _gatt.writeCharacteristic(charact);
		}
		else if( op.get_type() == Type.READ_CHARACTERISTICS )
		{		
			ok = _gatt.readCharacteristic(charact);
		}
		else
		{
			Logger.e( "Unhandled Operation");
		}
		
		if( ok == false ){
			Logger.e( "Operation failed, fetching next ..." );
			next();
		}
	}
	
	@Override
    public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) { 
    	if( newState == BluetoothProfile.STATE_CONNECTED )
    	{
    		Logger.i( "Connected to GATT server, starting services discovery ..." );
    		
    		_gatt = gatt;
    		
    		gatt.discoverServices();    		
    	}
    	else if( newState == BluetoothProfile.STATE_DISCONNECTED )
    	{
    		Logger.w( "Disconnected." );
    	}
    }
			
	@Override
    public void onServicesDiscovered(final BluetoothGatt gatt, final int status) 
    { 
		_callbacks.onServicesDiscovered( this, gatt, status );	
		
		next();
    }
		
	@Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
				
		next();
	}
		
	@Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
		Packet p = new Packet( characteristic.getValue() );
				
		int op = ( p.getBuffer()[1] & 0xFF );
		
		if( op == 0xF4 || op == 0x15  )
		{
			// just session pings
		}
		else if( _current != null && _current.get_callback() != null )
		{
			_current.get_callback().onData(p);
		}
		else 
		{			
			Logger.w( "Unhandled Response: " + p.toString() );
		}		
		
		next();
    }	
}
