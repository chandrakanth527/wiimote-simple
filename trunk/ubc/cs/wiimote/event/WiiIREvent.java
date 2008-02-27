/*
	Copyright 2008 Garth Shoemaker
	
 	This file is part of Wiimote Simple.

    Wiimote Simple is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Wiimote Simple is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Wiimote Simple.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.ubc.cs.wiimote.event;

import ca.ubc.cs.wiimote.Wiimote;

/**
 * Represents an IR event. These are only fired if an IR source has been detected by
 * the wiimote.
 */
public class WiiIREvent extends WiiEvent {
	
	///The index of the light source for the given event. The wiimote tries to keep track of different light sources over time.
	int lightSource;
	
	/// The x position of the light source, from 0-1023.
	int x;
	/// The y position of the light source, from 0-767.
	int y;
	
	///The size of the light source.
	int size;
	
	public WiiIREvent(Wiimote wiimote, int lightSource, int x, int y, int size) {
		super(wiimote);
		
		this.lightSource = lightSource;
		this.x = x;
		this.y = y;
		this.size = size;
	}
	
	///Returns the index of the current light source, as determined by the wiimote
	public int getLightSource() {
		return lightSource;
	}
	
	///Returns the x position of the light source, from 0-1023.
	public int getX() {
		return x;
	}
	
	///Returns the y position of the light source, from 0-767
	public int getY() {
		return y;
	}
	
	///Returns the size of the light source.
	public int getSize() {
		return size;
	}
	
	public String toString() {
		return new String("WiiREvent [" + wiimote.getLight() + " "+ getX() + " " + getY() + " " + getSize() + "]");
	}
}
