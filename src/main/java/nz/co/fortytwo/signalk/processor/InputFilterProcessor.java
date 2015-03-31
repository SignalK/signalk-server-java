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
package nz.co.fortytwo.signalk.processor;

import java.util.Map;

import mjson.Json;
import nz.co.fortytwo.signalk.util.Constants;
import nz.co.fortytwo.signalk.util.JsonConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.log4j.Logger;

/**
 * Churns through incoming nav data and filters out misc debug and unnecessary messages from the other devices
 * Maps misc content to tags (AIS,NMEA,JSON)
 * 
 * @author robert
 *
 */
public class InputFilterProcessor extends SignalkProcessor implements Processor {
	private static Logger logger = Logger.getLogger(InputFilterProcessor.class);
	
	public InputFilterProcessor(){
		
	}
	public void process(Exchange exchange) throws Exception {
		String msg = exchange.getIn().getBody(String.class);
		if(msg !=null){
			msg=msg.trim();
			//stomp messages are prefixed with 'ascii:'
			if(msg.startsWith("ascii:"))msg = msg.substring(6).trim();
			boolean ok = false;
			if(msg.startsWith("!AIVDM")){
				//AIS
				//!AIVDM,1,1,,B,15MwkRUOidG?GElEa<iQk1JV06Jd,0*6D
				sendNmea(exchange);
				ok = true;
			}else if(msg.startsWith("$")){
				//NMEA - good
				//System.out.println(msg);
				sendNmea(exchange);
				ok = true;
			}else if(msg.startsWith("{")&& msg.endsWith("}")){
				Json json = Json.read(msg);
				//n2k
				if(json.has(JsonConstants.PGN)){
					exchange.getIn().setHeader(JsonConstants.N2K_MESSAGE, msg);
				}
				//compensate for MQTT and STOMP sessionid
				if(json.has(WebsocketConstants.CONNECTION_KEY)){
					exchange.getIn().setHeader(WebsocketConstants.CONNECTION_KEY, json.at(WebsocketConstants.CONNECTION_KEY).asString());
				}
				//deal with REPLY_TO
				Map<String, Object> headers = exchange.getIn().getHeaders();
				if(headers!=null && headers.containsKey(Constants.REPLY_TO)){
					exchange.getIn().setHeader(Constants.DESTINATION, headers.get(Constants.REPLY_TO));
					//headers.remove(Constants.REPLY_TO);
				}
				//for MQTT
				if(json.has(Constants.REPLY_TO)){
					exchange.getIn().setHeader(Constants.DESTINATION, ( json.at(Constants.REPLY_TO).asString()));
				}
				//json
				exchange.getIn().setBody(json);
				ok = true;
			}
			if(ok){
				return;
			}
			//uh-oh log it, squash it
			exchange.getUnitOfWork().done(exchange);
			//System.out.println("Dropped invalid message:"+msg);
			logger.info("Dropped invalid message:"+msg);
			exchange.getIn().setBody(null);
			//throw new CamelExchangeException("Invalid msg", exchange);
		}
		
	}

	
}
