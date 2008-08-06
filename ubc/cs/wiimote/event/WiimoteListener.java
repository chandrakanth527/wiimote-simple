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

/**
 * Implement this and register with a Wiimote instance in order to receive updates from
 * the wiimotes.
 */
public interface WiimoteListener {
	
	public void wiiButtonPress(WiiButtonEvent e);

	public void wiiButtonRelease(WiiButtonEvent e);

	///Is called to notify the listener of IR events. Is called continuously as long as at least one IR source is visible to the wiimote.
	public void wiiIRInput(WiiIREvent e);
	
	///Is called to notify the listener of acceleration events. Is called continuously.
	public void wiiAccelInput(WiiAccelEvent e);

	public void wiiNunchukAccelInput(WiiAccelEvent e);
	
	public void wiiNunchukJoystickInput(WiiNunchukJoystickEvent e);
}
