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
package nz.co.fortytwo.signalk.server;

import java.util.List;

import nz.co.fortytwo.signalk.processor.AISProcessor;
import nz.co.fortytwo.signalk.processor.DeclinationProcessor;
import nz.co.fortytwo.signalk.processor.FullImportProcessor;
import nz.co.fortytwo.signalk.processor.FullToDeltaProcessor;
import nz.co.fortytwo.signalk.processor.FullExportProcessor;
import nz.co.fortytwo.signalk.processor.DeltaImportProcessor;
import nz.co.fortytwo.signalk.processor.HeartbeatProcessor;
import nz.co.fortytwo.signalk.processor.InputFilterProcessor;
import nz.co.fortytwo.signalk.processor.JsonGetProcessor;
import nz.co.fortytwo.signalk.processor.JsonListProcessor;
import nz.co.fortytwo.signalk.processor.MapToJsonProcessor;
import nz.co.fortytwo.signalk.processor.MqttProcessor;
import nz.co.fortytwo.signalk.processor.N2KProcessor;
import nz.co.fortytwo.signalk.processor.NMEAProcessor;
import nz.co.fortytwo.signalk.processor.OutputFilterProcessor;
import nz.co.fortytwo.signalk.processor.RestApiProcessor;
import nz.co.fortytwo.signalk.processor.RestAuthProcessor;
import nz.co.fortytwo.signalk.processor.SignalkModelProcessor;
import nz.co.fortytwo.signalk.processor.RestSubscribeProcessor;
import nz.co.fortytwo.signalk.processor.JsonSubscribeProcessor;
import nz.co.fortytwo.signalk.processor.StompProcessor;
import nz.co.fortytwo.signalk.processor.ValidationProcessor;
import nz.co.fortytwo.signalk.processor.WindProcessor;
import nz.co.fortytwo.signalk.processor.WsSessionProcessor;
import nz.co.fortytwo.signalk.util.Constants;
import nz.co.fortytwo.signalk.util.JsonConstants;

import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.component.websocket.WebsocketEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.log4j.Logger;



public class SignalkRouteFactory {

	private static Logger logger = Logger.getLogger(SignalkRouteFactory.class);
	/**
	 * Configures a route for all input traffic, which will parse the traffic and update the signalk model
	 * @param routeBuilder
	 * @param input
	 * @param inputFilterProcessor
	 * @param nmeaProcessor
	 * @param aisProcessor
	 * @param signalkModelProcessor
	 * @throws Exception 
	 */
	public static void configureInputRoute(RouteBuilder routeBuilder,String input) {
		routeBuilder.from(input)
			.onException(Exception.class).handled(true).maximumRedeliveries(0)
			.to("log:nz.co.fortytwo.signalk.model.receive?level=ERROR&showException=true&showStackTrace=true")
			.end()
		// dump misc rubbish
		.process(new InputFilterProcessor())
		//convert NMEA to signalk
		.process(new NMEAProcessor())
		//convert AIS to signalk
		.process(new AISProcessor())
		//convert n2k
		.process(new N2KProcessor())
		//handle list
		.process(new JsonListProcessor())
		//handle get
		.process(new JsonGetProcessor())
		//handle subscribe messages
		.process(new JsonSubscribeProcessor())
		//deal with delta format
		.process(new DeltaImportProcessor())
		//deal with full format
		.process(new FullImportProcessor())
		//make sure we have timestamp/source
		.process(new ValidationProcessor())
		//and update signalk model
		.process(new SignalkModelProcessor());
		
	}
	
	/**
	 * Configures the route for output to websockets
	 * @param routeBuilder
	 * @param input
	 */
	public static void configureWebsocketTxRoute(RouteBuilder routeBuilder ,String input, int port){
		
		//from SEDA_WEBSOCKETS
			routeBuilder.from(input)
				.onException(Exception.class)
				.handled(true)
				.maximumRedeliveries(0)
				.end()
				//.to("log:nz.co.fortytwo.signalk.model.websocket.tx?level=ERROR&showException=true&showStackTrace=true")
			.process(new OutputFilterProcessor())
			.to("skWebsocket://0.0.0.0:"+port+JsonConstants.SIGNALK_WS);
		
	}
	/**
	 * Configures the route for input to websockets
	 * @param routeBuilder
	 * @param input
	 * @throws Exception 
	 */
	public static void configureWebsocketRxRoute(RouteBuilder routeBuilder ,String input, int port)  {
		
		WebsocketEndpoint wsEndpoint = (WebsocketEndpoint) routeBuilder.getContext().getEndpoint("skWebsocket://0.0.0.0:"+port+JsonConstants.SIGNALK_WS);
		
		wsEndpoint.setSessionSupport(true);
		
		routeBuilder.from(wsEndpoint)
			.onException(Exception.class)
			.handled(true).maximumRedeliveries(0)
			.to("log:nz.co.fortytwo.signalk.model.websocket.rx?level=ERROR&showException=true&showStackTrace=true")
			.end()
		.process(new WsSessionProcessor())
		//.to("log:nz.co.fortytwo.signalk.model.websocket.rx?level=INFO&showException=true&showStackTrace=true")
		.to(input);
		
	}
	public static void configureTcpServerRoute(RouteBuilder routeBuilder ,String input, NettyServer nettyServer) throws Exception{
		// push out via TCPServer.
		routeBuilder.from(input)
			.onException(Exception.class)
			.handled(true)
			.maximumRedeliveries(0)
			.end()
		
		.process((Processor) nettyServer).end();
			
	}
	
