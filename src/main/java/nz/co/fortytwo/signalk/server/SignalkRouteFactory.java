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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.http.HttpServletResponse;

import nz.co.fortytwo.signalk.processor.AISProcessor;
import nz.co.fortytwo.signalk.processor.AlarmProcessor;
import nz.co.fortytwo.signalk.processor.AnchorWatchProcessor;
import nz.co.fortytwo.signalk.processor.ClientAppProcessor;
import nz.co.fortytwo.signalk.processor.ConfigFilterProcessor;
import nz.co.fortytwo.signalk.processor.DeclinationProcessor;
import nz.co.fortytwo.signalk.processor.DeltaImportProcessor;
import nz.co.fortytwo.signalk.processor.FullExportProcessor;
import nz.co.fortytwo.signalk.processor.FullImportProcessor;
import nz.co.fortytwo.signalk.processor.FullToDeltaProcessor;
import nz.co.fortytwo.signalk.processor.HeartbeatProcessor;
import nz.co.fortytwo.signalk.processor.IncomingSecurityFilter;
import nz.co.fortytwo.signalk.processor.InputFilterProcessor;
import nz.co.fortytwo.signalk.processor.JsonGetProcessor;
import nz.co.fortytwo.signalk.processor.JsonListProcessor;
import nz.co.fortytwo.signalk.processor.JsonSubscribeProcessor;
import nz.co.fortytwo.signalk.processor.LoggerProcessor;
import nz.co.fortytwo.signalk.processor.MapToJsonProcessor;
import nz.co.fortytwo.signalk.processor.MqttProcessor;
import nz.co.fortytwo.signalk.processor.N2KProcessor;
import nz.co.fortytwo.signalk.processor.NMEA0183ExportProcessor;
import nz.co.fortytwo.signalk.processor.NMEAProcessor;
import nz.co.fortytwo.signalk.processor.OutputFilterProcessor;
import nz.co.fortytwo.signalk.processor.RestApiProcessor;
import nz.co.fortytwo.signalk.processor.RestAuthProcessor;
import nz.co.fortytwo.signalk.processor.RestPathFilterProcessor;
import nz.co.fortytwo.signalk.processor.SaveProcessor;
import nz.co.fortytwo.signalk.processor.SignalkModelProcessor;
import nz.co.fortytwo.signalk.processor.SourceRefToSourceProcessor;
import nz.co.fortytwo.signalk.processor.SourceToSourceRefProcessor;
import nz.co.fortytwo.signalk.processor.StompProcessor;
import nz.co.fortytwo.signalk.processor.StorageProcessor;
import nz.co.fortytwo.signalk.processor.TrackProcessor;
import nz.co.fortytwo.signalk.processor.UploadProcessor;
import nz.co.fortytwo.signalk.processor.ValidationProcessor;
import nz.co.fortytwo.signalk.processor.WindProcessor;
import nz.co.fortytwo.signalk.processor.WsSessionProcessor;
import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.SignalKConstants;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.component.websocket.WebsocketEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import mjson.Json;



public class SignalkRouteFactory {

