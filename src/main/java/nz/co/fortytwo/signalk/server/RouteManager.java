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
import java.net.MalformedURLException;
import java.util.Properties;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.processor.OutputFilterProcessor;
import nz.co.fortytwo.signalk.processor.RestApiProcessor;
import nz.co.fortytwo.signalk.util.Constants;
import nz.co.fortytwo.signalk.util.JsonConstants;
import nz.co.fortytwo.signalk.util.JsonSerializer;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.stomp.SkStompComponent;
import org.apache.camel.component.websocket.SignalkWebsocketComponent;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;

/**
 * Main camel route definition to handle input to signalk
 * 
 * 
 * <ul>
 * <li>Basically all input is added to seda:input
 * <li>Message is converted to hashmap, processed,added to signalk model
 * <li>Output is sent out 1 sec.
 * </ul>
 * 
 * 
 * @author robert
 * 
 */
public class RouteManager extends RouteBuilder {
	private static Logger logger = Logger.getLogger(RouteManager.class);
	
	public static final String SEDA_INPUT = "seda:inputData?purgeWhenStopping=true&size=100";
	public static final String SEDA_WEBSOCKETS = "seda:websockets?purgeWhenStopping=true&size=100";
	public static final String DIRECT_STOMP = "direct:stomp";
	public static final String DIRECT_MQTT = "direct:mqtt";
	public static final String DIRECT_TCP = "seda:tcp?purgeWhenStopping=true&size=100";
	public static final String REMOTE_ADDRESS = "remote.address";
	public static final String SEDA_NMEA = "seda:nmeaOutput?purgeWhenStopping=true&size=100";
	public static final String SEDA_COMMON_OUT = "seda:commonOut?purgeWhenStopping=true&size=100";

	public static final String STOMP = "skStomp:queue:signalk?brokerURL=tcp://0.0.0.0:"+Util.getConfigProperty(Constants.STOMP_PORT);
	public static final String MQTT = "mqtt:signalk?host=tcp://0.0.0.0:"+Util.getConfigProperty(Constants.MQTT_PORT);

	
	private int wsPort = 3000;
	private int restPort = 8080;
	private String streamUrl;
	
	private SerialPortManager serialPortManager;
    
	private SignalKModel signalkModel=SignalKModelFactory.getInstance();
	
	private NettyServer skServer;
	private NettyServer nmeaServer;

	protected RouteManager() {
		signalkModel.getData().clear();
		// web socket on port 3000
		logger.info("  Websocket port:"+Util.getConfigProperty(Constants.WEBSOCKET_PORT));
		wsPort=Util.getConfigPropertyInt(Constants.WEBSOCKET_PORT);
		logger.info("  Signalk REST API port:"+Util.getConfigProperty(Constants.REST_PORT));
		restPort=Util.getConfigPropertyInt(Constants.REST_PORT);
		//are we running demo?
		if (Boolean.valueOf(Util.getConfigProperty(Constants.DEMO))) {
			logger.info("  Demo streaming url:"+Util.getConfigProperty(Constants.STREAM_URL));
			setStreamUrl(Util.getConfigProperty(Constants.STREAM_URL));
		}
	}

