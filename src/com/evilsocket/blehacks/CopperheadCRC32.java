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

import java.security.InvalidParameterException;

public class CopperheadCRC32
{
  private static final int CRCPOLY = 79764919;
  private static final int INITIAL_VALUE = -1;
  private int mValue = -1;

  public int getValue()
  {
    return this.mValue;
  }

  public void reset()
  {
    this.mValue = -1;
  }

  public void update(byte[] array)
    throws InvalidParameterException
  {
	  if (array.length % 4 != 0) {
          throw new InvalidParameterException("Length of data must be a multiple of 4");
      }
      for (int i = 0; i < array.length; ++i) {
          this.mValue ^= array[i ^ 0x3] << 24;
          for (int j = 0; j < 8; ++j) {
              if ((Integer.MIN_VALUE & this.mValue) != 0x0) {
                  this.mValue = (0x4C11DB7 ^ this.mValue << 1);
              }
              else {
                  this.mValue <<= 1;
              }
          }
      }
  }
}