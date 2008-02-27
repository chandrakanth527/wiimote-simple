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

import javax.bluetooth.*;
import java.util.*;

/**
 * This class is used to perform searches for wiimotes. When it finds a wiimote
 * it creates a Wiimote instance and notifies any listeners.
 */
public class WiimoteDiscoverer extends Thread implements DiscoveryListener{
	//stores previously found wiimotes so that we don't try to connect to the same one twice
	static LinkedList<String> discoveredWiimoteAddresses = new LinkedList<String>();
	static WiimoteDiscoverer discoverer;
	
	//stores the address of a newly found wiimote
	String discoveredAddress;
	DiscoveryAgent agent;
	
	Object lock;
	
	//stores the live connections so we can close them later
	LinkedList<WiimoteDiscoveryListener> listeners;
	
	protected WiimoteDiscoverer() {
		listeners = new LinkedList<WiimoteDiscoveryListener>();
		try {
			LocalDevice device = LocalDevice.getLocalDevice();
			agent = device.getDiscoveryAgent();
			lock = new Object();
		}
		catch (BluetoothStateException e) {
			System.out.println("BluetoothStateException. Exiting. " + e);
			System.exit(0);
		}
		catch (Exception e) {System.out.println("exception " + e);}
	}
	
	/**
	 * Adds a WiimoteDiscoveryListener to the list of listeners. This listener
	 * will be notified whenver a Wiimote is discovered and connected to.
	 */
	public void addWiimoteDiscoveryListener(WiimoteDiscoveryListener l) {
		if (!listeners.contains(l))
			listeners.add(l);
	}
	
	/**
	 * Removes the given WiimoteDiscoveryListener from the list of listeners.
	 */
	public void removeWiimoteDiscoveryListener(WiimoteDiscoveryListener l) {
		listeners.remove(l);
	}
	
	/**
	 * Returns an instance of WiimoteDiscoverer. This is how you should get one for use.
	 */
	synchronized static public WiimoteDiscoverer getWiimoteDiscoverer() {
		if (discoverer==null)
			discoverer = new WiimoteDiscoverer();
		
		return discoverer;
	}
	
	/**
	 * Notifies listeners that a wiimote has been discovered and connected to.
	 */
	protected void notifyListeners(Wiimote wiimote) {
		for (ListIterator<WiimoteDiscoveryListener> it = listeners.listIterator(); it.hasNext(); ) {
			it.next().wiimoteDiscovered(wiimote);
		}
	}
	
	/**
	 * This runs while wiimotes are being searched for. It is initiated by a call to startWiimoteSearch().
	 * It continusously finds wiimotes and notifies any listeners until stopWiimoteSearch() is called.
	 */
	public void run() {
		System.out.println("Wiimote Discovery Started");
		Wiimote wiimote = null;
		try {
			while (true && !isInterrupted()) {
				do {
					System.out.println("Press 1 & 2 on your Wiimote now");
					agent.startInquiry(DiscoveryAgent.GIAC, this);
					synchronized (lock) {
						lock.wait();
					}
				} while (discoveredAddress==null);
				try {
					if (discoveredAddress!=null) {
						wiimote = new Wiimote(discoveredAddress);
						System.out.println("Connected.");
						notifyListeners(wiimote);
						wiimote = null;
						discoveredWiimoteAddresses.add(discoveredAddress);
						discoveredAddress = null;
					}
				} catch (Exception e) {
		    		discoveredAddress = null;
		    		discoveredWiimoteAddresses.remove(discoveredAddress);
		    		System.out.println("Connection failed. Try again. " + e);
				}
			}
		}
		catch (InterruptedException e) {}
		catch (BluetoothStateException bse) {
			System.err.println("Error Starting Wiimote Discovery");
			bse.printStackTrace();
		}
		finally {
			if (wiimote!=null)
				wiimote.cleanup();
		}
		System.out.println("Wiimote Discovery Stopped");
	}
	
	/**
	 * Starts a search for Wiimotes. The search will continue until
	 * stopWiimoteSearch is called.
	 */
	public void startWiimoteSearch() {
		if (!isAlive())
			start();
	}
	
	/**
	 * Stops a search for Wiimotes. Call this if you are done connecting with wiimotes.
	 */
	public void stopWiimoteSearch() {
		if (isAlive()) {
			interrupt();
		}
	}
	
	/**
	 * Is called by a JSR-82 implementation when a Bluetooth inquiry is complete. This occurs either after a Wiimote
	 * has been found, or after the Bluetooth implementation has given up. It should
	 * not be necessary to call this.
	 */
	public void inquiryCompleted(int i) {
		//findWiimote function is waiting for us. Let's let it go
		synchronized(lock) {
			lock.notify();
		}
	}
	
	/**
	 * Is called by a JSR-82 implementation when a device has been discovered. It should
	 * not be necessary to call this.
	 */
	public void deviceDiscovered(RemoteDevice remotedevice, DeviceClass deviceclass) {
		String name = null;
		try {
			name = remotedevice.getFriendlyName(true);
		
			System.out.print("Discovered " + name);
		}
		catch (Exception e) {System.out.println(e);}
		
		//if this isn't named correctly then it isn't a wiimote.
		//we will return and wait until we find a wiimote
		if (!name.equals("Nintendo RVL-CNT-01")) {
			System.out.println();
			return;
		}
		
		//it is a wiimote, so we will get it's address
		String address = remotedevice.getBluetoothAddress();
		System.out.print(" " + address+". ");
		
		//check to see if we found this wiimote previously. return if we did. we want a new one.
		if (discoveredWiimoteAddresses.contains(address)) {
			System.out.println("Already connected.");
			return;
		}
		
		discoveredAddress = address;
		//cancel inquiry so we can go to the next step of creating the connections
		agent.cancelInquiry(this);
	}
	
	/**
	 * Not implemented
	 */
	public void servicesDiscovered(int i, ServiceRecord aservicerecord[]) {}
	
	/**
	 * Not implemented
	 */
	public void serviceSearchCompleted(int i, int j) {}

}

