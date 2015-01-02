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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.util.Constants;
import nz.co.fortytwo.signalk.server.util.JsonConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.restlet.RestletConstants;
import org.apache.camel.component.websocket.SignalkWebsocketComponent;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.handler.ResourceHandler;

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
	
	public static final String SEDA_INPUT = "seda:input";
	public static final String SEDA_WEBSOCKETS = "seda:websockets";
	public static final String DIRECT_TCP = "direct:tcp";
	private int wsPort = 9292;
	private int restPort = 9290;
	private String streamUrl;
	
	private SerialPortManager serialPortManager;
    
	
	private Properties config;
	private SignalKModel signalkModel=SignalKModelFactory.getInstance();
	private TcpServer tcpServer;

	protected RouteManager(Properties config) {
		this.config = config;
		// web socket on port 9090
		logger.info("  Websocket port:"+config.getProperty(Constants.WEBSOCKET_PORT));
		setWsPort(Integer.valueOf(config.getProperty(Constants.WEBSOCKET_PORT)));
		logger.info("  Signalk REST API port:"+config.getProperty(Constants.REST_PORT));
		setRestPort(Integer.valueOf(config.getProperty(Constants.REST_PORT)));
	}

	public int getWsPort() {
		return wsPort;
	}

	public void setWsPort(int port) {
		this.wsPort = port;
	}

	@Override
	public void configure() throws Exception {
		
		File jsonFile = new File("./conf/self.json");
		log.info("Checking for previous state: "+jsonFile.getAbsolutePath());
		if(jsonFile.exists()){
			try{
				
				Json temp = Json.read(jsonFile.toURI().toURL());
				signalkModel.merge(temp);
				log.info("   Saved state found");
			}catch(Exception ex){
				System.out.println(ex.getMessage());
			}
		}else{
			log.info("   Saved state not found");
		}
		
		// init processors who depend on this being started
		

		// dump nulls, but avoid quartz jobs
		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(header(Exchange.TIMER_FIRED_TIME).isNull());
		predicates.add(header(RestletConstants.RESTLET_REQUEST).isNull());
		predicates.add(body().isNull());
		Predicate stopNull = PredicateBuilder.and(predicates);
		//intercept().when(stopNull).stop();

		//if (Boolean.valueOf(config.getProperty(Constants.DEMO))) {
		//("stream:file?fileName=" + streamUrl).to(SEDA_INPUT);
		//}
		tcpServer = new TcpServer();
		tcpServer.start();
		// start a serial port manager
		serialPortManager = new SerialPortManager();
		new Thread(serialPortManager).start();
		
		// main input to destination route
		// send input to listeners
		SignalkRouteFactory.configureInputRoute(this, SEDA_INPUT);
		
		File htmlRoot = new File(config.getProperty(Constants.STATIC_DIR));
		log.info("Serving static files from "+htmlRoot.getAbsolutePath());
		
		//restlet
		ResourceHandler staticHandler = new ResourceHandler();
		staticHandler.setResourceBase(config.getProperty(Constants.STATIC_DIR));
		//bind in registry
		PropertyPlaceholderDelegateRegistry registry = (PropertyPlaceholderDelegateRegistry) CamelContextFactory.getInstance().getRegistry(); 
		((JndiRegistry)registry.getRegistry()).bind("staticHandler",staticHandler);
		
		CamelContextFactory.getInstance().addComponent("skWebsocket", new SignalkWebsocketComponent());
		
		SignalkRouteFactory.configureWebsocketTxRoute(this, SEDA_WEBSOCKETS, wsPort);
		SignalkRouteFactory.configureWebsocketRxRoute(this, SEDA_INPUT, wsPort);
		SignalkRouteFactory.configureTcpServerRoute(this, DIRECT_TCP, tcpServer);
		
		SignalkRouteFactory.configureRestRoute(this, "jetty:http://0.0.0.0:" + restPort + JsonConstants.SIGNALK_API+"?sessionSupport=true&matchOnUriPrefix=true");//&handlers=#staticHandler
		SignalkRouteFactory.configureAuthRoute(this, "jetty:http://0.0.0.0:" + restPort + JsonConstants.SIGNALK_AUTH+"?sessionSupport=true&matchOnUriPrefix=true");
		SignalkRouteFactory.configureSubscribeRoute(this, "jetty:http://0.0.0.0:" + restPort + JsonConstants.SIGNALK_SUBSCRIBE+"?sessionSupport=true&matchOnUriPrefix=true");
		
		// timed actions
		SignalkRouteFactory.configureDeclinationTimer(this, "timer://declination?fixedRate=true&period=10000");
		SignalkRouteFactory.configureWindTimer(this, "timer://wind?fixedRate=true&period=1000");
		SignalkRouteFactory.configureOutputTimer(this, "timer://signalkAll?fixedRate=true&period=1000");
		//SignalkRouteFactory.configureOutputTimer(this, "timer://subscribe"++"?fixedRate=true&period="+period,wsSession);
		
		//WebsocketEndpoint wsEndpoint = (WebsocketEndpoint) getContext().getEndpoint("websocket://0.0.0.0:"+wsPort+JsonConstants.SIGNALK_WS);
		
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

	/**
	 * When the serial port is used to read from the arduino this must be called to shut
	 * down the readers, which are in their own threads.
	 */
	public void stopSerial() {
		serialPortManager.stopSerial();
		//nmeaTcpServer.stop();
	}

	public int getRestPort() {
		return restPort;
	}

	public void setRestPort(int restPort) {
		this.restPort = restPort;
	}
	

}
