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

import static nz.co.fortytwo.signalk.util.SignalKConstants.CONTEXT;
import static nz.co.fortytwo.signalk.util.SignalKConstants.LIST;
import static nz.co.fortytwo.signalk.util.SignalKConstants.PATHLIST;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels;

import java.util.Map;

import mjson.Json;
import nz.co.fortytwo.signalk.handler.JsonListHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

/**
 * Handles json messages with 'list' requests. These provide a list of available signalk keys the server understands
 * 
 * @author robert
 * 
 */
public class JsonListProcessor extends SignalkProcessor implements Processor{

	private static Logger logger = LogManager.getLogger(JsonListProcessor.class);
	//private static DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	private JsonListHandler listHandler = new JsonListHandler();
	
	
	
	public void process(Exchange exchange) throws Exception {
		
		try {
			if(exchange.getIn().getBody()==null ||!(exchange.getIn().getBody() instanceof Json)) return;
			String wsSession = exchange.getIn().getHeader(WebsocketConstants.CONNECTION_KEY, String.class);
			if(wsSession==null){
				if(logger.isDebugEnabled())logger.debug("Skipped, no session:"+exchange.getIn().getBody(Json.class));
				return;
			}
			Json json = exchange.getIn().getBody(Json.class);
			//avoid full signalk syntax
			if(json.has(vessels))return;
			if(json.has(CONTEXT) && (json.has(LIST))){
				Json pathList = listHandler.handle(json);
				json.set(PATHLIST, pathList);
				
				//also STOMP headers etc, swap replyTo
				Map<String, Object> headers = exchange.getIn().getHeaders();
				
				json.delAt(LIST);
				if(exchange.getIn().getHeader(RestApiProcessor.REST_REQUEST)!=null){
					if(logger.isDebugEnabled())logger.debug("Processed REST LIST request:"+exchange.getIn().getBody(Json.class));
					exchange.getIn().setBody(json);
				}else{
					if(logger.isDebugEnabled())logger.debug("Processed LIST request:"+exchange.getIn().getBody(Json.class));
					asyncSendBodyAndHeaders(outProducer,json, headers);
				}
				
			}else{
				if(logger.isDebugEnabled())logger.debug("Skipped, not a LIST request:"+exchange.getIn().getBody(Json.class));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

		
}
