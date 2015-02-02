/*
 *
 * Copyright (C) 2012-2014 R T Huitema. All Rights Reserved.
 * Web: www.42.co.nz
 * Email: robert@42.co.nz
 * Author: R T Huitema
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package nz.co.fortytwo.signalk.server.util;

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
	public static final String SELF = "signalk.vessel.self";
//	public static final String SESSIONID = "signalk.session";
	public static final String TCP_PORT = "signalk.tcp.port";
	public static final String UDP_PORT = "signalk.udp.port";
	public static final String TCP_NMEA_PORT = "signalk.tcp.nmea.port";
	public static final String UDP_NMEA_PORT = "signalk.udp.nmea.port";
	
	public Constants() {
		// TODO Auto-generated constructor stub
	}

}
