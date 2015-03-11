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

import static nz.co.fortytwo.signalk.util.JsonConstants.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import mjson.Json;
import nz.co.fortytwo.signalk.handler.JsonListHandler;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.Subscription;
import nz.co.fortytwo.signalk.server.SubscriptionManagerFactory;
import nz.co.fortytwo.signalk.util.JsonConstants;
import nz.co.fortytwo.signalk.util.SignalKConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.collect.ImmutableList;

/**
 * Handles json messages with 'list' requests. These provide a list of available signalk keys the server understands
 * 
 * @author robert
 * 
 */
public class JsonListProcessor extends SignalkProcessor implements Processor{

	private static Logger logger = Logger.getLogger(JsonListProcessor.class);
	//private static DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	private JsonListHandler listHandler = new JsonListHandler();
	
	
	
	public void process(Exchange exchange) throws Exception {
		
		try {
			if(exchange.getIn().getBody()==null ||!(exchange.getIn().getBody() instanceof Json)) return;
			String wsSession = exchange.getIn().getHeader(WebsocketConstants.CONNECTION_KEY, String.class);
			if(wsSession==null){
				return;
			}
			Json json = exchange.getIn().getBody(Json.class);
			//avoid full signalk syntax
			if(json.has(VESSELS))return;
			if(json.has(CONTEXT) && (json.has(LIST))){
				Json pathList = listHandler.handle(json);
				json.set(PATHLIST, pathList);
				json.delAt(LIST);
				outProducer.sendBodyAndHeader(json, WebsocketConstants.CONNECTION_KEY, wsSession);
				exchange.getIn().setBody(json);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

		
}
