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
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import android.widget.TextView;

public class Utils 
{
	static class Logger
	{
		private static final String TAG = "BLEHACKS";
		private static TextView _logView = null;
		private static MainActivity _activity = null;
		
		public static void setLogView( MainActivity act, TextView v ) {
			_activity = act;
			_logView = v;
		}
		
		private static void log2view( final String m ) {
			if( _activity != null && _logView != null ){
				_activity.runOnUiThread(new Runnable() {
				     @Override
				     public void run() {
				    	 _logView.append( m + "\n" );
				     }
				});
			}
		}
		
		public static void e( String m ) {
			Log.e( TAG, m );
			log2view( "[ERROR] " + m );
		}
		
		public static void i( String m ) {
			Log.i( TAG, m );
			log2view( "[INFO] " + m );
		}
		
		public static void w( String m ) {
			Log.w( TAG, m );
			log2view( "[WARN] " + m );
		}
		
		public static void d( String m ) {
			//Log.d( TAG, m );
			//log2view( "[DEBUG] " + m );
		}
	}
	
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		if( bytes == null ){
			return "<null>";
		}
		
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    } 
	
	public static byte[] hexToBytes( String s ) {
		byte[] buffer = new byte[ s.length() / 2 ];
		
		for( int i = 0, j = 0; i < s.length(); i += 2, ++j ){
			buffer[j] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                     			+ Character.digit(s.charAt(i+1), 16));
		}
		
		return buffer;
	}
	
	private static Map<Integer, String> sSettingsCodeToStringMap;
	private static Map<String, Integer> sSettingsStringToCodeMap;
	    
    static 
    {
        final HashMap<String, Integer> hashMap = new HashMap<String, Integer>(50);
        
        hashMap.put("SERIAL_NUMBER", 0);
        hashMap.put("BAND_COLOR", 1);
        hashMap.put("BLE_ADDRESS", 2);
        hashMap.put("XTAL_TRIM", 3);
        hashMap.put("BLE_POWER_LEVEL", 4);
        hashMap.put("ADC_CAL_VCC_MV", 5);
        hashMap.put("ADC_CAL_VREFINT_CONV", 6);
        hashMap.put("ADC_CAL_BAT_RATIO", 7);
        hashMap.put("LED_ROW_BALANCE", 8);
        hashMap.put("LED_DOT_CORRECTION", 9);
        hashMap.put("ALS_BOOST", 10);
        hashMap.put("GOAL_0", 40);
        hashMap.put("GOAL_1", 41);
        hashMap.put("GOAL_2", 42);
        hashMap.put("GOAL_3", 43);
        hashMap.put("GOAL_4", 44);
        hashMap.put("GOAL_5", 45);
        hashMap.put("GOAL_6", 46);
        hashMap.put("TEMP_GOAL", 47);
        hashMap.put("FUEL", 48);
        hashMap.put("CALORIES", 49);
        hashMap.put("STEPS", 50);
        hashMap.put("24HRCLOCK", 56);
        hashMap.put("WEIGHT", 61);
        hashMap.put("HEIGHT", 62);
        hashMap.put("DOB", 63);
        hashMap.put("GENDER", 64);
        hashMap.put("ZONEOFFSET", 69);
        hashMap.put("DSTOFFSET", 70);
        hashMap.put("MOVE_REMINDER_HRS", 92);
        hashMap.put("HOURSWON", 90);
        hashMap.put("FIRST_NAME", 97);
        hashMap.put("HANDEDNESS", 65);
        hashMap.put("IN_SESSION_LED", 99);
        hashMap.put("MENU_CALORIES", 57);
        hashMap.put("MENU_FUELRATE", 60);
        hashMap.put("MENU_GOAL", 59);
        hashMap.put("MENU_STEPS", 58);
        hashMap.put("MENU_STARS", 89);
        hashMap.put("LIFETIME_FUEL", 94);
        hashMap.put("DISCOVERY_TOKEN", 75);
        
        sSettingsStringToCodeMap = (Map<String, Integer>)Collections.unmodifiableMap(hashMap);
        sSettingsCodeToStringMap = new HashMap<Integer, String>(sSettingsStringToCodeMap.size());
        
        for( String s : sSettingsStringToCodeMap.keySet() ) 
        {
            sSettingsCodeToStringMap.put( sSettingsStringToCodeMap.get(s), s);
        }
    }
    
    protected static String getSettingKey(final int n) {
        return sSettingsCodeToStringMap.get(n);
    }
    
    public static int getSettingCode(String name ){
    	return sSettingsStringToCodeMap.get(name);
    }
    
    public static Collection<Integer> getSettingCodes(){
    	return sSettingsStringToCodeMap.values();
    }
    
    protected static SettingType getSettingType(final int n) {
        switch (n) {
            default: {
                return SettingType.UINT32;
            }
            case 0:
            case 97: {
                return SettingType.STRING;
            }
            case 3:
            case 4:
            case 65: {
                return SettingType.BYTE;
            }
            case 2:
            case 8:
            case 9:
            case 10:
            case 75: {
                return SettingType.BYTE_ARRAY;
            }
            case 63: {
                return SettingType.DOB_CALENDAR;
            }
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 89: {
                return SettingType.BOOLEAN;
            }
            case 64: {
                return SettingType.GENDER;
            }
            case 69: {
                return SettingType.UINT32;
            }
            case 70: {
                return SettingType.BYTE;
            }
            case 99: {
                return SettingType.BOOLEAN;
            }
            case 62: {
                return SettingType.BYTE;
            }
            case 61: {
                return SettingType.UINT16;
            }
        }
    }
	    
	protected enum SettingType
    {
        BOOLEAN, 
        BYTE, 
        BYTE_ARRAY, 
        DOB_CALENDAR, 
        GENDER, 
        HOURS, 
        STRING, 
        UINT16, 
        UINT32;
    }
	
	public static void processGetSettingsResponse(byte[] raw) throws Exception {
		ByteBuffer byteBuffer = ByteBuffer.wrap(raw);
		
		// skip protocol first four bytes
		byte dummy1 = byteBuffer.get();
		byte dummy2 = byteBuffer.get();
		byte dummy3 = byteBuffer.get();
		byte dummy4 = byteBuffer.get();
		
        byte elements = byteBuffer.get();

        if (elements <= 0) {
            return;
        }

        for(byte b = 0; b < elements; ++b) 
        {
            if( byteBuffer.remaining() < 2 ) {
                throw new Exception("Decoding error, not enough remaining bytes");
            }
            
            byte configKey = byteBuffer.get();
            byte valueSize = byteBuffer.get();

            valueSize = (byte)( Math.min( (int)valueSize, byteBuffer.remaining() ) & 0xFF );
            
            final String settingKey = getSettingKey(configKey);
            if (settingKey == null) 
            {
            	Logger.e( "Unknown config key " + configKey );
                byteBuffer.position(valueSize + byteBuffer.position());
            }
            else 
            {
                switch (getSettingType(configKey)) 
                {
                    case BYTE: {
                    	Logger.i( "Got setting: " + settingKey + " = " + ( byteBuffer.get() & 0xFF ) );                        
                        break;
                    }
                    case BYTE_ARRAY: {
                        final byte[] array = new byte[valueSize];
                        byteBuffer.get(array);
                    	Logger.i( "Got setting: " + settingKey + " = " + Utils.bytesToHex(array) );                                                
                        break;
                    }
                    case STRING: {
                        final byte[] array2 = new byte[valueSize];
                        byteBuffer.get(array2);
                        byte b2;
                        for (b2 = 0; b2 < valueSize && array2[b2] != 0; ++b2) {}
                    	Logger.i( "Got setting: " + settingKey + " = " + new String(array2, 0, b2, Charset.forName("US-ASCII")) );                                                
                        break;
                    }
                    case UINT32: {
                    	Logger.i( "Got setting: " + settingKey + " = " + byteBuffer.getInt() );                        
                        break;
                    }
                    case UINT16: {
                    	Logger.i( "Got setting: " + settingKey + " = " + byteBuffer.getShort() );                        
                        break;
                    }
                    case BOOLEAN: {
                    	Logger.i( "Got setting: " + settingKey + " = " + ( byteBuffer.get() == 0 ? "false" : "true" ) );                        
                        break;
                    }
                    case DOB_CALENDAR: {
                        final int int1 = byteBuffer.getInt();
                    	Logger.i( "Got setting: " + settingKey + " = " + ( new GregorianCalendar(int1 & 0xFFFF, -1 + (0xFF & int1 >>> 16), int1 >>> 24) ) );                        
                        break;
                    }
                    case GENDER: {
                    	String gender = "UNKNOWN";
                        switch (byteBuffer.get()) {
                            case 77: {
                                gender = "MALE";
                                break;
                            }
                            case 70: {
                                gender = "FEMALE";
                                break;
                            }
                        }
                    	Logger.i( "Got setting: " + settingKey + " = " + gender );                        

                        break;
                    }
                    
                    default:
                    	
                    	throw new Exception("Unknown config type");
                }
            }
        }
    }
}
