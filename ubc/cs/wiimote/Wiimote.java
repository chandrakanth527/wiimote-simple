
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

package ca.ubc.cs.wiimote;

import java.io.IOException;
import java.util.*;
import javax.bluetooth.*;
import javax.microedition.io.Connector;

import ca.ubc.cs.wiimote.event.*;

import java.nio.ByteBuffer;

/*
 * Represents a single wiimote that has been connected to. This will normally be
 * provided for you by a WiimoteDiscoverer. Once you have it you can register a WiimoteListener
 * to receive Wiimote events.
 */
public class Wiimote {
	
	String address;
	
	protected LinkedList<WiimoteListener> listeners;
	
    protected L2CAPConnection sendCon;
    protected L2CAPConnection receiveCon;
    boolean connectionOpen;
	protected int light;
    protected Wiimote wiimote;
    protected WiiButtonEvent lastButtonEvent;
    
    final protected static byte COMMAND_LIGHT = 0x11;
	final protected static byte COMMAND_IR = 0x13;
	final protected static byte COMMAND_IR_2 = 0x1a;
	final protected static byte COMMAND_REGISTER = 0x16;
	final protected static byte COMMAND_REPORTING = 0x12;
	final protected static byte COMMAND_READ_CALIBRATION = 0x17;
	
	double[] calibrationZero, calibrationOne;
	Thread commandListener;
	static Object gate = new Object();
	boolean inited = false;
	
	/**
	 * Creates a Wiimote instance given an address. Forms connections to the wiimote, and
	 * readies sending and receiving of data. You likely won't call this yourself. Instead,
	 * your Wiimote instances will be created by WiimoteDiscoverer
	 */
	public Wiimote(String a) throws Exception {
		connectionOpen = false;
		calibrationZero = new double[3];
		calibrationOne = new double[3];
		
		listeners = new LinkedList<WiimoteListener>();
		light = -1;
		wiimote = this;
		address = "btl2cap://"+a;
		
		try {
			//these sometimes take very long to open, at least for me. This is the primary
			//reason that we need to fail elegantly should they not open.
			receiveCon = (L2CAPConnection)Connector.open(address+":13", Connector.READ, true);
			sendCon = (L2CAPConnection)Connector.open(address+":11", Connector.WRITE, true);
			connectionOpen = true;
		}
		catch (Exception e) {
			if (sendCon!=null)
				sendCon.close();
			if (receiveCon!=null)
				receiveCon.close();
			System.out.println("Could not open. Connections reset.");
			throw e;
		}
		commandListener = new CommandListener();
		commandListener.start();
		readCalibration();
	}

	/**
	 * Attaches a WiimoteListener to this wiimote. The wiimote will send events to the
	 * given listener whenever somethign happens (e.g. button is pressed, accelerometer event occurs)
	 */
	public void addListener(WiimoteListener l) {
		if (!listeners.contains(l))
			listeners.add(l);
	}
	
	/**
	 * Detaches the given WiimoteListener.
	 */
	public void removeListener(WiimoteListener l) {
		listeners.remove(l);
	}
	
	/**
	 * Dispatches wiimote events to all the WiimoteListeners.
	 */
	protected void dispatchEvent(WiiEvent e) {
		for (ListIterator<WiimoteListener> it = listeners.listIterator(); it.hasNext();) {
			WiimoteListener listener = it.next();
			if (e instanceof WiiButtonEvent)
				listener.wiiButtonChange((WiiButtonEvent)e);
			if (e instanceof WiiIREvent)
				listener.wiiIRInput((WiiIREvent)e);
			if (e instanceof WiiAccelEvent)
				listener.wiiAccelInput((WiiAccelEvent)e);
		}
	}
	
