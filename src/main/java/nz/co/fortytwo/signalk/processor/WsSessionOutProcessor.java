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

import java.util.List;

import nz.co.fortytwo.signalk.server.CamelContextFactory;
import nz.co.fortytwo.signalk.server.util.JsonConstants;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.Producer;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class WsSessionOutProcessor extends WsSessionProcessor implements Processor {
	
	
	private static Logger logger = Logger.getLogger(WsSessionOutProcessor.class);

    Producer wsProducer=null;
	
    public WsSessionOutProcessor(){
    	
    }
	/* Sends a copy of the message to each subscription, adjusting the message for each client
	 * @see nz.co.fortytwo.signalk.processor.WsSessionProcessor#process(org.apache.camel.Exchange)
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		if(wsProducer==null){
			wsProducer = exchange.getContext().getEndpoint("websocket://0.0.0.0:9292"+JsonConstants.SIGNALK_WS).createProducer();
		}
		for (String key : subscriptionMap.keySet()) {
			logger.info("WS subs for key " + key);
			
			exchange.getIn().setHeader(WebsocketConstants.CONNECTION_KEY, key);
			wsProducer.process(exchange);
		}

	}

}
