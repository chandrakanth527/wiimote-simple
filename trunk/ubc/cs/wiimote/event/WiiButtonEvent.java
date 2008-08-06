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
	public enum Button {B_A, B_B, B_1, B_2, B_C, B_Z};
	
	protected Button button;
	protected boolean pressedEvent;
	
	public WiiButtonEvent(Wiimote w, Button b, boolean pE) {
		super(w);
		button = b;
		pressedEvent = pE;
	}

	public Button getButton() {
		return button;
	}
	
	public boolean getWasPress() {
		return pressedEvent;
	}
	
	public String toString() {
		return new String("WiiButtonEvent");
	}
}
