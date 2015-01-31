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

import static nz.co.fortytwo.signalk.server.util.JsonConstants.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.Subscription;
import nz.co.fortytwo.signalk.server.SubscriptionManagerFactory;
import nz.co.fortytwo.signalk.server.util.JsonConstants;
import nz.co.fortytwo.signalk.server.util.SignalKConstants;

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
 * Handles  json messages with 'get' requests
 * 
 * @author robert
 * 
 */
public class JsonGetProcessor extends SignalkProcessor implements Processor{

	private static Logger logger = Logger.getLogger(JsonGetProcessor.class);
	//private static DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	
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
			if(json.has(CONTEXT) && (json.has(GET))){
				json = handle(json, wsSession);
				exchange.getIn().setBody(json);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	/*
	 *  
	 *  <pre>
{
    "context": "vessels.*",
    "get": [
        {
                    "path": "navigation.position",
                }
        ]
}

</pre>
*/
	 
	//@Override
	public Json  handle(Json node, String wsSession) throws Exception {
		
		if(logger.isDebugEnabled())logger.debug("Checking for list  "+node );
		
		//go to context
		String context = node.at(CONTEXT).asString();
		//TODO: is the context and path valid? A DOS attack is possible if we allow numerous crap/bad paths?
		
		Json paths = node.at(GET);
		if(paths!=null){
			if(paths.isArray()){
				SignalKModel tree = SignalKModelFactory.getCleanInstance();
				for(Json path: paths.asJsonList()){
					parseGet(wsSession, context, path, tree);
				}
				node.delAt(GET);
				Map<String, Object> headers = new HashMap<String, Object>();
				headers.put(WebsocketConstants.CONNECTION_KEY, wsSession);
				headers.put(SIGNALK_FORMAT, FORMAT_DELTA);
				
				outProducer.sendBodyAndHeaders(tree, headers);
			}
			if(logger.isDebugEnabled())logger.debug("Processed list  "+node );
			
		}
		
		return node;
		
	}

	/**
	 *  
	 *   <pre>{
                    "path": "navigation.speedThroughWater",
                  
                }
                </pre>
	 * @param context
	 * @param list
	 * @throws Exception 
	 */
	private void parseGet(String wsSession, String context, Json path, SignalKModel tree) throws Exception {
		//get values
		String regexKey = context+DOT+path.at(PATH).asString();
		if(logger.isDebugEnabled())logger.debug("Parsing list  "+regexKey );
		
		List<String> rslt = getMatchingPaths(regexKey);
		//add to tree
		for(String p: rslt){
			populateTree(tree, p);
		}
		
	}
	
	/**
	 * Returns a list of paths that this implementation is currently providing.
	 * The list is filtered by the key if it is not null or empty in which case a full list is returned,
	 * supports * and ? wildcards.
	 * @param regex
	 * @return
	 */
	public List<String> getMatchingPaths(String regex) {
		if(StringUtils.isBlank(regex)){
			return ImmutableList.copyOf(signalkModel.getFullPaths());
		}
		regex=sanitizePath(regex);
		Pattern pattern = regexPath(regex);
		List<String> paths = new ArrayList<String>();
		for (String p : signalkModel.getFullPaths()) {
			if (pattern.matcher(p).matches()) {
				if(logger.isDebugEnabled())logger.debug("Adding path:" + p);
				paths.add(p);
			}
		}
		return paths;
	}

	
}
