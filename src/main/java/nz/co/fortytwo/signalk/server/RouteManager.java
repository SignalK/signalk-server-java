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

import static nz.co.fortytwo.signalk.util.ConfigConstants.OUTPUT_XMPP;
//import static nz.co.fortytwo.signalk.util.ConfigConstants.UUID;
import static nz.co.fortytwo.signalk.util.SignalKConstants.FORMAT_DELTA;
import static nz.co.fortytwo.signalk.util.SignalKConstants.MSG_SRC_BUS;
import static nz.co.fortytwo.signalk.util.SignalKConstants.POLICY_IDEAL;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_API;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_AUTH;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_CONFIG;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_DISCOVERY;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_INSTALL;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_LOGGER;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_RESTART;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_UPGRADE;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_UPLOAD;
import static nz.co.fortytwo.signalk.util.SignalKConstants._SIGNALK_WS_TCP_LOCAL;
import static nz.co.fortytwo.signalk.util.SignalKConstants.dot;

import java.io.File;
import java.net.Inet4Address;
import java.util.Arrays;
import java.util.UUID;

import javax.jmdns.JmmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import org.apache.camel.Endpoint;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.ahc.ws.WsEndpoint;
import org.apache.camel.component.stomp.SkStompComponent;
import org.apache.camel.component.websocket.SignalkWebsocketComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smackx.jiveproperties.JivePropertiesManager;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.processor.ClientAppProcessor;
import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.Util;



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
public class RouteManager extends RouteBuilder  {
	protected static final String JETTY_HTTP_0_0_0_0 = "jetty:http://0.0.0.0:";

	
	private static Logger logger = LogManager.getLogger(RouteManager.class);
	
	//public static final String SEDA_INPUT = "seda:inputData?purgeWhenStopping=true&size=1000";
	public static final String SEDA_INPUT = "activemq:queue:inputData?jmsMessageType=Text&timeToLive=10000&asyncConsumer=true&acceptMessagesWhileStopping=true";
	public static final String SEDA_XMPP = "activemq:queue:xmppData?jmsMessageType=Text&timeToLive=10000&asyncConsumer=true&acceptMessagesWhileStopping=true";
	public static final String SEDA_WEBSOCKETS = "seda:websockets?purgeWhenStopping=true&size=1000";
	public static final String DIRECT_STOMP = "direct:stomp";
	public static final String DIRECT_MQTT = "direct:mqtt";
	public static final String DIRECT_XMPP = "direct:xmpp";
	public static final String DIRECT_TCP = "seda:tcp?purgeWhenStopping=true&size=1000";
	
	public static final String SEDA_NMEA = "seda:nmeaOutput?purgeWhenStopping=true&size=100";
	public static final String SEDA_COMMON_OUT = "seda:commonOut?purgeWhenStopping=true&size=100";

	public static final String STOMP = "skStomp:queue:signalk?brokerURL=tcp://0.0.0.0:"+Util.getConfigPropertyInt(ConfigConstants.STOMP_PORT);
	public static final String MQTT = "mqtt:signalk?host=tcp://0.0.0.0:"+Util.getConfigPropertyInt(ConfigConstants.MQTT_PORT);

	
	private int wsPort = 3000;
	private int restPort = 8080;
	//private String streamUrl;
	
	private SerialPortManager serialPortManager;
    
	private SignalKModel signalkModel=SignalKModelFactory.getInstance();
	
	private NettyServer skServer;
	private NettyServer nmeaServer;

	protected RouteManager() {
		
		// web socket on port 3000
		logger.info("  Websocket port:"+Util.getConfigPropertyInt(ConfigConstants.WEBSOCKET_PORT));
		wsPort=Util.getConfigPropertyInt(ConfigConstants.WEBSOCKET_PORT);
		logger.info("  Signalk REST API port:"+Util.getConfigPropertyInt(ConfigConstants.REST_PORT));
		restPort=Util.getConfigPropertyInt(ConfigConstants.REST_PORT);
		
	}

