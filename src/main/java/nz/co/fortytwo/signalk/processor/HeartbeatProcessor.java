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

import static nz.co.fortytwo.signalk.util.JsonConstants.UPDATES;

import java.util.List;

import mjson.Json;
import nz.co.fortytwo.signalk.server.CamelContextFactory;
import nz.co.fortytwo.signalk.server.RouteManager;
import nz.co.fortytwo.signalk.server.SubscriptionManager;
import nz.co.fortytwo.signalk.server.SubscriptionManagerFactory;
import nz.co.fortytwo.signalk.util.JsonConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;

/**
 * Exports the signalkModel as a json object
 * 
 * @author robert
 * 
 */
public class HeartbeatProcessor extends SignalkProcessor implements Processor{

	private static Logger logger = Logger.getLogger(HeartbeatProcessor.class);
	private SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
	ProducerTemplate producer;
	Json msg = null;
	
	public HeartbeatProcessor(){
		producer= new DefaultProducerTemplate(CamelContextFactory.getInstance());
		producer.setDefaultEndpointUri(RouteManager.SEDA_COMMON_OUT );
		try {
			producer.start();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		msg=getEmptyDelta();
	}
	
	public void process(Exchange exchange) throws Exception {
		
		try {
			List<String> heartbeats = ImmutableList.copyOf(manager.getHeartbeats());
			if(logger.isDebugEnabled())logger.debug("process heartbeats: "+heartbeats.size());
			for(String session : heartbeats){
				producer.sendBodyAndHeader(msg, WebsocketConstants.CONNECTION_KEY, session);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		}
	}
	
	
	private Json getEmptyDelta(){
		Json delta = Json.object();
		delta.set(UPDATES,Json.array());
		delta.set(JsonConstants.CONTEXT,"vessels.self");
		
		return delta;
	}
	
}
