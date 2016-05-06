/*
 * 
 * Copyright (C) 2012-2014 R T Huitema. All Rights Reserved.
 * Web: www.42.co.nz
 * Email: robert@42.co.nz
 * Author: R T Huitema
 * 
 * This file is part of the signalk-server-java project
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

import nz.co.fortytwo.signalk.util.SignalKConstants;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

public class WsSessionProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = LogManager.getLogger(WsSessionProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		// check the client
		// exchange.getIn().setHeader(SignalKConstants.MSG_TYPE,
		// SignalKConstants.INTERNAL_IP);
		String wsSession = exchange.getIn().getHeader(
				WebsocketConstants.CONNECTION_KEY, String.class);
		if (logger.isDebugEnabled()) {
			logger.debug("WS connection key is " + wsSession);
			for (String key : exchange.getIn().getHeaders().keySet()) {
				logger.debug("In headers = " + key + "="
						+ exchange.getIn().getHeader(key));
			}
			for (String key : exchange.getProperties().keySet()) {
				logger.debug("props = " + key);
			}
			logger.debug("Found sessionId session = " + wsSession + ","
					+ manager.getSessionId(wsSession));
		}
		// need to set the ipaddress header here
		String remoteAddress = manager.getRemoteIpAddress(wsSession);
		String localAddress = manager.getLocalIpAddress(wsSession);

		exchange.getIn().setHeader(SignalKConstants.MSG_SRC_IP, remoteAddress);
		exchange.getIn().setHeader(SignalKConstants.MSG_SRC_BUS, "ws."+remoteAddress.replace('.', '_'));
		
		if (Util.sameNetwork(localAddress, remoteAddress)) {
			exchange.getIn().setHeader(SignalKConstants.MSG_TYPE,
					SignalKConstants.INTERNAL_IP);
		} else {
			exchange.getIn().setHeader(SignalKConstants.MSG_TYPE,
					SignalKConstants.EXTERNAL_IP);
		}

	}

}