	@Override
	public void configure() throws Exception {
		configure0();
	}
	public void configure0() throws Exception {
		//XMPP
		JivePropertiesManager.setJavaObjectEnabled(true);
		SASLAuthentication.unsupportSASLMechanism("DIGEST-MD5");
		SASLAuthentication.unregisterSASLMechanism("DIGEST-MD5");
		SASLAuthentication.supportSASLMechanism("PLAIN",0);
		//SmackConfiguration.DEBUG_ENABLED=true;
		
		errorHandler(deadLetterChannel("direct:fail")
		        .useOriginalMessage()
		        .maximumRedeliveries(1)
		        .redeliveryDelay(1000));
		
		from ("direct:fail").id("Fail")
        .to("log:log:nz.co.fortytwo.signalk.error?level=ERROR&showAll=true");
		 
		SignalKModelFactory.load(signalkModel);
		
		//set shutdown quickly, 5 min is too long
		CamelContextFactory.getInstance().getShutdownStrategy().setShutdownNowOnTimeout(true);
		CamelContextFactory.getInstance().getShutdownStrategy().setTimeout(10);
		//CamelContextFactory.getInstance().addComponent("activemq", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
		
		//Netty tcp server
		skServer = new NettyServer(null, ConfigConstants.OUTPUT_TCP);
		skServer.setTcpPort(Util.getConfigPropertyInt(ConfigConstants.TCP_PORT));
		skServer.setUdpPort(Util.getConfigPropertyInt(ConfigConstants.UDP_PORT));
		skServer.run();
		
		nmeaServer = new NettyServer(null, ConfigConstants.OUTPUT_NMEA);
		nmeaServer.setTcpPort(Util.getConfigPropertyInt(ConfigConstants.TCP_NMEA_PORT));
		nmeaServer.setUdpPort(Util.getConfigPropertyInt(ConfigConstants.UDP_NMEA_PORT));
		nmeaServer.run();
		
		// start a serial port manager
		if(serialPortManager==null){
			serialPortManager = new SerialPortManager();
		}
		new Thread(serialPortManager).start();
		
	
		// main input to destination route
		// put all input into signalk model 
		SignalkRouteFactory.configureInputRoute(this, SEDA_INPUT);
		
		File htmlRoot = new File(Util.getConfigProperty(ConfigConstants.STATIC_DIR));
		log.info("Serving static files from "+htmlRoot.getAbsolutePath());

		//restlet
		
		//bind in registry
		PropertyPlaceholderDelegateRegistry registry = (PropertyPlaceholderDelegateRegistry) CamelContextFactory.getInstance().getRegistry();
		JndiRegistry reg = (JndiRegistry)registry.getRegistry();
		if(reg.lookup("staticHandler")==null){		
			ResourceHandler staticHandler = new ResourceHandler();
			staticHandler.setResourceBase(Util.getConfigProperty(ConfigConstants.STATIC_DIR));	
			staticHandler.setDirectoriesListed(false);
			MimeTypes mimeTypes = staticHandler.getMimeTypes();
			mimeTypes.addMimeMapping("log", MimeTypes.TEXT_HTML_UTF_8);
			staticHandler.setMimeTypes(mimeTypes);
			
			//static files
			reg.bind("staticHandler",staticHandler );
			
		}
		
		
		restConfiguration().component("jetty")
			.consumerProperty("matchOnUriPrefix", "true")
			.componentProperty("matchOnUriPrefix", "true")
			.host("0.0.0.0").port(8080);
		
		//websockets
		
		if(CamelContextFactory.getInstance().getComponent("skWebsocket")==null){
			SignalkWebsocketComponent skws = new SignalkWebsocketComponent(); 
			CamelContextFactory.getInstance().addComponent("skWebsocket", skws);
		}
		//STOMP
		if(CamelContextFactory.getInstance().getComponent("skStomp")==null){
			CamelContextFactory.getInstance().addComponent("skStomp", new SkStompComponent());
		}
		
		
        
		//setup routes
		SignalkRouteFactory.configureWebsocketTxRoute(this, SEDA_WEBSOCKETS, wsPort);
		SignalkRouteFactory.configureWebsocketRxRoute(this, SEDA_INPUT, wsPort);
		
		SignalkRouteFactory.configureTcpServerRoute(this, DIRECT_TCP, skServer, ConfigConstants.OUTPUT_TCP);
		SignalkRouteFactory.configureTcpServerRoute(this, SEDA_NMEA, nmeaServer, ConfigConstants.OUTPUT_NMEA);
		
		SignalkRouteFactory.configureCommonOut(this);
		
		SignalkRouteFactory.configureHeartbeatRoute(this,"timer://heartbeat?fixedRate=true&period=1000");
		
		SignalkRouteFactory.configureAuthRoute(this, JETTY_HTTP_0_0_0_0 + restPort + SIGNALK_AUTH+"?sessionSupport=true&matchOnUriPrefix=true&handlers=#staticHandler&enableJMX=true&enableCORS=true");
		SignalkRouteFactory.configureRestRoute(this, JETTY_HTTP_0_0_0_0 + restPort + SIGNALK_DISCOVERY+"?sessionSupport=true&matchOnUriPrefix=false&enableJMX=true&enableCORS=true","REST Discovery");
		SignalkRouteFactory.configureRestRoute(this, JETTY_HTTP_0_0_0_0 + restPort + SIGNALK_API+"?sessionSupport=true&matchOnUriPrefix=true&enableJMX=true&enableCORS=true","REST Api");
		SignalkRouteFactory.configureRestConfigRoute(this, JETTY_HTTP_0_0_0_0 + restPort + SIGNALK_CONFIG+"?sessionSupport=true&matchOnUriPrefix=true&enableJMX=true&enableCORS=false","Config Api");
		
		SignalkRouteFactory.configureRestLoggerRoute(this, JETTY_HTTP_0_0_0_0 + restPort + SIGNALK_LOGGER+"?sessionSupport=true&matchOnUriPrefix=true&enableJMX=true&enableCORS=false","Logger");
		
		SignalkRouteFactory.configureRestUploadRoute(this, SIGNALK_UPLOAD,"Upload");

		
		if(Util.getConfigPropertyBoolean(ConfigConstants.ALLOW_INSTALL)){
			SignalkRouteFactory.configureInstallRoute(this, JETTY_HTTP_0_0_0_0 + restPort + SIGNALK_INSTALL+"?sessionSupport=true&matchOnUriPrefix=true&enableJMX=true", "REST Install");
		}
		
		if(Util.getConfigPropertyBoolean(ConfigConstants.ALLOW_UPGRADE)){
			SignalkRouteFactory.configureInstallRoute(this, JETTY_HTTP_0_0_0_0 + restPort + SIGNALK_UPGRADE+"?sessionSupport=true&matchOnUriPrefix=true&enableJMX=true", "REST Upgrade");
		}
		
		// timed actions
		SignalkRouteFactory.configureBackgroundTimer(this, "timer://background?fixedRate=true&period=60000");
		SignalkRouteFactory.configureWindTimer(this, "timer://wind?fixedRate=true&period=1000");
		SignalkRouteFactory.configureAnchorWatchTimer(this, "timer://anchorWatch?fixedRate=true&period=5000");
		SignalkRouteFactory.configureAlarmsTimer(this, "timer://alarms?fixedRate=true&period=1000");
		
		if(Util.getConfigPropertyBoolean(ConfigConstants.GENERATE_NMEA0183)){
			SignalkRouteFactory.configureNMEA0183Timer(this, "timer://nmea0183?fixedRate=true&period=1000");
		}
		//STOMP
		if(Util.getConfigPropertyBoolean(ConfigConstants.START_STOMP)){
			from("skStomp:queue:signalk.put").id("STOMP In")
				.setHeader(ConfigConstants.OUTPUT_TYPE, constant(ConfigConstants.OUTPUT_STOMP))
				.setHeader(MSG_SRC_BUS, constant("stomp.queue:signalk.put"))
				.to(SEDA_INPUT).id(SignalkRouteFactory.getName("SEDA_INPUT"));
		}
		//MQTT
		if(Util.getConfigPropertyBoolean(ConfigConstants.START_MQTT)){
			from(MQTT+"&subscribeTopicName=signalk.put").id("MQTT In")
				.transform(body().convertToString())
				.setHeader(ConfigConstants.OUTPUT_TYPE, constant(ConfigConstants.OUTPUT_MQTT))
				.setHeader(MSG_SRC_BUS, constant("mqtt.queue:signalk.put"))
				.to(SEDA_INPUT).id(SignalkRouteFactory.getName("SEDA_INPUT"));
		}
		//start any clients if they exist
		//WS
		Json wsClients = Util.getConfigJsonArray(ConfigConstants.CLIENT_WS);
		logger.info("  Starting WS connection to url:"+wsClients);
		if(wsClients!=null){
			for(Object client: wsClients.asList()){
				logger.info("  Starting WS connection to url:ahc-ws://"+client);
				WsEndpoint wsEndpoint = (WsEndpoint)getContext().getEndpoint("ahc-ws://"+client);
				setupClient("ahc-ws://"+client,client.toString(),"ws");
				wsEndpoint.connect();
			}
		}
		//TCP
		Json tcpClients = Util.getConfigJsonArray(ConfigConstants.CLIENT_TCP);
		if(tcpClients!=null){
			for(Object client: tcpClients.asList()){
				setupClient("netty4:tcp://"+client+"?clientMode=true&textline=true",client.toString(),"tcp");
			}
		}
		//MQTT
			Json mqttClients = Util.getConfigJsonArray(ConfigConstants.CLIENT_MQTT);
			if(mqttClients!=null){
				for(Object client: mqttClients.asList()){
					setupClient("mqtt://"+client,client.toString(),"mqtt");
				}
			}
		
		//STOMP
		//TODO: test stomp client actually works!
		Json stompClients = Util.getConfigJsonArray(ConfigConstants.CLIENT_STOMP);
		if(stompClients!=null){
			for(Object client: stompClients.asList()){
				setupClient("stomp://"+client,client.toString(),"stomp");
			}
		}
		
		//XMPP
		//"xmpp": [{"server":"xmpp.www.42.co.nz","passwd":"motu","user":"motu","room":"signalk"}]
		Json xmppClients = Util.getConfigJsonArray(ConfigConstants.XMPP);
		if(xmppClients!=null){
			for(Json client: xmppClients.asJsonList()){
				String server = client.at("server").asString();
				String user = client.at("user").asString();
				String passwd = client.at("passwd").asString();
				String room = client.at("room").asString();
				String filter = client.at("filter").asString();
				Endpoint xmppEndpoint = getContext().getEndpoint("xmpp://"+server+"?testConnectionOnStartup=false&room="+room+"&user="+user+"&password="+passwd+"&resource="+user+"&serviceName="+server);
				//receive
				setupClient(xmppEndpoint,server+dot+room,"xmpp");
				//tx
				from(SEDA_XMPP+"&selector="+ConfigConstants.DESTINATION+" %3D '"+room+"'").id("XMPP out: "+room)
					.convertBodyTo(String.class)
					.to(xmppEndpoint).id("XMPP Service:"+room);
				//and subscribe
				String wsSession = UUID.randomUUID().toString();
				for(String f:filter.split(",")){
					Subscription sub = new Subscription(wsSession, f, 5000, 1000, FORMAT_DELTA, POLICY_IDEAL);
					sub.setDestination(room);
					SubscriptionManagerFactory.getInstance().add(wsSession, wsSession,OUTPUT_XMPP,Inet4Address.getLocalHost().getHostAddress(), Inet4Address.getByName(server).getHostAddress());
					SubscriptionManagerFactory.getInstance().addSubscription(sub);
				}
			}
		}
		//restart support
		from(JETTY_HTTP_0_0_0_0 + restPort + SIGNALK_RESTART).id("Restart route")
			.setExchangePattern(ExchangePattern.InOut)
			.setBody(constant("Restarting now.."))
			.to("file://./conf/?fileName=signalk-restart");
		
		
		//Demo mode
		if (Util.getConfigPropertyBoolean(ConfigConstants.DEMO)) {
			String streamUrl = Util.getConfigProperty(ConfigConstants.STREAM_URL);
			logger.info("  Demo streaming url:"+Util.getConfigProperty(ConfigConstants.STREAM_URL));
			from("file://./src/test/resources/samples/?move=done&fileName=" + streamUrl).id("demo feed")
				.onException(Exception.class).handled(true).maximumRedeliveries(0)
				.to("log:nz.co.fortytwo.signalk.model.receive?level=ERROR&showException=true&showStackTrace=true")
				.end()
			.split(body().tokenize("\n")).streaming()
			.convertBodyTo(String.class)
			.throttle(2).timePeriodMillis(1000)
			.setHeader(MSG_SRC_BUS, constant("demo"))
			.to(SEDA_INPUT).id(SignalkRouteFactory.getName("SEDA_INPUT"))
			.end();
			
			//and copy it back again to rerun it
			from("file://./src/test/resources/samples/done?fileName=" + streamUrl).id("demo restart")
				.onException(Exception.class).handled(true).maximumRedeliveries(0)
				.end()
			.to("file://./src/test/resources/samples/?fileName=" + streamUrl);
		}
		
		SignalkRouteFactory.startLogRoutes(this, JETTY_HTTP_0_0_0_0, restPort);
		
		if (Util.getConfigPropertyBoolean(ConfigConstants.ZEROCONF_AUTO)) {
			startMdnsAutoconnect();
		}
	}