	private static Logger logger = LogManager.getLogger(SignalkRouteFactory.class);
	private static Set<String> nameSet = new HashSet<String>();
	/**
	 * Configures a route for all input traffic, which will parse the traffic and update the signalk model
	 * @param routeBuilder
	 * @param input
	 * @param inputFilterProcessor
	 * @param nmeaProcessor
	 * @param aisProcessor
	 * @param signalkModelProcessor
	 * @throws IOException 
	 * @throws Exception 
	 */
	public static void configureInputRoute(RouteBuilder routeBuilder,String input) throws Exception {
		routeBuilder.from(input).id(getName("INPUT"))
			.onException(Exception.class).handled(true).maximumRedeliveries(0)
			.to("log:nz.co.fortytwo.signalk.model.receive?level=ERROR&showException=true&showStackTrace=true")
			.end()
		// dump misc rubbish
		.process(new InputFilterProcessor()).id(getName(InputFilterProcessor.class.getSimpleName()))
		//now filter security
		//.process(new IncomingSecurityFilter()).id(getName(IncomingSecurityFilter.class.getSimpleName()))
		//swap payloads to storage
		//.process(new StorageProcessor()).id(getName(StorageProcessor.class.getSimpleName()))
		//convert NMEA to signalk
		.process(new NMEAProcessor()).id(getName(NMEAProcessor.class.getSimpleName()))
		//convert AIS to signalk
		.process(new AISProcessor()).id(getName(AISProcessor.class.getSimpleName()))
		//convert n2k
		.process(new N2KProcessor()).id(getName(N2KProcessor.class.getSimpleName()))
		//handle subscribe messages
		.process(new JsonSubscribeProcessor()).id(getName(JsonSubscribeProcessor.class.getSimpleName()))
		//deal with delta format
		.process(new DeltaImportProcessor()).id(getName(DeltaImportProcessor.class.getSimpleName()))
		//deal with full format
		.process(new FullImportProcessor()).id(getName(FullImportProcessor.class.getSimpleName()))
		//make sure we have timestamp/source
		.process(new ValidationProcessor()).id(getName(ValidationProcessor.class.getSimpleName()))
		//record track
		.process(new TrackProcessor()).id(getName(TrackProcessor.class.getSimpleName()))
		//push source to sources and add $source
		.process(new SourceToSourceRefProcessor()).id(getName(SourceToSourceRefProcessor.class.getSimpleName()))
		//and update signalk model
		.process(new SignalkModelProcessor()).id(getName(SignalkModelProcessor.class.getSimpleName()))
		//we have processed all incoming data now - if there is more left its LIST, GET.
		//handle list
		.process(new JsonListProcessor()).id(getName(JsonListProcessor.class.getSimpleName()))
		//handle get
		.process(new JsonGetProcessor()).id(getName(JsonGetProcessor.class.getSimpleName()));
		//.process(new StorageProcessor()).id(getName(StorageProcessor.class.getSimpleName()));
		
	}
	
	/**
	 * Configures the route for output to websockets
	 * @param routeBuilder
	 * @param input
	 */
	public static void configureWebsocketTxRoute(RouteBuilder routeBuilder ,String input, int port){
		Predicate p1 = routeBuilder.header(ConfigConstants.OUTPUT_TYPE).isEqualTo(ConfigConstants.OUTPUT_WS);
		Predicate p2 = routeBuilder.header(WebsocketConstants.CONNECTION_KEY).isEqualTo(WebsocketConstants.SEND_TO_ALL);
		//from SEDA_WEBSOCKETS
			routeBuilder.from(input).id(getName("Websocket Tx"))
				.onException(Exception.class)
				.handled(true)
				.maximumRedeliveries(0)
				.to("log:nz.co.fortytwo.signalk.model.websocket.tx?level=ERROR&showException=true&showStackTrace=true")
				.end()
			.filter(PredicateBuilder.or(p1, p2))
			.to("skWebsocket://0.0.0.0:"+port+SignalKConstants.SIGNALK_WS).id(getName("Websocket Client"));
		
	}
	/**
	 * Configures the route for input to websockets
	 * @param routeBuilder
	 * @param input
	 * @throws Exception 
	 */
	public static void configureWebsocketRxRoute(RouteBuilder routeBuilder ,String input, int port)  {
		
		WebsocketEndpoint wsEndpoint = (WebsocketEndpoint) routeBuilder.getContext().getEndpoint("skWebsocket://0.0.0.0:"+port+SignalKConstants.SIGNALK_WS);
		wsEndpoint.setEnableJmx(true);
		wsEndpoint.setSessionSupport(true);
		wsEndpoint.setCrossOriginFilterOn(true);
		wsEndpoint.setAllowedOrigins("*");
		
		routeBuilder.from(wsEndpoint).id(getName("Websocket Rx"))
			.onException(Exception.class)
			.handled(true).maximumRedeliveries(0)
			.to("log:nz.co.fortytwo.signalk.model.websocket.rx?level=ERROR&showException=true&showStackTrace=true")
			.end()
		.process(new WsSessionProcessor()).id(getName(WsSessionProcessor.class.getSimpleName()))
		//.to("log:nz.co.fortytwo.signalk.model.websocket.rx?level=INFO&showException=true&showStackTrace=true")
		.to(input).id(getName("SEDA_INPUT"));
		
	}
	public static void configureTcpServerRoute(RouteBuilder routeBuilder ,String input, NettyServer nettyServer, String outputType) throws Exception{
		// push out via TCPServer.
		Predicate p1 = routeBuilder.header(ConfigConstants.OUTPUT_TYPE).isEqualTo(outputType);
		Predicate p2 = routeBuilder.header(WebsocketConstants.CONNECTION_KEY).isEqualTo(WebsocketConstants.SEND_TO_ALL);
		routeBuilder.from(input).id(getName("Netty "+outputType+" Server"))
			.onException(Exception.class)
			.handled(true)
			.maximumRedeliveries(0)
			.end()
		.filter(PredicateBuilder.or(p1, p2))
		.process((Processor) nettyServer).id(getName(NettyServer.class.getSimpleName())).end();
			
	}
	
