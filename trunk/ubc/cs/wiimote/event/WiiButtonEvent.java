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
 * Represents a wii button event. Not that not all buttons have been implemented (only A, B, 1 and 2).
 */
public class WiiButtonEvent extends WiiEvent {
	final static public int BUTTON_A = 8;
	final static public int BUTTON_B = 4;
	final static public int BUTTON_1 = 2;
	final static public int BUTTON_2 = 1;
	
	protected int pressedButtons;
	
	public WiiButtonEvent(Wiimote w, int buttonFlags) {
		super(w);
		pressedButtons = buttonFlags;
	}

	/**
	 * Returns true if the button state of the given event are identical to the button states of this event.
	 */
	public boolean equals(WiiButtonEvent e) {
		if (e==null)
			return false;
		return (isAPressed()==e.isAPressed() && isBPressed()==e.isBPressed() && is1Pressed()==e.is1Pressed() && is2Pressed()==e.is2Pressed());
	}
	
	/**
	 * Returns true if the A button was down when this event was created
	 */
	public boolean isAPressed() {
		return ((pressedButtons & BUTTON_A) != 0);
	}

	/**
	 * Returns true if the B button was down when this event was created
	 */
	public boolean isBPressed() {
		return ((pressedButtons & BUTTON_B) != 0);		
	}

	/**
	 * Returns true if the 1 button was down when this event was created
	 */
	public boolean is1Pressed() {
		return ((pressedButtons & BUTTON_1) != 0);
	}

	/**
	 * Returns true if the 2 button was down when this event was created
	 */
	public boolean is2Pressed() {
		return ((pressedButtons & BUTTON_2) != 0);		
	}
	
	public String toString() {
		return new String("WiiButtonEvent [A: "+isAPressed()+" B: "+isBPressed()+" 1: "+is1Pressed()+" 2: "+is2Pressed()+"]");
	}
}
