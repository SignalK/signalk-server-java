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
package nz.co.fortytwo.signalk.model;

import nz.co.fortytwo.signalk.processor.AISProcessor;
import nz.co.fortytwo.signalk.processor.DeclinationProcessor;
import nz.co.fortytwo.signalk.processor.DeltaExportProcessor;
import nz.co.fortytwo.signalk.processor.DeltaImportProcessor;
import nz.co.fortytwo.signalk.processor.InputFilterProcessor;
import nz.co.fortytwo.signalk.processor.NMEAProcessor;
import nz.co.fortytwo.signalk.processor.OutputFilterProcessor;
import nz.co.fortytwo.signalk.processor.RestAuthProcessor;
import nz.co.fortytwo.signalk.processor.RestProcessor;
import nz.co.fortytwo.signalk.processor.SignalkModelProcessor;
import nz.co.fortytwo.signalk.processor.ValidationProcessor;
import nz.co.fortytwo.signalk.processor.WindProcessor;
import nz.co.fortytwo.signalk.server.SignalKReceiver;
import nz.co.fortytwo.signalk.server.TcpServer;
import nz.co.fortytwo.signalk.server.util.JsonConstants;

import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang3.StringUtils;



public class SignalkRouteFactory {

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
	public static void configureWebsocketTxRoute(RouteBuilder routeBuilder ,String input, int port, String staticResources){
		if(StringUtils.isBlank(staticResources)){
			routeBuilder.from(input).onException(Exception.class).handled(true).maximumRedeliveries(0)
			.process(new OutputFilterProcessor())
			.to("log:nz.co.fortytwo.signalk.model.websocket.tx?level=ERROR&showException=true&showStackTrace=true").end()
			.to("websocket://0.0.0.0:"+port+JsonConstants.SIGNALK_WS+"?sendToAll=true");
		}else{
			routeBuilder.from(input).onException(Exception.class).handled(true).maximumRedeliveries(0)
			.process(new OutputFilterProcessor())
			.to("log:nz.co.fortytwo.signalk.model.websocket.tx?level=ERROR&showException=true&showStackTrace=true").end()
			.to("websocket://0.0.0.0:"+port+JsonConstants.SIGNALK_WS+"?sendToAll=true&staticResources="+staticResources);
		}
	}
	/**
	 * Configures the route for input to websockets
	 * @param routeBuilder
	 * @param input
	 */
	public static void configureWebsocketRxRoute(RouteBuilder routeBuilder ,String input, int port){
		routeBuilder.from("websocket://0.0.0.0:"+port+JsonConstants.SIGNALK_WS).onException(Exception.class).handled(true).maximumRedeliveries(0)
		.to("log:nz.co.fortytwo.signalk.model.websocket.rx?level=ERROR&showException=true&showStackTrace=true").end()
		.to(input);
	}
	public static void configureTcpServerRoute(RouteBuilder routeBuilder ,String input, TcpServer tcpServer){
	// push NMEA out via TCPServer.
		routeBuilder.from(input).process((Processor) tcpServer).end();
	}
	
	public static void configureRestRoute(RouteBuilder routeBuilder ,String input){
		routeBuilder.from(input)
			.setExchangePattern(ExchangePattern.InOut)
			.process(new RestProcessor());
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
		routeBuilder.from("timer://signalkAll?fixedRate=true&period=1000")
		.process(new DeltaExportProcessor()).split(routeBuilder.body())
		.to("log:nz.co.fortytwo.signalk.model.output?level=INFO")
		.to(SignalKReceiver.DIRECT_WEBSOCKETS).to(SignalKReceiver.DIRECT_TCP).end();
	}
}
