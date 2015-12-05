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
package nz.co.fortytwo.signalk.server;

import static nz.co.fortytwo.signalk.util.SignalKConstants.FORMAT;
import static nz.co.fortytwo.signalk.util.SignalKConstants.GET;
import static nz.co.fortytwo.signalk.util.SignalKConstants.PATH;

import java.util.concurrent.CountDownLatch;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.JsonSerializer;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.activemq.broker.BrokerService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.websocket.SignalkWebsocketComponent;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;

public abstract class SignalKCamelTestSupport extends CamelTestSupport {
	static Logger logger = Logger.getLogger(SignalKCamelTestSupport.class);
	protected SignalKModel signalkModel = null;
	protected RouteManager routeManager = null;
	protected final CountDownLatch latch = new CountDownLatch(1);
    protected BrokerService broker = null;
    protected int restPort = 0;
    protected int wsPort = 0;
    protected JsonSerializer ser = new JsonSerializer();
    
    @BeforeClass
	public static void setClass() throws Exception {
		Util.setConfig(SignalKModelFactory.getMotuTestInstance());
		
	}
    
	public SignalKCamelTestSupport() {
		super();
		restPort= Util.getConfigPropertyInt(ConfigConstants.REST_PORT);
		wsPort=Util.getConfigPropertyInt(ConfigConstants.WEBSOCKET_PORT);
		try {
			broker = ActiveMqBrokerFactory.newInstance();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			fail();
		}
	}
	
	@After
	public void shutdownBroker() throws Exception{
		routeManager.stopNettyServers();
		broker.stop();
		routeManager.getContext().stop();
	}

	/**
	 * <pre>
	 {
    "context": "vessels.230099999",
    "subscribe": [
        {
                    "path": "navigation.speedThroughWater",
                    "period": 1000,
                    "format": "delta",
                    "policy": "ideal",
                    "minPeriod": 200
                },
         {
                    "path": "navigation.logTrip",
                    "period": 10000
                }
        ],
     "unsubscribe": [
        {
                    "path": "environment.depth.belowTransducer",
                }
         ]
}
	 * </pre>
	 * @param string
	 * @param i
	 * @param j
	 * @param formatDelta
	 * @param policyFixed
	 * @return
	 */
	public Json getSubscribe(String context, String path, int period, int minPeriod, String format, String policy) {
		Json json = Json.read("{\"context\":\""+context+"\", \"subscribe\": []}");
		Json sub = Json.object();
		sub.set("path",path);
		sub.set("period",period);
		sub.set("minPeriod",minPeriod);
		sub.set("format",format);
		sub.set("policy",policy);
		json.at("subscribe").add(sub);
		logger.debug("Created json subcribe: "+json);
		return json;
	}
	public Json getUnsubscribe(String context, String path, int period, int minPeriod, String format, String policy) {
		Json json = Json.read("{\"context\":\""+context+"\", \"unsubscribe\": []}");
		Json sub = Json.object();
		sub.set("path",path);
		sub.set("period",period);
		sub.set("minPeriod",minPeriod);
		sub.set("format",format);
		sub.set("policy",policy);
		json.at("unsubscribe").add(sub);
		logger.debug("Created json unsubscribe: "+json);
		return json;
	}
	
	public Json getList(String context, String path) {
		Json json = Json.read("{\"context\":\""+context+"\", \"list\": []}");
		Json sub = Json.object();
		sub.set("path",path);
		json.at("list").add(sub);
		logger.debug("Created json list: "+json);
		return json;
	}
	public Json getGet(String context, String path, String format) {
		Json json = Json.read("{\"context\":\"" + context + "\", \"get\": []}");
		Json sub = Json.object();
		sub.set(PATH, path);
		sub.set(FORMAT, format);
		json.at(GET).add(sub);
		logger.debug("Created json get: " + json);
		return json;
	}
	
	@Override
	protected RouteBuilder createRouteBuilder() {
	    try {
	    	try {
				//Util.getConfig();
				
				broker.start();
				logger.debug("Started broker");
				routeManager=new RouteManager(){
					@Override
					public void configure() throws Exception {
						if(CamelContextFactory.getInstance().getComponent("skWebsocket")==null){
							CamelContextFactory.getInstance().addComponent("skWebsocket", new SignalkWebsocketComponent());
						}
						configureRouteBuilder(this);
					
					}
				};
				//must do this early!
				CamelContextFactory.setContext(routeManager);
				RouteManagerFactory.manager=routeManager;
	    	} catch (Exception e) {
				logger.error(e);
			} 
			signalkModel=SignalKModelFactory.getInstance();

			return routeManager;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	    return null;
	}
	public abstract void configureRouteBuilder(RouteBuilder routeBuilder);

}