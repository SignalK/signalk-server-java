/*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
 * 
 * This file is part of FreeBoard. (http://www.42.co.nz/signalk)
 * 
 * FreeBoard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FreeBoard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FreeBoard. If not, see <http://www.gnu.org/licenses/>.
 */
package nz.co.fortytwo.freeboard.server.util;

public class Constants {

	// attached device types
	public static final String UID = "UID";
	public static final String IMU = "IMU";
	public static final String MEGA = "MEGA";

	public static final String MERGE_MODEL = "MRG"; 

		
	// config constants
	public static final String DEMO = "signalk.demo";
	public static final String WEBSOCKET_PORT = "signalk.websocket.port";
	public static final String REST_PORT = "signalk.rest.port";
	public static final String CFG_DIR = "signalk.cfg.dir";
	public static final String CFG_FILE = "signalk.cfg.file";
	public static final String STREAM_URL = "signalk.stream.demo.file";

	public static final String USBDRIVE = "signalk.usb.usbdrive";

	public static final String SERIAL_PORTS = "signalk.serial.ports";
	public static final String SERIAL_PORT_BAUD = "signalk.serial.port.baud";

	
	//debug flags
	public static final String SEND_MESSAGE = "signalk.debug.sendMessage";
	public static final String STATIC_DIR = "signalk.static.files.dir";
	
	public Constants() {
		// TODO Auto-generated constructor stub
	}

}