	private void setupClient(String endpoint, String client, String serviceName) {
		setupClient(getContext().getEndpoint(endpoint), client, serviceName);
	}
	
	private void setupClient(Endpoint endpoint, String client, String serviceName) {
		from(endpoint).id(serviceName.toUpperCase()+" Client:"+client)
			.onException(Exception.class).handled(true).maximumRedeliveries(0)
				.to("log:nz.co.fortytwo.signalk.client."+serviceName+"?level=ERROR&showException=true&showStackTrace=true")
				.end()
			.to("log:nz.co.fortytwo.signalk.client."+serviceName+"?level=DEBUG")
			.convertBodyTo(String.class)
			.setHeader(MSG_SRC_BUS, constant(serviceName+"."+client.toString().replace('.', '_')))
			.to(SEDA_INPUT);
	}

	private void startMdnsAutoconnect() {
		//now listen and report other services
		logger.info("Starting jmdns listener..");
		JmmDNS.Factory.getInstance().addServiceListener(_SIGNALK_WS_TCP_LOCAL, new ServiceListener() {
			
			@Override
			public void serviceResolved(ServiceEvent evt) {
				try {
						String name = evt.getName();
						String thisHost = evt.getDNS().getInetAddress().getHostAddress();
						logger.info("Resolved mDns service:"+name+" at "+thisHost);
						logger.debug(name+" Server:"+evt.getInfo().getServer());
						String[] remoteHost = evt.getInfo().getHostAddresses();
						logger.debug(name+" Remotehost:"+remoteHost[0]);
					
						logger.debug(name+" URLs:"+Arrays.toString(evt.getInfo().getURLs()));
						if(thisHost.startsWith(remoteHost[0]) 
								|| evt.getDNS().getInetAddress().isLinkLocalAddress()
								|| evt.getDNS().getInetAddress().isLoopbackAddress()){
							logger.info(name+" Found own host: "+remoteHost[0]+", ignoring..");
							return;
						}
						if(remoteHost[0].startsWith("[fe80") || evt.getDNS().getInetAddress().isLinkLocalAddress()){
							logger.info(name+" Found ipv6 host: "+remoteHost[0]+", ignoring..");
							return;
						}
						//we want to connect here
						String url =evt.getInfo().getURLs()[0];
						if(StringUtils.isNotBlank(url)){
							logger.info(name+" Connecting to: "+url);
							
							url=url.substring(url.indexOf("://")+3);
							url=url+"/v1/stream";
							logger.info("  Starting WS connection to url:ahc-ws://"+url);
			
							startWsClient(url);
							
						}
					
				} catch (Exception e) {
				
					logger.error(e);
				}
				
			}
			
			@Override
			public void serviceRemoved(ServiceEvent evt) {
				logger.info("Lost mDns service:"+evt.getName());
			}
			
			@Override
			public void serviceAdded(ServiceEvent evt) {
				logger.info("Found mDns service:"+evt.getName()+" at "+evt.getType());
				
			}
		});
		logger.info("Started jmdns listener");
		
	}

	private void startWsClient(String client) throws Exception {
		WsEndpoint wsEndpoint = (WsEndpoint)getContext().getEndpoint("ahc-ws://"+client);
		RouteDefinition route = from(wsEndpoint);
		
		route.onException(Exception.class).handled(true).maximumRedeliveries(0)
				.to("log:nz.co.fortytwo.signalk.client.ws?level=ERROR&showException=true&showStackTrace=true")
				.end()
			.to("log:nz.co.fortytwo.signalk.client.ws?level=DEBUG")
			.convertBodyTo(String.class)
			.setHeader(MSG_SRC_BUS, constant("ws."+client.toString().replace('.', '_')))
			.to(SEDA_INPUT);
		route.setId("Websocket Client:"+client);
		((DefaultCamelContext)CamelContextFactory.getInstance()).addRouteDefinition(route);
		((DefaultCamelContext)CamelContextFactory.getInstance()).startRoute(route.getId());
		wsEndpoint.connect();
		
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
		serialPortManager=null;
	}


	


}