	public static void configureRestRoute(RouteBuilder routeBuilder ,String input){
		routeBuilder.from(input)
			.setExchangePattern(ExchangePattern.InOut)
			.process(new RestApiProcessor())
			.process(new OutputFilterProcessor());
		}
	
	public static void configureAuthRoute(RouteBuilder routeBuilder ,String input){
		routeBuilder.from(input)
			.setExchangePattern(ExchangePattern.InOut)
			.process(new RestAuthProcessor());
		}
	
	public static void configureDeclinationTimer(RouteBuilder routeBuilder ,String input){
		routeBuilder.from(input).process(new DeclinationProcessor()).to("log:nz.co.fortytwo.signalk.model.update?level=INFO").end();
	}
	
	public static void configureWindTimer(RouteBuilder routeBuilder ,String input){
		routeBuilder.from("timer://wind?fixedRate=true&period=1000").process(new WindProcessor()).to("log:nz.co.fortytwo.signalk.model.update?level=INFO").end();
	}
	
	public static void configureCommonOut(RouteBuilder routeBuilder ){
		routeBuilder.from(RouteManager.SEDA_COMMON_OUT)
			.onException(Exception.class).handled(true).maximumRedeliveries(0)
			.to("log:nz.co.fortytwo.signalk.model.output?level=ERROR")
			.end()
		.process(new MapToJsonProcessor())
		.process(new FullToDeltaProcessor())
		.process(new StompProcessor())
		.process(new OutputFilterProcessor())
		.multicast().parallelProcessing()
			.to(RouteManager.DIRECT_TCP,
					RouteManager.SEDA_WEBSOCKETS, 
					RouteManager.DIRECT_MQTT, 
					RouteManager.DIRECT_STOMP,
					"log:nz.co.fortytwo.signalk.model.output?level=DEBUG"
					)
		.end();
		routeBuilder.from(RouteManager.DIRECT_MQTT)
		.filter(routeBuilder.header(Constants.OUTPUT_TYPE).isEqualTo(Constants.OUTPUT_MQTT))
			.process(new MqttProcessor())
			.to(RouteManager.MQTT+"?publishTopicName=signalk.dlq");
		routeBuilder.from(RouteManager.DIRECT_STOMP)
			.filter(routeBuilder.header(Constants.OUTPUT_TYPE).isEqualTo(Constants.OUTPUT_STOMP))
			.process(new StompProcessor())
			.to(RouteManager.STOMP);
	}
	
	public static void configureSubscribeTimer(RouteBuilder routeBuilder ,Subscription sub) throws Exception{
		String input = "timer://"+getRouteId(sub)+"?fixedRate=true&period="+sub.getPeriod();
		if(logger.isDebugEnabled())logger.debug("Configuring route "+input);
		String wsSession = sub.getWsSession();
		RouteDefinition route = routeBuilder.from(input);
			route.process(new FullExportProcessor(wsSession))
				.onException(Exception.class).handled(true).maximumRedeliveries(0)
				.to("log:nz.co.fortytwo.signalk.model.output.subscribe?level=ERROR")
				.end()
			.setHeader(WebsocketConstants.CONNECTION_KEY, routeBuilder.constant(wsSession))
			.to(RouteManager.SEDA_COMMON_OUT)
			.end();
		route.setId(getRouteId(sub));
		sub.setRouteId(getRouteId(sub));
		((DefaultCamelContext)CamelContextFactory.getInstance()).addRouteDefinition(route);
		((DefaultCamelContext)CamelContextFactory.getInstance()).startRoute(route.getId());
		//routeBuilder.getContext().startAllRoutes();
	}

	private static String getRouteId(Subscription sub) {
		return "sub_"+sub.getWsSession();
	}

	public static void configureSubscribeRoute(RouteBuilder routeBuilder, String input) {
		routeBuilder.from(input)
		.setExchangePattern(ExchangePattern.InOut)
		.process(new RestSubscribeProcessor());
	}

	public static void removeSubscribeTimers(RouteManager routeManager, List<Subscription> subs) throws Exception {
		for(Subscription sub : subs){
			SignalkRouteFactory.removeSubscribeTimer(routeManager, sub);
		}
		
	}
	
	public static void removeSubscribeTimer(RouteBuilder routeManager, Subscription sub) throws Exception {
			RouteDefinition routeDef = ((DefaultCamelContext)routeManager.getContext()).getRouteDefinition(getRouteId(sub));
			if(routeDef==null)return;
			if(logger.isDebugEnabled())logger.debug("Stopping sub "+getRouteId(sub)+","+routeDef);
			((DefaultCamelContext)routeManager.getContext()).stopRoute(routeDef);
			if(logger.isDebugEnabled())logger.debug("Removing sub "+getRouteId(sub));
			((DefaultCamelContext)routeManager.getContext()).removeRouteDefinition(routeDef);
			
			if(logger.isDebugEnabled())logger.debug("Done removing sub "+getRouteId(sub));
	}

	public static void configureHeartbeatRoute(RouteBuilder routeBuilder, String input) {
		
		routeBuilder.from(input)
			.onException(Exception.class).handled(true).maximumRedeliveries(0)
			.to("log:nz.co.fortytwo.signalk.model.output.all?level=ERROR")
			.end()
		.process(new HeartbeatProcessor());
		
	}
		
	
}
