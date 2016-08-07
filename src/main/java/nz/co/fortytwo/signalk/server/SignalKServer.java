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
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.co.fortytwo.signalk.server;

import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_DISCOVERY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.jmdns.JmmDNS;
import javax.jmdns.ServiceInfo;

import org.apache.activemq.broker.BrokerService;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainSupport;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.SignalKConstants;
import nz.co.fortytwo.signalk.util.Util;

public class SignalKServer {

	private static Server server;

	private static Logger logger = LogManager.getLogger(SignalKServer.class);

	private JmmDNS jmdns = null;
	
	protected SignalKServer(String configDir) throws Exception {
		// init config
		Properties props = System.getProperties();
		props.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperties(props);

		
		Util.getConfig();
		// make sure we have all the correct dirs and files now
		ensureInstall();
		
		logger.info("SignalKServer starting....");

		// do we have a USB drive connected?
		//logger.info("USB drive " + Util.getUSBFile());

		// create a new Camel Main so we can easily start Camel
		Main main = new Main();
		//main.setApplicationContextUri("classpath:META-INF/spring/camel-context.xml");
		// enable hangup support which mean we detect when the JVM terminates,
		// and stop Camel graceful
		main.enableHangupSupport();

		// Start activemq broker
		BrokerService broker = ActiveMqBrokerFactory.newInstance();

		broker.start();
		//DNS-SD, zeroconf mDNS
		startMdns();
		configureRouteManager(main);
		// and run, which keeps blocking until we terminate the JVM (or stop
		// CamelContext)
		main.start();
		
		WatchService service = FileSystems.getDefault().newWatchService();
		Path dir = Paths.get("./conf");
		dir.register(service, StandardWatchEventKinds.ENTRY_MODIFY);
		WatchKey key = null;
		while(true) {
			key = service.take();
			// Dequeueing events
			Kind<?> kind = null;
			for(WatchEvent<?> watchEvent : key.pollEvents()) {
				// Get the type of the event
				kind = watchEvent.kind();
				logger.debug("SignalKServer conf/ event:"+watchEvent.kind() +" : "+watchEvent.context().toString());
				if (StandardWatchEventKinds.OVERFLOW == kind) {
					continue; //loop
				} else if (StandardWatchEventKinds.ENTRY_MODIFY == kind) {
					// A new Path was created 
					@SuppressWarnings("unchecked")
					Path newPath = ((WatchEvent<Path>) watchEvent).context();
					// Output
					if(newPath.endsWith("signalk-restart")){
						logger.info("SignalKServer conf/signalk-restart changed, stopping..");
						main.stop();
						main.getCamelContexts().clear();
						main.getRouteBuilders().clear();
						main.getRouteDefinitions().clear();

						// so now shutdown serial reader and server
						RouteManager routeManager = RouteManagerFactory.getInstance();
						routeManager.stopNettyServers();
						routeManager.stopSerial();
						if(server!=null){
							server.stop();
							server=null;
						}
						RouteManagerFactory.clear();
						configureRouteManager(main);
						main.start();
					}
			
				}
			}
			
			if(!key.reset()) {
				break; //loop
			}
		}
		
		stopMdns();
		broker.stop();
		// write out the signalk model
		SignalKModelFactory.save(SignalKModelFactory.getInstance());
		System.exit(0);
	}

	private void configureRouteManager(MainSupport main) throws Exception {
		logger.info("SignalKServer conf/signalk-config.json changed, restarting");
		
		if (Util.getConfigPropertyBoolean(ConfigConstants.HAWTIO_START)) {
			logger.info("SignalKServer starting hawtio manager....");
			server = startHawtio();
		}else{
			//start jolokia for remote management
			logger.info("SignalKServer starting jolokia remote management agent....");
			server = startJolokia();
		}
		
		RouteManager routeManager = RouteManagerFactory.getInstance();

		// add our routes to Camel
		main.addRouteBuilder(routeManager);
	
		logger.info("SignalKServer configured");
		
	}

	private Server startJolokia() throws Exception {
		Properties props = System.getProperties();
		props.setProperty("jolokia.authenticationEnabled", "false");
		System.setProperties(props);
		//System.setProperty("hawtio.authenticationEnabled",Util.getConfigPropertyBoolean(ConfigConstants.HAWTIO_AUTHENTICATE).toString());
		int hawtPort = Util.getConfigPropertyInt(ConfigConstants.JOLOKIA_PORT);
		return startServer(hawtPort, Util.getConfigProperty(ConfigConstants.JOLOKIA_CONTEXT),Util.getConfigProperty(ConfigConstants.JOLOKIA_WAR),"/.jolokia");
	}