	/**
	 * sends the necessary commands to the wiimote to enable IR events
	 */
	public void enableIREvents() {
		sendCommand(COMMAND_REPORTING, new byte[] {0x04, 0x33});
		
		//http://wiibrew.org/index.php?title=Wiimote#Memory_and_Registers
		
		try {
			sendCommand(COMMAND_IR, new byte[] {0x04});
			Thread.sleep(100);
			sendCommand(COMMAND_IR_2, new byte[] {0x04});
			Thread.sleep(100);		
					
			writeToRegister(0xb00030, new byte[] {0x08});
			Thread.sleep(100);
	        //write sensitivity block 1
			writeToRegister(0xb00000, new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0x90, 0x00, (byte)0xC0});
			Thread.sleep(100);
			//write sensitivity block 2
			writeToRegister(0xb0001a, new byte[] {0x40, 0x00});
	        //write mode # (1=basic 3=extended 5=full)
			Thread.sleep(100);
			writeToRegister(0xb00033, new byte[] {(byte)3});
			Thread.sleep(100);
			writeToRegister(0xb00030, new byte[] {0x08});
		} catch (Exception e) {System.out.println("enableIREvents exception: " + e);}

	}
    
	/**
	 * Not yet implemented
	 */
	public void disableIREvents() {
		
	}
	
	/**
	 * Turns on the given light on the wiimote. Lights are indexed from 0.
	 */
	public void setLight(int i) {
		if (i<0 || i>3)
			return;
		light = i;
		sendCommand(COMMAND_LIGHT, new byte[] {(byte)Math.pow(2, 4+i)});
	}
	
	/**
	 * Returns which light is turned on on this wiimote
	 */
	public int getLight() {
		return light;
	}
	
	/**
	 * Causes the wiimote to vibrate for the given number of milliseconds.
	 */
	public void vibrate(final int millis) {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				sendCommand(COMMAND_LIGHT, new byte[] {(byte)(0x01 | (byte)(Math.pow(2, 4+light)))});
				try {
					Thread.sleep(millis);
				} catch (Exception e) {
					System.out.println("vibrate exception: " + e);
				}
				sendCommand(COMMAND_LIGHT, new byte[] {(byte)(Math.pow(2, 4+light))});
			}
		});
		thread.start();
	}
	
	/**
	 * Causes the given data to be written to the given register address on the wiimote.
	 */
	private void writeToRegister(int address, byte[] data) {
		byte[] message = new byte[21];
		
		message[0] = 0x04;
		
		message[1] = (byte)(address >> 16 & 0xff);
    	message[2] = (byte)(address >> 8 & 0xff);
    	message[3] = (byte)(address & 0xff);    	
    	
		message[4] = (byte)data.length;
		for (int i=0; i<data.length; i++) {
			message[5+i] = data[i];
		}
		sendCommand(COMMAND_REGISTER, message);
	}
	
	/**
	 * Sends message to the wiimote asking for accelerometer calibration data.
	 */
	synchronized private void readCalibration() {
		try {
			byte[] message = new byte[] {0x00, 0x00, 0x00, 0x16, 0x00, 0x08};
			sendCommand(COMMAND_READ_CALIBRATION, message);
		} catch (Exception e) {
			System.out.println("readCalibration exception: " + e);
		}
	}

	/**
	 * Sends a generic command to the wiimote.
	 */
	synchronized private void sendCommand(byte command, byte[] payload) {
		try {
			byte[] message = new byte[2+payload.length];
			message[0] = 82;
			message[1] = command;
			System.arraycopy(payload, 0, message, 2, payload.length);
						
			sendCon.send(message);
		}
		catch(IOException ioexception) {
			System.out.println("error sending data " + ioexception);
		}
	}
	
	/**
	 * Causes the wiimote connections to close.
	 */
	protected void finalize() throws Throwable {
		cleanup();
	}
	
	/**
	 * Closes any open wiimote connections.
	 */
	public void cleanup() {
		synchronized (receiveCon) { 
			connectionOpen = false;
			try {
				if (sendCon!=null) {
					sendCon.close();
				}
				if (receiveCon!=null) {
					receiveCon.close();
				}
			} catch (Exception e) {System.out.println("cleanup exception " + e);}		
		}
	}
	
	/**
	 * This loops infinitely, constantly listenign for data from the wiimote. When
	 * data is received it responds accordingly.
	 */
	protected class CommandListener extends Thread {
		
		public void run() {
			while (true) {
				byte[] bytes = new byte[32];
					if (connectionOpen==true) {
						try {
							receiveCon.receive(bytes); //this blocks until data ready
						}
						catch (IOException e) {}
						catch (Exception e) {
							System.out.println("wiimote " + receiveCon+ " "+ light + " exception: receive(bytes) " + e);
						}
												
						switch (bytes[1]) {
						case 0x21: //read data
							parseCalibrationResponse(ByteBuffer.allocate(8).put(bytes, 7, 8));
							break;
						case 0x30: //buttons only
							createButtonEvent(ByteBuffer.allocate(2).put(bytes, 2, 2));
							break;
						case 0x33: //buttons -> accel -> IR
							createButtonEvent(ByteBuffer.allocate(2).put(bytes, 2, 2));
							createAccelEvent(ByteBuffer.allocate(5).put(bytes, 2, 5));
							createIREvent(ByteBuffer.allocate(12).put(bytes, 7, 12));
							break;
						}
					} else {
						try {
							Thread.sleep(100);
						} catch (Exception e) {e.printStackTrace();}
					}
				}
		}
		
		/**
		 * When a response to an accelerometer calibration request is received this
		 * parses it.
		 */
		protected void parseCalibrationResponse(ByteBuffer b) {
			calibrationZero[0] = ((b.get(0) & 0xFF) << 2) + (b.get(3) & 3);
			calibrationZero[1] = ((b.get(1) & 0xFF) << 2) + ((b.get(3) & 0xC) >> 2);
			calibrationZero[2] = ((b.get(2) & 0xFF) << 2) + ((b.get(3) & 0x30) >> 4);
			
			calibrationOne[0] = ((b.get(4) & 0xFF) << 2) + (b.get(7) & 3);
			calibrationOne[1] = ((b.get(5) & 0xFF) << 2) + ((b.get(7) & 0xC) >> 2);
			calibrationOne[2] = ((b.get(6) & 0xFF) << 2) + ((b.get(7) & 0x30) >> 4);		
		}
		
		/**
		 * Creates a WiiButtonEvent from raw data
		 */
		protected void createButtonEvent(ByteBuffer b) {			
			int i = (b.get(0) << 8) | b.get(1);
			WiiButtonEvent event = new WiiButtonEvent(wiimote, i);
			if (!event.equals(lastButtonEvent)) {
				lastButtonEvent = event;
				wiimote.dispatchEvent(event);
			}
		}
		
		/**
		 * Creates a WiiAccelEvent from raw data
		 */
		protected void createAccelEvent(ByteBuffer b) {
			int x = ((b.get(2) & 0xff) << 2) + ((b.get(0) & 0x60) >> 5);
			int y = ((b.get(3) & 0xff) << 2) + ((b.get(1) & 0x60) >> 5);
			int z = ((b.get(4) & 0xff) << 2) + ((b.get(1) & 0x80) >> 6);
			
			double xaccel = ((double)x-calibrationZero[0])/(calibrationOne[0]-calibrationZero[0]);
			double yaccel = ((double)y-calibrationZero[1])/(calibrationOne[1]-calibrationZero[1]);
			double zaccel = ((double)z-calibrationZero[2])/(calibrationOne[2]-calibrationZero[2]);
		
			WiiAccelEvent event = new WiiAccelEvent(wiimote, xaccel, yaccel, zaccel);
			wiimote.dispatchEvent(event);
		}
		
		/**
		 * Creates a WiiIREvent from raw data
		 */
		protected void createIREvent(ByteBuffer b) {
			for (int i=0; i<b.limit(); i+=3) {
				int x = (b.get(i) & 0xFF) + ((b.get(i+2) & 0x30) << 4);
				int y = (b.get(i+1) & 0xFF) + ((b.get(i+2) & 0xc0) << 2);
				int size = (b.get(i+2)& 0xF);
				if (x!=1023 && y!=1023) {
					WiiIREvent event = new WiiIREvent(wiimote, i, x, y, size);
					wiimote.dispatchEvent(event);
				}
			}
		}
	}
}