	public static void configureRestRoute(RouteBuilder routeBuilder ,String input, String name)throws IOException{
		routeBuilder.from(input).id(getName(name)) //.setExchangePattern(ExchangePattern.InOut);
			.setExchangePattern(ExchangePattern.InOut)
			.process(new RestApiProcessor())
			.to(ExchangePattern.InOut,RouteManager.SEDA_INPUT)
			.process(new ConfigFilterProcessor(false)).id(getName(ConfigFilterProcessor.class.getSimpleName()))
			.process(new RestPathFilterProcessor()).id(getName(RestPathFilterProcessor.class.getSimpleName()));
		
				
//		routeBuilder.rest(SignalKConstants.SIGNALK_API).id("REST POST Client")
//			    	.post("/")
//					.to("log:nz.co.fortytwo.signalk.client.rest?level=INFO&showException=true&showStackTrace=true")
//					.to("direct:restTest");

		}
	public static void configureRestLoggerRoute(RouteBuilder routeBuilder ,String input, String name)throws IOException{
		routeBuilder.from(input).id(getName(name)) 
			.setExchangePattern(ExchangePattern.InOut)
			.process(new LoggerProcessor()).id(getName(LoggerProcessor.class.getSimpleName()));
		}
	
	public static void configureRestUploadRoute(RouteBuilder routeBuilder ,String input, String name)throws IOException{
		routeBuilder.rest(input).id(getName(name))
			.post()
			.route().id(getName(name+"Route"))
			.setExchangePattern(ExchangePattern.InOut)
			.process(new UploadProcessor()).id(getName(UploadProcessor.class.getSimpleName()));
		}
	
	public static void configureRestConfigRoute(RouteBuilder routeBuilder ,String input, String name)throws IOException{
		routeBuilder.from(input).id(getName(name)) 
			.setExchangePattern(ExchangePattern.InOut)
			.process(new RestApiProcessor())
			.to(ExchangePattern.InOut,RouteManager.SEDA_INPUT)
			.process(new ConfigFilterProcessor(true)).id(getName(ConfigFilterProcessor.class.getSimpleName()));
		}
	
	public static void configureAuthRoute(RouteBuilder routeBuilder ,String input){
		routeBuilder.from(input).id(getName("REST Authenticate"))
			.setExchangePattern(ExchangePattern.InOut)
			.process(new RestAuthProcessor()).id(getName(RestAuthProcessor.class.getSimpleName()));
		}
	
	public static void configureInstallRoute(RouteBuilder routeBuilder ,String input, String name){
		routeBuilder.from(input).id(getName(name))
			.setExchangePattern(ExchangePattern.InOut)
			.process(new ClientAppProcessor()).id(getName(ClientAppProcessor.class.getSimpleName()));
		}
	
