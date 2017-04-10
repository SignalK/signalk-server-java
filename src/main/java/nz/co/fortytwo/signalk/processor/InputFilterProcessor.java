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
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.fortytwo.signalk.processor;

import java.util.Map;

import mjson.Json;
import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.SignalKConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

/**
 * Churns through incoming nav data and filters out misc debug and unnecessary messages from the other devices
 * Maps misc content to tags (AIS,NMEA,JSON)
 * 
 * @author robert
 * 
 */
public class InputFilterProcessor extends SignalkProcessor implements Processor {
	private static Logger logger = LogManager.getLogger(InputFilterProcessor.class);

	public InputFilterProcessor() {

	}

	public void process(Exchange exchange) throws Exception {
		String msg = exchange.getIn().getBody(String.class);
		if (msg != null) {
			msg = msg.trim();
			// stomp messages are prefixed with 'ascii:'
			if (msg.startsWith("ascii:"))
				msg = msg.substring(6).trim();
			msg = StringUtils.chomp(msg);
			boolean ok = false;
			if (msg.startsWith("!AIVDM")) {
				// AIS
				// !AIVDM,1,1,,B,15MwkRUOidG?GElEa<iQk1JV06Jd,0*6D
				exchange.getIn().setBody(msg);
				sendNmea(exchange);
				ok = true;
			} else if (msg.startsWith("$")) {
				// NMEA - good
				// System.out.println(msg);
				exchange.getIn().setBody(msg);
				logger.info(msg.toString());
                                sendNmea(exchange);
				ok = true;
			} else if (msg.startsWith("{") && msg.endsWith("}")) {
				Json json = Json.read(msg);
				// n2k
				if (json.has(SignalKConstants.pgn)) {
					exchange.getIn().setHeader(SignalKConstants.N2K_MESSAGE, msg);
				}
				// full or delta format
				if (exchange.getIn().getHeader(SignalKConstants.SIGNALK_FORMAT) == null) {
					if (json.has(SignalKConstants.CONTEXT)) {
						exchange.getIn().setHeader(SignalKConstants.SIGNALK_FORMAT, SignalKConstants.FORMAT_DELTA);
					}
					if (json.has(SignalKConstants.vessels)||json.has(SignalKConstants.resources)) {
						exchange.getIn().setHeader(SignalKConstants.SIGNALK_FORMAT, SignalKConstants.FORMAT_FULL);
					}
				}
				// compensate for MQTT and STOMP sessionid
				if (json.has(WebsocketConstants.CONNECTION_KEY)) {
					exchange.getIn().setHeader(WebsocketConstants.CONNECTION_KEY, json.at(WebsocketConstants.CONNECTION_KEY).asString());
				}
				// deal with REPLY_TO
				Map<String, Object> headers = exchange.getIn().getHeaders();
				if (headers != null && headers.containsKey(ConfigConstants.REPLY_TO)) {
					exchange.getIn().setHeader(ConfigConstants.DESTINATION, headers.get(ConfigConstants.REPLY_TO));
					// headers.remove(Constants.REPLY_TO);
				}
				// for MQTT
				if (json.has(ConfigConstants.REPLY_TO)) {
					exchange.getIn().setHeader(ConfigConstants.DESTINATION, (json.at(ConfigConstants.REPLY_TO).asString()));
				}
				//if it has a config object, flag it as such
				if (json.has(SignalKConstants.CONFIG)
						||(json.has(SignalKConstants.CONTEXT) && StringUtils.startsWith(json.at(SignalKConstants.CONTEXT).toString(),SignalKConstants.CONFIG))) {
					exchange.getIn().setHeader(SignalKConstants.CONFIG_ACTION, SignalKConstants.CONFIG_ACTION_SAVE);
				}
				// json
				exchange.getIn().setBody(json);
				ok = true;
			}
			if (ok) {
				return;
			}
			// uh-oh log it, squash it
			exchange.getUnitOfWork().done(exchange);
			// System.out.println("Dropped invalid message:"+msg);
			logger.info("Dropped invalid message:" + msg);
			exchange.getIn().setBody(null);
			// throw new CamelExchangeException("Invalid msg", exchange);
		}

	}

}