	private Server startHawtio() throws Exception {
		// hawtio, auth disabled
		Properties props = System.getProperties();
		props.setProperty("hawtio.authenticationEnabled", "false");
		System.setProperties(props);
		//System.setProperty("hawtio.authenticationEnabled",Util.getConfigPropertyBoolean(ConfigConstants.HAWTIO_AUTHENTICATE).toString());
		int hawtPort = Util.getConfigPropertyInt(ConfigConstants.HAWTIO_PORT);
		return startServer(hawtPort, Util.getConfigProperty(ConfigConstants.HAWTIO_CONTEXT),Util.getConfigProperty(ConfigConstants.HAWTIO_WAR), "/.hawtio");
	}
	private Server startServer(int hawtPort, String contextPath, String war, String dirName) throws Exception {
		
		Server server = new Server(hawtPort);
		HandlerCollection handlers = new HandlerCollection();
		handlers.setServer(server);
		server.setHandler(handlers);
		WebAppContext webapp = new WebAppContext();
		webapp.setServer(server);
		webapp.setContextPath(contextPath);

		webapp.setWar(war);
		webapp.setParentLoaderPriority(true);
		webapp.setLogUrlOnStart(true);
		// lets set a temporary directory so jetty doesn't bork if some process
		// zaps /tmp/*
		String homeDir = System.getProperty("user.home", ".")+dirName;
		String tempDirPath = homeDir + "/tmp";
		File tempDir = new File(tempDirPath);
		tempDir.mkdirs();
		logger.info("using temp directory for hawtio/jolokia jetty: "
				+ tempDir.getPath());
		webapp.setTempDirectory(tempDir);
		// add hawtio
		handlers.addHandler(webapp);

		server.start();
		return server;
	}

	private void ensureInstall() throws IOException {

		File rootDir = new File(".");
		//if (Util.cfg != null) {
		//	rootDir = Util.cfg.getParentFile().getParentFile();
		//}
		// do we have a log dir?
		File logDir = new File(rootDir, "logs");
		if (!logDir.exists()) {
			logDir.mkdirs();
		}
		// do we have a log4j.properties?
		File log4j = new File(rootDir, "conf/log4j2.json");
		if (!log4j.exists()) {
			File log4jSample = new File(rootDir, "conf/log4j2.json.sample");
			FileUtils.copyFile(log4jSample, log4j);
		}
		Configurator.initialize("signalk",log4j.toString());
		// do we have a storage dir?
		File storageDir = new File(
				Util.getConfigProperty(ConfigConstants.STORAGE_ROOT));
		if (!storageDir.exists()) {
			storageDir.mkdirs();
		}
		
		

	}
	
	/**
	 * Stop the DNS-SD server.
	 * @throws IOException 
	 */
	public void stopMdns() throws IOException {
		if(jmdns!=null){
			jmdns.unregisterAllServices();
			jmdns.close();
			jmdns=null;
		}
	}
	
	private void startMdns() {
		//DNS-SD
		//NetworkTopologyDiscovery netTop = NetworkTopologyDiscovery.Factory.getInstance();
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				jmdns = JmmDNS.Factory.getInstance();
				
				jmdns.registerServiceType(SignalKConstants._SIGNALK_WS_TCP_LOCAL);
				jmdns.registerServiceType(SignalKConstants._SIGNALK_HTTP_TCP_LOCAL);
				ServiceInfo wsInfo = ServiceInfo.create(SignalKConstants._SIGNALK_WS_TCP_LOCAL,"signalk-ws",Util.getConfigPropertyInt(ConfigConstants.WEBSOCKET_PORT), 0,0, getMdnsTxt());
				try {
					jmdns.registerService(wsInfo);
					ServiceInfo httpInfo = ServiceInfo
						.create(SignalKConstants._SIGNALK_HTTP_TCP_LOCAL, "signalk-http",Util.getConfigPropertyInt(ConfigConstants.REST_PORT),0,0, getMdnsTxt());
					jmdns.registerService(httpInfo);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		Thread t = new Thread(r);
		t.setDaemon(true);
		t.start();
		
	}

	private Map<String,String> getMdnsTxt() {
		Map<String,String> txtSet = new HashMap<String, String>();
		txtSet.put("path", SIGNALK_DISCOVERY);
		txtSet.put("server","signalk-server");
		txtSet.put("version",Util.getConfigProperty(ConfigConstants.VERSION));
		txtSet.put("vessel_name",Util.getConfigProperty(ConfigConstants.UUID));
		txtSet.put("vessel_mmsi",Util.getConfigProperty(ConfigConstants.UUID));
		txtSet.put("vessel_uuid",Util.getConfigProperty(ConfigConstants.UUID));
		return txtSet;
	}
	
	public static void main(String[] args) throws Exception {
		// we look for and use a freeboard.cfg in the launch/cfg dir and use
		// that to override defaults
		// the only arg is conf dir
		String conf = null;
		if (args != null && args.length > 0 && StringUtils.isNotBlank(args[0])) {
			conf = args[0];
			if (!conf.endsWith("/")) {
				conf = conf + "/";
			}
		}
		//Configurator.initialize("test","./conf/log4j.json");
		SignalKServerFactory.getInstance(conf);

	}

}