	public static void configureBackgroundTimer(RouteBuilder routeBuilder ,String input){
		routeBuilder.from(input).id(getName("Declination"))
			.process(new SaveProcessor()).id(getName(SaveProcessor.class.getSimpleName()))
			.process(new DeclinationProcessor()).id(getName(DeclinationProcessor.class.getSimpleName()))
			.to("log:nz.co.fortytwo.signalk.model.update?level=DEBUG").end();
	}
	
	public static void configureAlarmsTimer(RouteBuilder routeBuilder ,String input){
		routeBuilder.from(input).id(getName("Alarms"))
			.process(new AlarmProcessor()).id(getName(AlarmProcessor.class.getSimpleName()))
			.to("log:nz.co.fortytwo.signalk.model.update?level=DEBUG").end();
	}
	public static void configureNMEA0183Timer(RouteBuilder routeBuilder ,String input){
		routeBuilder.from(input).id(getName("NMEA0183"))
			.process(new NMEA0183ExportProcessor()).id(getName(NMEA0183ExportProcessor.class.getSimpleName()))
			.to("log:nz.co.fortytwo.signalk.model.update?level=DEBUG").end();
	}
	
	public static void configureAnchorWatchTimer(RouteBuilder routeBuilder ,String input){
		routeBuilder.from(input).id(getName("AnchorWatch"))
			.process(new AnchorWatchProcessor()).id(getName(AnchorWatchProcessor.class.getSimpleName()))
			.to("log:nz.co.fortytwo.signalk.model.update?level=DEBUG").end();
	}
	
	public static void configureWindTimer(RouteBuilder routeBuilder ,String input){
		routeBuilder.from("timer://wind?fixedRate=true&period=1000").id(getName("True Wind"))
			.process(new WindProcessor()).id(getName(WindProcessor.class.getSimpleName()))
			.to("log:nz.co.fortytwo.signalk.model.update?level=DEBUG")
			.end();
	}
	
	public static void configureCommonOut(RouteBuilder routeBuilder ) throws IOException{
		routeBuilder.from(RouteManager.SEDA_COMMON_OUT).id(getName("COMMON_OUT"))
			.onException(Exception.class).handled(true).maximumRedeliveries(0)
			.to("log:nz.co.fortytwo.signalk.model.output?level=ERROR")
			.end()
		.process(new SourceRefToSourceProcessor()).id(getName(SourceRefToSourceProcessor.class.getSimpleName()))
		.process(new MapToJsonProcessor()).id(getName(MapToJsonProcessor.class.getSimpleName()))
		.process(new FullToDeltaProcessor()).id(getName(FullToDeltaProcessor.class.getSimpleName()))
		.split().body()
		//swap payloads from storage
		//.process(new StorageProcessor()).id(getName(InputFilterProcessor.class.getSimpleName()))
		.process(new OutputFilterProcessor()).id(getName(OutputFilterProcessor.class.getSimpleName()))
		.multicast().parallelProcessing()
			.to(RouteManager.DIRECT_TCP,
					RouteManager.SEDA_WEBSOCKETS, 
					RouteManager.DIRECT_MQTT, 
					RouteManager.DIRECT_STOMP,
					"log:nz.co.fortytwo.signalk.model.output?level=DEBUG"
					).id(getName("Multicast Outputs"))
		.end();
		routeBuilder.from(RouteManager.DIRECT_MQTT).id(getName("MQTT out"))
			.filter(routeBuilder.header(ConfigConstants.OUTPUT_TYPE).isEqualTo(ConfigConstants.OUTPUT_MQTT))
			.process(new MqttProcessor()).id(getName(MqttProcessor.class.getSimpleName()))
			.to(RouteManager.MQTT+"?publishTopicName=signalk.dlq").id(getName("MQTT Broker"));
		routeBuilder.from(RouteManager.DIRECT_STOMP).id(getName("STOMP out"))
			.filter(routeBuilder.header(ConfigConstants.OUTPUT_TYPE).isEqualTo(ConfigConstants.OUTPUT_STOMP))
			.process(new StompProcessor()).id(getName(StompProcessor.class.getSimpleName()))
			.to(RouteManager.STOMP).id(getName("STOMP Broker"));
	}
	
