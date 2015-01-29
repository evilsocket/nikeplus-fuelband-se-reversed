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

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

public class BLEIoOperation 
{
	public enum Type
	{
		NOTIFY_START,
		NOTIFY_STOP,
		READ_CHARACTERISTICS,
		WRITE_CHARACTERISTICS,
		READ_DESCRIPTOR,
		WRITE_DESCRIPTOR
	};
	
	public interface OnResponseCallback
	{
		void onData( Packet packet );
	};
	
	private Type 						_type;
	private BluetoothGattCharacteristic _characteristic = null;
	private BluetoothGattDescriptor     _descriptor = null;
	private byte[]					    _data = null;
	private String					    _description;
	private OnResponseCallback			_callback = null;
	
	public BLEIoOperation( Type type, String description ){
		_type = type;
		_description = description;
	}
	
	public BLEIoOperation( Type type, String description, OnResponseCallback callback ){
		_type = type;
		_description = description;
		_callback = callback;
	}
	
	public String toString() {
		return "[OP] " + _description;
	}
		
	public Type get_type() {
		return _type;
	}
	public void set_type(Type _type) {
		this._type = _type;
	}
	public BluetoothGattCharacteristic get_characteristic() {
		return _characteristic;
	}
	public void set_characteristic(BluetoothGattCharacteristic _characteristic) {
		this._characteristic = _characteristic;
	}
	public BluetoothGattDescriptor get_descriptor() {
		return _descriptor;
	}
	public void set_descriptor(BluetoothGattDescriptor _descriptor) {
		this._descriptor = _descriptor;
	}
	public String get_description() {
		return _description;
	}
	public void set_description(String _description) {
		this._description = _description;
	}

	public byte[] get_data() {
		return _data;
	}

	public void set_data(byte[] _data) {
		this._data = _data;
	}

	public OnResponseCallback get_callback() {
		return _callback;
	}

	public void set_callback(OnResponseCallback _callback) {
		this._callback = _callback;
	}
}
