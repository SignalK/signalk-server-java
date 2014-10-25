/*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
 * 
 * This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
 * 
 * FreeBoard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FreeBoard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FreeBoard. If not, see <http://www.gnu.org/licenses/>.
 */
package nz.co.fortytwo.freeboard.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import mjson.Json;
import nz.co.fortytwo.freeboard.server.util.Constants;
import nz.co.fortytwo.freeboard.signalk.SignalKModel;
import nz.co.fortytwo.freeboard.signalk.SignalkRouteFactory;
import nz.co.fortytwo.freeboard.signalk.impl.SignalKModelFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.restlet.RestletConstants;

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
public class SignalKReceiver extends RouteBuilder {
	public static final String SEDA_INPUT = "seda:input";
	public static final String DIRECT_WEBSOCKETS = "direct:websockets";
	private static final String DIRECT_TCP = "direct:tcp";
	private int ws_port = 9291;
	private int rest_port = 9290;
	private String serialUrl;
	
	private SerialPortManager serialPortManager;
    
	
	private Properties config;
	private SignalKModel signalkModel=SignalKModelFactory.getInstance();
	private TcpServer tcpServer;

	public SignalKReceiver(Properties config) {
		this.config = config;
	}

	public int getPort() {
		return ws_port;
	}

	public void setPort(int port) {
		this.ws_port = port;
	}

	@Override
	public void configure() throws Exception {
		
		File jsonFile = new File("./conf/self.json");
		System.out.println(jsonFile.getAbsolutePath());
		try{
			Json temp = Json.read(jsonFile.toURI().toURL());
			signalkModel.merge(temp);
		}catch(Exception ex){
			System.out.println(ex.getMessage());
		}
		
		// init processors who depend on this being started
		

		// dump nulls, but avoid quartz jobs
		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(header(Exchange.TIMER_FIRED_TIME).isNull());
		predicates.add(header(RestletConstants.RESTLET_REQUEST).isNull());
		predicates.add(body().isNull());
		Predicate stopNull = PredicateBuilder.and(predicates);
		intercept().when(stopNull).stop();

		if (Boolean.valueOf(config.getProperty(Constants.DEMO))) {
			from("stream:file?fileName=" + serialUrl).to(SEDA_INPUT);
		}
		tcpServer = new TcpServer();
		tcpServer.start();
		// start a serial port manager
		serialPortManager = new SerialPortManager();
		new Thread(serialPortManager).start();
		
		// main input to destination route
		// send input to listeners
		SignalkRouteFactory.configureInputRoute(this, SEDA_INPUT);
		SignalkRouteFactory.configureWebsocketRoute(this, DIRECT_WEBSOCKETS);
		SignalkRouteFactory.configureTcpServerRoute(this, DIRECT_TCP, tcpServer);
		//restlet
		SignalkRouteFactory.configureRestRoute(this, "restlet:http://0.0.0.0:" + rest_port + "/signalk/api/");
		SignalkRouteFactory.configureAuthRoute(this, "restlet:http://0.0.0.0:" + rest_port + "/signalk/auth/");
		
		// timed actions
		from("timer://declination?fixedRate=true&period=10000").process(new DeclinationProcessor()).to("log:nz.co.fortytwo.freeboard.signalk.update?level=INFO").end();
		from("timer://wind?fixedRate=true&period=1000").process(new WindProcessor()).to("log:nz.co.fortytwo.freeboard.signalk.update?level=INFO").end();
		from("timer://signalkAll?fixedRate=true&period=1000").process(new SignalkModelExportProcessor()).to("log:nz.co.fortytwo.freeboard.signalk.signalkAll?level=INFO")
			.to(DIRECT_WEBSOCKETS).to(DIRECT_TCP).end();
		
		//react to changes
		//from("seda.output").
		
	}

	public String getSerialUrl() {
		return serialUrl;
	}

	public void setSerialUrl(String serialUrl) {
		this.serialUrl = serialUrl;
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