	public static void configureSubscribeTimer(RouteBuilder routeBuilder ,Subscription sub) throws Exception{
		String routeId = getRouteId(sub);
		String input = "quartz2://"+routeId+"?trigger.repeatCount=-1&trigger.repeatInterval="+sub.getPeriod();
		logger.info("Configuring route "+input);
		//if(logger.isDebugEnabled())logger.debug("Configuring route "+input);
		String wsSession = sub.getWsSession();
		RouteDefinition route = routeBuilder.from(input);
			route.process(new FullExportProcessor(wsSession,routeId)).id(FullExportProcessor.class.getSimpleName()+"-"+routeId)
				.onException(Exception.class).handled(true).maximumRedeliveries(0)
				.to("log:nz.co.fortytwo.signalk.model.output.subscribe?level=ERROR&maxChars=1000")
				.end()
			.setHeader(WebsocketConstants.CONNECTION_KEY, routeBuilder.constant(wsSession))
			.to(RouteManager.SEDA_COMMON_OUT).id(getName("SEDA_COMMON_OUT"))
			.end();
		route.setId(routeId);
		((DefaultCamelContext)CamelContextFactory.getInstance()).addRouteDefinition(route);
		((DefaultCamelContext)CamelContextFactory.getInstance()).startRoute(route.getId());
		//routeBuilder.getContext().startAllRoutes();
	}

	private static String getRouteId(Subscription sub) {
		if(StringUtils.isBlank(sub.getRouteId())){
			String routeId = getName("sub_"+sub.getWsSession());
			sub.setRouteId(routeId);
		}
		
		return sub.getRouteId();
	}


	public static void removeSubscribeTimers(RouteManager routeManager, ConcurrentLinkedQueue<Subscription> subs) throws Exception {
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
		
		routeBuilder.from(input).id(getName("Heartbeat"))
			.onException(Exception.class).handled(true).maximumRedeliveries(0)
			.to("log:nz.co.fortytwo.signalk.model.output.all?level=ERROR")
			.end()
		.process(new HeartbeatProcessor()).id(getName(HeartbeatProcessor.class.getSimpleName()));
		
	}

	public static String getName(String name) {
		int c = 0;
		String tmpName = name;
		while(nameSet.contains(tmpName)){
			tmpName=name+"-"+c;
			c++;
		}
		nameSet.add(tmpName);
		return tmpName;
	}

	public static void startLogRoutes(RouteBuilder routeBuilder, String  host, int restPort) {
		//list logs dir
		routeBuilder.from(host + restPort + "/signalk/v1/listLogs?sessionSupport=true&matchOnUriPrefix=true")
					.setExchangePattern(ExchangePattern.InOut)
					.process(new Processor() {
						
						@Override
						public void process(Exchange exchange) throws Exception {
							File dir = new File("signalk-static/logs");
							exchange.getIn().setBody(Json.array(dir.list()));
						}
					});
				
		routeBuilder.from(host + restPort + "/signalk/v1/getLogs?sessionSupport=true&matchOnUriPrefix=true")
				.setExchangePattern(ExchangePattern.InOut)
				.process(new Processor() {
					
					@Override
					public void process(Exchange exchange) throws Exception {
						
						String logFile = exchange.getIn().getHeader("logFile", String.class);
						if(logFile.contains("/")){
							logFile=logFile.substring(logFile.lastIndexOf("/")+1, logFile.length());
						}
						exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/plain");
						if(StringUtils.isBlank(logFile)){
							
							exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE,
									HttpServletResponse.SC_BAD_REQUEST);
							exchange.getIn().setBody("Bad request");
						}
						File dir = new File("signalk-static/logs/"+logFile);
						exchange.getIn().setBody(FileUtils.readFileToString(dir));
					}
				});
		
	}
		
	
}
