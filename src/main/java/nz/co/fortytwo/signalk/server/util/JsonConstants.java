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

public class JsonConstants extends SignalKConstants {

	
	
	public static final String MSG_TYPE = "MSG_TYPE";
	public static final String SERIAL = "SERIAL";
	public static final String EXTERNAL_IP = "EXTERNAL_IP";
	public static final String INTERNAL_IP = "INTERNAL_IP";
	public static final String MSG_PORT = "MSG_PORT";
	public static final String MSG_APPROVAL = "MSG_APPROVAL";
	public static final Object REQUIRED = "REQUIRED";
	
	public static final String VESSELS = "vessels";
	public static final String SELF = Util.getConfigProperty(Constants.SELF);
	public static final String CONTEXT = "context";
	public static final String UPDATES = "updates";
	public static final String SUBSCRIBE = "subscribe";
	public static final String UNSUBSCRIBE = "unsubscribe";
	public static final String LIST = "list";
	public static final String GET = "get";
	public static final String PATHLIST = "pathlist";
	public static final String SOURCE = "source";
	public static final String DEVICE = "device";
	public static final String TIMESTAMP = "timestamp";
	public static final String SRC = "src";
	public static final String PGN = "pgn";
	public static final String VALUE = "value";
	public static final String VALUES = "values";
	public static final String PATH = "path";
	public static final String PERIOD = "period";
	public static final String MIN_PERIOD = "minPeriod";
	public static final String SIGNALK_FORMAT="SIGNALK_FORMAT";
	public static final String FORMAT="format";
	public static final String FORMAT_DELTA="delta";
	public static final String FORMAT_FULL="full";
	public static final String POLICY="policy";
	public static final String POLICY_FIXED = "fixed";
	public static final String POLICY_INSTANT = "instant";
	public static final String POLICY_IDEAL = "ideal";
	
	public static final String N2K_MESSAGE = "N2K_MESSAGE";
	
	
	//public static final String name = "name";
	//public static final String mmsi = "mmsi";
	public static final String source = "source";
	public static final String timezone = "timezone";
	

	public static final String SIGNALK_AUTH = "/signalk/auth";
	public static final String SIGNALK_API = "/signalk/api";
	public static final String SIGNALK_SUBSCRIBE = "/signalk/stream";
	public static final String SIGNALK_WS = "/signalk/stream";
	
	public JsonConstants() {
		// TODO Auto-generated constructor stub
	}
	



}
