## Wiimote Simple ##

This Java library allows for the development of applications that interface with the Nintendo Wii Remote. It was designed to be very simple to use.

If you need a Java library for interfacing with Wiimotes, I suggest you first try a different project: [WiiremoteJ](http://www.wiili.org/WiiremoteJ). It has more functionality than this library, but is NOT open source. If you run into trouble, you might get stuck.

If WiiremoteJ didn't work out for you (it didn't for me), then feel free to try this library. It has less functionality than WiiremoteJ, but it will likely do what you need, and if it doesn't, you can add that functionality, since you have the source.

## Features ##

  * Read accelerometer data, useful for responding to dynamic swinging actions.
  * Read infrared camera data, useful for cursor pointing.
  * Respond to button pushes.
  * Vibrate Wiimote.
  * Set lights on the Wiimote.

## Instructions ##

Requirements:

  * A Java Compiler (not sure on version requirements)
  * An implementation of JSR-82 (e.g. [Bluecove](http://code.google.com/p/bluecove/) or [Avetana](http://www.avetana-gmbh.de/avetana-gmbh/produkte/jsr82.eng.xml))
  * Bluetooth drivers (e.g. WIDCOMM on Windows XP, or the built-in OS X drivers)

Note that the built-in drivers in Windows XP SP2 do not work with Wiimotes. Many Bluetooth dongles come with their own WIDCOMM drivers, which should work. Wiimote Simple should work on Linux as well, although I have never tested it. Please refer to the bluecove site for Linux driver information.

## Disclaimer ##

Supporting this project is not high on my priority list. I released this code mostly so that people in a similar situation to me have some code that can provide a head start. I will try to check in on this project page every once in a while, but do not be surprised if you ask a question and I don't answer.

Garth