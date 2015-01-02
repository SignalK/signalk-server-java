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
import nz.co.fortytwo.signalk.processor.DeltaExportProcessor;
import nz.co.fortytwo.signalk.processor.DeltaImportProcessor;
import nz.co.fortytwo.signalk.processor.InputFilterProcessor;
import nz.co.fortytwo.signalk.processor.NMEAProcessor;
import nz.co.fortytwo.signalk.processor.OutputFilterProcessor;
import nz.co.fortytwo.signalk.processor.RestApiProcessor;
import nz.co.fortytwo.signalk.processor.RestAuthProcessor;
import nz.co.fortytwo.signalk.processor.SignalkModelProcessor;
import nz.co.fortytwo.signalk.processor.SubscribeProcessor;
import nz.co.fortytwo.signalk.processor.ValidationProcessor;
import nz.co.fortytwo.signalk.processor.WindProcessor;
import nz.co.fortytwo.signalk.processor.WsSessionProcessor;
import nz.co.fortytwo.signalk.server.util.JsonConstants;

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
	 */
	public static void configureInputRoute(RouteBuilder routeBuilder,String input) {
		routeBuilder.from(input).onException(Exception.class).handled(true).maximumRedeliveries(0)
			.to("log:nz.co.fortytwo.signalk.model.receive?level=ERROR&showException=true&showStackTrace=true").end()
		// dump misc rubbish
		.process(new InputFilterProcessor())
		//convert NMEA to signalk
		.process(new NMEAProcessor())
		//convert AIS to signalk
		.process(new AISProcessor())
		//deal with diff format
		.process(new DeltaImportProcessor())
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
				//.onException(Exception.class)
				//.handled(true).maximumRedeliveries(0)
				.to("log:nz.co.fortytwo.signalk.model.websocket.tx?level=ERROR&showException=true&showStackTrace=true")
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
			//.onException(Exception.class)
			//.handled(true).maximumRedeliveries(0)
			//.to("log:nz.co.fortytwo.signalk.model.websocket.rx?level=ERROR&showException=true&showStackTrace=true")
			//.end()
		.process(new WsSessionProcessor())
		.to("log:nz.co.fortytwo.signalk.model.websocket.rx?level=INFO&showException=true&showStackTrace=true")
		.to(input);
		
	}
	public static void configureTcpServerRoute(RouteBuilder routeBuilder ,String input, TcpServer tcpServer){
		// push NMEA out via TCPServer.
		routeBuilder.from(input).process((Processor) tcpServer).end();
	}
	
	public static void configureRestRoute(RouteBuilder routeBuilder ,String input){
		routeBuilder.from(input)
			.setExchangePattern(ExchangePattern.InOut)
			.process(new RestApiProcessor());
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
	public static void configureOutputTimer(RouteBuilder routeBuilder ,String input){
		routeBuilder.from(input)
		.process(new DeltaExportProcessor())
		.split(routeBuilder.body())
		.setHeader(WebsocketConstants.SEND_TO_ALL, routeBuilder.constant(true))
		.to("log:nz.co.fortytwo.signalk.model.output.all?level=INFO")
		.multicast()
			.to(RouteManager.SEDA_WEBSOCKETS)
			.to(RouteManager.DIRECT_TCP)
		.end();
	}
	public static void configureSubscribeTimer(RouteBuilder routeBuilder ,Subscription sub) throws Exception{
		String input = "timer://sub_"+sub.getWsSession()+"_"+sub.getPath()+"?fixedRate=true&period="+sub.getPeriod();
		logger.debug("Configuring route "+input);
		String wsSession = sub.getWsSession();
		RouteDefinition route = routeBuilder.from(input);
			route.process(new DeltaExportProcessor())
			.split(routeBuilder.body())
			.setHeader(WebsocketConstants.CONNECTION_KEY, routeBuilder.constant(wsSession))
			.to("log:nz.co.fortytwo.signalk.model.output.wsSession?level=INFO")
			.to(RouteManager.SEDA_WEBSOCKETS)
			.end();
		route.setId("sub_"+sub.getWsSession()+"_"+sub.getPath());
		((DefaultCamelContext)CamelContextFactory.getInstance()).addRouteDefinition(route);
		((DefaultCamelContext)CamelContextFactory.getInstance()).startRoute(route.getId());
		//routeBuilder.getContext().startAllRoutes();
	}

	public static void configureSubscribeRoute(RouteBuilder routeBuilder, String input) {
		routeBuilder.from(input)
		.setExchangePattern(ExchangePattern.InOut)
		.process(new SubscribeProcessor());
	}

	public static void removeSubscribeTimers(RouteManager routeManager, List<Subscription> subs) throws Exception {
		for(Subscription sub : subs){
			SignalkRouteFactory.removeSubscribeTimer(routeManager, sub);
		}
		
	}
	
	public static void removeSubscribeTimer(RouteManager routeManager, Subscription sub) throws Exception {
		
			logger.debug("Removing sub "+sub);
			((DefaultCamelContext)CamelContextFactory.getInstance()).stopRoute("sub_"+sub.getWsSession()+"_"+sub.getPath());
			((DefaultCamelContext)CamelContextFactory.getInstance()).removeRoute("sub_"+sub.getWsSession()+"_"+sub.getPath());
		
	}
		
	
}
