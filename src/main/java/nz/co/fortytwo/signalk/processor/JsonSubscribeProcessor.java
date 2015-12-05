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
import static nz.co.fortytwo.signalk.util.SignalKConstants.FORMAT;
import static nz.co.fortytwo.signalk.util.SignalKConstants.FORMAT_DELTA;
import static nz.co.fortytwo.signalk.util.SignalKConstants.MIN_PERIOD;
import static nz.co.fortytwo.signalk.util.SignalKConstants.PATH;
import static nz.co.fortytwo.signalk.util.SignalKConstants.PERIOD;
import static nz.co.fortytwo.signalk.util.SignalKConstants.POLICY;
import static nz.co.fortytwo.signalk.util.SignalKConstants.POLICY_FIXED;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SUBSCRIBE;
import static nz.co.fortytwo.signalk.util.SignalKConstants.UNSUBSCRIBE;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels;

import java.util.Map;

import mjson.Json;
import nz.co.fortytwo.signalk.server.Subscription;
import nz.co.fortytwo.signalk.server.SubscriptionManagerFactory;
import nz.co.fortytwo.signalk.util.ConfigConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.log4j.Logger;

/**
 * Updates the subscription manager with this session subs
 * 
 * @author robert
 * 
 */
public class JsonSubscribeProcessor extends SignalkProcessor implements Processor{

	private static Logger logger = Logger.getLogger(JsonSubscribeProcessor.class);
	//private static DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	
	public void process(Exchange exchange) throws Exception {
		
		try {
			if(exchange.getIn().getBody()==null ||!(exchange.getIn().getBody() instanceof Json)) return;
			String wsSession = exchange.getIn().getHeader(WebsocketConstants.CONNECTION_KEY, String.class);
			if(wsSession==null){
				if(logger.isDebugEnabled())logger.debug("WsSession is null:"+exchange.getIn().getHeaders());
				//return;
			}
			Json json = exchange.getIn().getBody(Json.class);
			//avoid full signalk syntax
			if(json.has(vessels))return;
			if(json.has(CONTEXT) && (json.has(SUBSCRIBE) || json.has(UNSUBSCRIBE))){
				json = handle(json, exchange.getIn().getHeaders());
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
    "subscribe": [
        {
                    "path": "navigation.position",
                    "period": 120000,
                    "format": "full",
                    "policy": "fixed"
                },
         {
                   "path": "navigation.courseOverGround",
                    "period": 120000,
                    "format": "full",
                    "policy": "fixed"
                }
        ]
}

{"context":"vessels.*","subscribe":[{"path":"navigation.position","period":120000,"format":"full","policy":"fixed"},{"path":"navigation.courseOverGround","period":120000,"format":"full","policy":"fixed"}]}
</pre>
*/
	 
	//@Override
	public Json  handle(Json node, Map<String, Object> headers) throws Exception {
		
		//deal with diff format
		
		if(logger.isDebugEnabled())logger.debug("Checking for subscribe  "+node );
		
		//go to context
		String context = node.at(CONTEXT).asString();
		//TODO: is the context and path valid? A DOS attack is possible if we allow numerous crap/bad paths?
		
		Json subscriptions = node.at(SUBSCRIBE);
		if(subscriptions!=null){
			//MQTT and STOMP wont have created proper session links
			if(node.has(WebsocketConstants.CONNECTION_KEY)){
				String wsSession = node.at(WebsocketConstants.CONNECTION_KEY).asString();
				if(node.has(ConfigConstants.OUTPUT_TYPE)){
					String outputType = node.at(ConfigConstants.OUTPUT_TYPE).asString();
					SubscriptionManagerFactory.getInstance().add(wsSession, wsSession, outputType,"127.0.0.1","127.0.0.1");
				}
			}
			if(subscriptions.isArray()){
				for(Json subscription: subscriptions.asJsonList()){
					
					parseSubscribe(headers, context, subscription);
				}
			}
			if(logger.isDebugEnabled())logger.debug("processed subscribe  "+node );
		}
		Json unsubscriptions = node.at(UNSUBSCRIBE);
		if(unsubscriptions!=null){
		
			if(unsubscriptions.isArray()){
				for(Json subscription: unsubscriptions.asJsonList()){
					parseUnsubscribe(headers, context, subscription);
				}
			}
			if(logger.isDebugEnabled())logger.debug("processed unsubscribe  "+node );
		}
			
		
		return node;
		
	}

	/**
	 *  
	 *   <pre>{
                    "path": "navigation.speedThroughWater",
                    "period": 1000,
                    "format": "delta",
                    "policy": "ideal",
                    "minPeriod": 200
                }
                </pre>
	 * @param context
	 * @param subscription
	 * @throws Exception 
	 */
	private void parseSubscribe(Map<String, Object> headers, String context, Json subscription) throws Exception {
		//get values
		if(logger.isDebugEnabled())logger.debug("Parsing subscribe  "+subscription );
		String wsSession = (String) headers.get(WebsocketConstants.CONNECTION_KEY);
		String path = context+"."+subscription.at(PATH).asString();
		long period = 1000;
		if(subscription.at(PERIOD)!=null)period = subscription.at(PERIOD).asInteger();
		String format = FORMAT_DELTA;
		if(subscription.at(FORMAT)!=null)format=subscription.at(FORMAT).asString();
		String policy = POLICY_FIXED;
		if(subscription.at(POLICY)!=null)policy=subscription.at(POLICY).asString();
		long minPeriod = 0;
		if(subscription.at(MIN_PERIOD)!=null)minPeriod=subscription.at(MIN_PERIOD).asInteger();
		Subscription sub = new Subscription(wsSession, path, period, minPeriod, format, policy);
		
		//STOMP, MQTT
		if(headers.containsKey(ConfigConstants.DESTINATION)){
			sub.setDestination( headers.get(ConfigConstants.DESTINATION).toString());
		}
		
		//sub.setActive(false);
		if(logger.isDebugEnabled())logger.debug("Created subscription; "+sub.toString() );
		SubscriptionManagerFactory.getInstance().addSubscription(sub);
		
	}
	
	/**
	 *  
	 *   <pre>{
                    "path": "navigation.speedThroughWater",
                    "period": 1000,
                    "format": "delta",
                    "policy": "ideal",
                    "minPeriod": 200
                }
                </pre>
	 * @param context
	 * @param subscription
	 * @throws Exception 
	 */
	private void parseUnsubscribe(Map<String, Object> headers, String context, Json subscription) throws Exception {
		//get values
		String path = context+"."+subscription.at(PATH).asString();
		String wsSession = (String) headers.get(WebsocketConstants.CONNECTION_KEY);
		long period = 1000;
		if(subscription.at(PERIOD)!=null)period = subscription.at(PERIOD).asInteger();
		String format = FORMAT_DELTA;
		if(subscription.at(FORMAT)!=null)format=subscription.at(FORMAT).asString();
		String policy = POLICY_FIXED;
		if(subscription.at(POLICY)!=null)policy=subscription.at(POLICY).asString();
		long minPeriod = 0;
		if(subscription.at(MIN_PERIOD)!=null)minPeriod=subscription.at(MIN_PERIOD).asInteger();
		Subscription sub = new Subscription(wsSession, path, period, minPeriod, format, policy);
		SubscriptionManagerFactory.getInstance().removeSubscription(sub);
		
	}

	
}
