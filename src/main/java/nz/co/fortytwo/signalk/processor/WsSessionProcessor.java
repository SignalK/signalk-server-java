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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.websocket.WebsocketComponent;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.component.websocket.WebsocketEndpoint;
import org.apache.log4j.Logger;

public class WsSessionProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = Logger.getLogger(WsSessionProcessor.class);
	

	@Override
	public void process(Exchange exchange) throws Exception {
		String connectionKey = exchange.getIn().getHeader(WebsocketConstants.CONNECTION_KEY, String.class);
		logger.info("WS connection key is " + connectionKey);
		
		for(String key : exchange.getIn().getHeaders().keySet()){
			logger.info("In headers = "+key+"="+exchange.getIn().getHeader(key));
		}
		for(String key : exchange.getProperties().keySet()){
			logger.info("props = "+key);
		}
		
		//String sessionId = exchange.getProperty(Constants.SESSIONID, String.class);
		
		String breadcrumb = exchange.getIn().getHeader(Exchange.BREADCRUMB_ID,String.class);
	    breadcrumb = breadcrumb.substring(0,breadcrumb.lastIndexOf("-",breadcrumb.lastIndexOf("-")));
	    logger.info("Found breadcrumb = "+breadcrumb);
	    String sessionId = manager.getWsSession(breadcrumb);
	    logger.info("Found sessionId session = "+breadcrumb+","+sessionId);
		manager.add(sessionId, connectionKey);
		logger.info("Added session = "+sessionId+","+connectionKey);
		
	}

}
