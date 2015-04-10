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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Properties;

import nz.co.fortytwo.signalk.util.Constants;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.activemq.broker.BrokerService;
import org.apache.camel.main.Main;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

public class SignalKServer {

	private static Server server;

	private static Logger logger = Logger.getLogger(SignalKServer.class);

	private Properties config = null;

	protected SignalKServer(String configDir) throws Exception {

		config = Util.getConfig(configDir);
		// make sure we have all the correct dirs and files now
		ensureInstall();

		logger.info("Freeboard starting....");

		// do we have a USB drive connected?
		logger.info("USB drive " + Util.getUSBFile());

		// create a new Camel Main so we can easily start Camel
		Main main = new Main();

		// enable hangup support which mean we detect when the JVM terminates, and stop Camel graceful
		main.enableHangupSupport();

		if(Boolean.valueOf(config.getProperty(Constants.HAWTIO_START))){
			server=startHawtio();
		}
		
		/*
		 * Connector connector = new SelectChannelConnector();
		 * logger.info("  Webserver http port:"+config.getProperty(Constants.REST_PORT));
		 * connector.setPort(Integer.valueOf(config.getProperty(Constants.REST_PORT)));
		 * 
		 * server = new Server();
		 * server.addConnector(connector);
		 * //serve tracks
		 * ServletContextHandler trackContext = new ServletContextHandler();
		 * logger.info("  Tracks url:"+config.getProperty(Constants.STATIC_DIR));
		 * trackContext.setContextPath(config.getProperty(Constants.STATIC_DIR));
		 * logger.info("  Tracks resource:"+config.getProperty(Constants.STATIC_DIR));
		 * trackContext.setResourceBase(config.getProperty(Constants.STATIC_DIR));
		 * trackContext.addServlet(DefaultServlet.class, "/dist/*");
		 * 
		 * HandlerList handlers = new HandlerList();
		 * 
		 * handlers.addHandler(trackContext);
		 * server.setHandler(handlers);
		 * server.start();
		 */

		// Start activemq broker
		BrokerService broker = ActiveMqBrokerFactory.newInstance();
		broker.start();

		RouteManager routeManager = RouteManagerFactory.getInstance(config);

		// add our routes to Camel
		main.addRouteBuilder(routeManager);

		// and run, which keeps blocking until we terminate the JVM (or stop CamelContext)
		main.run();

		// so now shutdown serial reader and server

		routeManager.stopSerial();
		server.stop();
		broker.stop();
		System.exit(0);
	}

	private Server startHawtio() throws Exception {
		//hawtio, auth disabled
		System.setProperty(Constants.HAWTIO_AUTHENTICATE, config.getProperty(Constants.HAWTIO_AUTHENTICATE));
		int hawtPort = Integer.valueOf(config.getProperty(Constants.HAWTIO_PORT));
		Server server = new Server(hawtPort);
		HandlerCollection handlers = new HandlerCollection();
		handlers.setServer(server);
		server.setHandler(handlers);
		WebAppContext webapp = new WebAppContext();
		webapp.setServer(server);
		webapp.setContextPath(config.getProperty(Constants.HAWTIO_CONTEXT));
		
		webapp.setWar(config.getProperty(Constants.HAWTIO_WAR));
		webapp.setParentLoaderPriority(true);
		webapp.setLogUrlOnStart(true);
		// lets set a temporary directory so jetty doesn't bork if some process zaps /tmp/*
		String homeDir = System.getProperty("user.home", ".") + System.getProperty("hawtio.dirname", "/.hawtio");
		String tempDirPath = homeDir + "/tmp";
		File tempDir = new File(tempDirPath);
		tempDir.mkdirs();
		logger.info("using temp directory for hawtio jetty: " + tempDir.getPath());
		webapp.setTempDirectory(tempDir);
		// add hawtio
		handlers.addHandler(webapp);

		server.start();
		return server;
	}

	private void ensureInstall() {

		File rootDir = new File(".");
		if (Util.cfg != null) {
			rootDir = Util.cfg.getParentFile();
		}
		// do we have a log dir?
		File logDir = new File(rootDir, "logs");
		if (!logDir.exists()) {
			logDir.mkdirs();
		}

	}

	public static void main(String[] args) throws Exception {
		// we look for and use a freeboard.cfg in the launch/cfg dir and use that to override defaults
		// the only arg is conf dir
		String conf = null;
		if (args != null && args.length > 0 && StringUtils.isNotBlank(args[0])) {
			conf = args[0];
			if (!conf.endsWith("/")) {
				conf = conf + "/";
			}
		}
		SignalKServerFactory.getInstance(conf);

	}

}