	@Override
	public void configure() throws Exception {
		configure0();
	}
	public void configure0() throws Exception {
		
		SignalKModelFactory.load(signalkModel);
		
		
		//set shutdown quickly, 5 min is too long
		CamelContextFactory.getInstance().getShutdownStrategy().setShutdownNowOnTimeout(true);
		CamelContextFactory.getInstance().getShutdownStrategy().setTimeout(10);
		
		// init processors who depend on this being started
		// dump nulls, but avoid quartz jobs
		//List<Predicate> predicates = new ArrayList<Predicate>();
		//predicates.add(header(Exchange.TIMER_FIRED_TIME).isNull());
		//predicates.add(header(RestletConstants.RESTLET_REQUEST).isNull());
		//predicates.add(body().isNull());
		//Predicate stopNull = PredicateBuilder.and(predicates);
		//intercept().when(stopNull).stop();
		
		
		skServer = new NettyServer(null, Constants.OUTPUT_TCP);
		skServer.setTcpPort(Util.getConfigPropertyInt(Constants.TCP_PORT));
		skServer.setUdpPort(Util.getConfigPropertyInt(Constants.UDP_PORT));
		skServer.run();
		
		nmeaServer = new NettyServer(null, Constants.OUTPUT_NMEA);
		nmeaServer.setTcpPort(Util.getConfigPropertyInt(Constants.TCP_NMEA_PORT));
		nmeaServer.setUdpPort(Util.getConfigPropertyInt(Constants.UDP_NMEA_PORT));
		nmeaServer.run();
		
		// start a serial port manager
		serialPortManager = new SerialPortManager();
		new Thread(serialPortManager).start();
		
		// main input to destination route
		// put all input into signalk model 
		SignalkRouteFactory.configureInputRoute(this, SEDA_INPUT);
		
		File htmlRoot = new File(Util.getConfigProperty(Constants.STATIC_DIR));
		log.info("Serving static files from "+htmlRoot.getAbsolutePath());
		
		//restlet
		
		ResourceHandler staticHandler = new ResourceHandler();
		staticHandler.setResourceBase(Util.getConfigProperty(Constants.STATIC_DIR));
		
		//bind in registry
		PropertyPlaceholderDelegateRegistry registry = (PropertyPlaceholderDelegateRegistry) CamelContextFactory.getInstance().getRegistry(); 
		//static files
		((JndiRegistry)registry.getRegistry()).bind("staticHandler",staticHandler );
		
		//websockets
		if(CamelContextFactory.getInstance().getComponent("skWebsocket")==null){
			CamelContextFactory.getInstance().addComponent("skWebsocket", new SignalkWebsocketComponent());
		}
		//STOMP
		if(CamelContextFactory.getInstance().getComponent("skStomp")==null){
			CamelContextFactory.getInstance().addComponent("skStomp", new SkStompComponent());
		}
		
		//setup routes
		SignalkRouteFactory.configureWebsocketTxRoute(this, SEDA_WEBSOCKETS, wsPort);
		SignalkRouteFactory.configureWebsocketRxRoute(this, SEDA_INPUT, wsPort);
		
		SignalkRouteFactory.configureTcpServerRoute(this, DIRECT_TCP, skServer, Constants.OUTPUT_TCP);
		SignalkRouteFactory.configureTcpServerRoute(this, SEDA_NMEA, nmeaServer, Constants.OUTPUT_NMEA);
		
		SignalkRouteFactory.configureCommonOut(this);
		
		SignalkRouteFactory.configureHeartbeatRoute(this,"timer://heartbeat?fixedRate=true&period=1000");
		
		SignalkRouteFactory.configureRestRoute(this, "jetty:http://0.0.0.0:" + restPort + JsonConstants.SIGNALK_API+"?sessionSupport=true&matchOnUriPrefix=true&handlers=#staticHandler&enableJMX=true");//&handlers=#mapHandler,#staticHandler
		SignalkRouteFactory.configureAuthRoute(this, "jetty:http://0.0.0.0:" + restPort + JsonConstants.SIGNALK_AUTH+"?sessionSupport=true&matchOnUriPrefix=true&enableJMX=true");
		
		if("true".equals(Util.getConfigProperty(Constants.ALLOW_INSTALL))){
			SignalkRouteFactory.configureInstallRoute(this, "jetty:http://0.0.0.0:" + restPort + JsonConstants.SIGNALK_INSTALL+"?sessionSupport=true&matchOnUriPrefix=true&enableJMX=true", "REST Install");
		}
		
		if("true".equals(Util.getConfigProperty(Constants.ALLOW_UPGRADE))){
			SignalkRouteFactory.configureInstallRoute(this, "jetty:http://0.0.0.0:" + restPort + JsonConstants.SIGNALK_UPGRADE+"?sessionSupport=true&matchOnUriPrefix=true&enableJMX=true", "REST Upgrade");
		}
		
		// timed actions
		SignalkRouteFactory.configureBackgroundTimer(this, "timer://background?fixedRate=true&period=60000");
		SignalkRouteFactory.configureWindTimer(this, "timer://wind?fixedRate=true&period=1000");
		SignalkRouteFactory.configureAnchorWatchTimer(this, "timer://anchorWatch?fixedRate=true&period=1000");
		SignalkRouteFactory.configureAlarmsTimer(this, "timer://alarms?fixedRate=true&period=1000");
		
		if("true".equals(Util.getConfigProperty(Constants.GENERATE_NMEA0183))){
			SignalkRouteFactory.configureNMEA0183Timer(this, "timer://nmea0183?fixedRate=true&period=1000");
		}
		//STOMP
		if("true".equals(Util.getConfigProperty(Constants.START_STOMP))){
			from("skStomp:queue:signalk.put").id("STOMP In")
				.setHeader(Constants.OUTPUT_TYPE, constant(Constants.OUTPUT_STOMP))
				.to(SEDA_INPUT).id(SignalkRouteFactory.getName("SEDA_INPUT"));
		}
		//MQTT
		if("true".equals(Util.getConfigProperty(Constants.START_MQTT))){
			from(MQTT+"&subscribeTopicName=signalk.put").id("MQTT In")
				.transform(body().convertToString())
				.setHeader(Constants.OUTPUT_TYPE, constant(Constants.OUTPUT_MQTT))
				.to(SEDA_INPUT).id(SignalkRouteFactory.getName("SEDA_INPUT"));
		}
		//start any clients if they exist
		//TCP
		String tcpClients = Util.getConfigProperty(Constants.CLIENT_TCP);
		if(StringUtils.isNotBlank(tcpClients)){
			//set up listeners
			String[] clients = tcpClients.split(",");
			for(String client: clients){
				from("netty4:tcp://"+client+"?clientMode=true&textline=true").id("TCP Client:"+client)
					.onException(Exception.class).handled(true).maximumRedeliveries(0)
						.to("log:nz.co.fortytwo.signalk.client.tcp?level=ERROR&showException=true&showStackTrace=true")
						.end().transform(body().convertToString())
					.to(SEDA_INPUT);
			}
		}
		//MQTT
		String mqttClients = Util.getConfigProperty(Constants.CLIENT_MQTT);
		if(StringUtils.isNotBlank(mqttClients)){
			//set up listeners
			String[] clients = mqttClients.split(",");
			for(String client: clients){
				from("mqtt://"+client).id("MQTT Client:"+client)
					.onException(Exception.class).handled(true).maximumRedeliveries(0)
						.to("log:nz.co.fortytwo.signalk.client.mqtt?level=ERROR&showException=true&showStackTrace=true")
						.end().transform(body().convertToString())
					.to(SEDA_INPUT);
			}
		}
		
		
		//Demo mode
		if (Boolean.valueOf(Util.getConfigProperty(Constants.DEMO))) {
			from("file://./src/test/resources/samples/?move=done&fileName=" + streamUrl).id("demo feed")
				.onException(Exception.class).handled(true).maximumRedeliveries(0)
				.to("log:nz.co.fortytwo.signalk.model.receive?level=ERROR&showException=true&showStackTrace=true")
				.end()
			.split(body().tokenize("\n")).streaming()
			.transform(body().convertToString())
			.throttle(50).timePeriodMillis(1000)
			.to(SEDA_INPUT).id(SignalkRouteFactory.getName("SEDA_INPUT"))
			.end();
			
			//and copy it back again to rerun it
			from("file://./src/test/resources/samples/done?fileName=" + streamUrl).id("demo restart")
				.onException(Exception.class).handled(true).maximumRedeliveries(0)
				.end()
			.to("file://./src/test/resources/samples/?fileName=" + streamUrl);
		}
	}

	/**
	 * @return
	 */
	public String getStreamUrl() {
		return streamUrl;
	}

	public void setStreamUrl(String serialUrl) {
		this.streamUrl = serialUrl;
	}

	public void stopNettyServers(){
		if(skServer!=null){
			skServer.shutdownServer();
			skServer=null;
		}
		
		if(nmeaServer!=null){
			nmeaServer.shutdownServer();
			nmeaServer=null;
		}
	}
	/**
	 * When the serial port is used to read from the arduino this must be called to shut
	 * down the readers, which are in their own threads.
	 */
	public void stopSerial() {
		serialPortManager.stopSerial();
		//nmeaTcpServer.stop();
	}
	

}
