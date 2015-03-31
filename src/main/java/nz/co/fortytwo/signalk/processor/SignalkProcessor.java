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

package nz.co.fortytwo.signalk.processor;

import java.util.NavigableMap;
import java.util.regex.Pattern;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.CamelContextFactory;
import nz.co.fortytwo.signalk.server.RouteManager;
import nz.co.fortytwo.signalk.server.SubscriptionManager;
import nz.co.fortytwo.signalk.server.SubscriptionManagerFactory;
import nz.co.fortytwo.signalk.util.JsonConstants;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.log4j.Logger;


/**
 * Holder for some useful methods for processors
 * @author robert
 *
 */
public class SignalkProcessor {
	
	private static Logger logger = Logger.getLogger(SignalkProcessor.class);
	static protected SignalKModel signalkModel = SignalKModelFactory.getInstance();
	static protected  SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
	//@Produce(uri = RouteManager.SEDA_NMEA )
    ProducerTemplate nmeaProducer;
    ProducerTemplate outProducer;
	public static final String DOT = ".";
	public static final String VESSELS_DOT_SELF = JsonConstants.VESSELS + ".self";
    
	
	
	public SignalkProcessor(){
		nmeaProducer= new DefaultProducerTemplate(CamelContextFactory.getInstance());
		nmeaProducer.setDefaultEndpointUri(RouteManager.SEDA_NMEA );
		outProducer= new DefaultProducerTemplate(CamelContextFactory.getInstance());
		outProducer.setDefaultEndpointUri(RouteManager.SEDA_COMMON_OUT );
		try {
			nmeaProducer.start();
			outProducer.start();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}
	
	
	/**
	 * If a processor generates an NMEA string, then this method is a convenient way to send it to the NMEA stream
	 * 
	 * @param output
	 */
	public void sendNmea(Exchange ex){
		Exchange exchange = ex.copy();
		exchange.getIn().setHeader(WebsocketConstants.CONNECTION_KEY, WebsocketConstants.SEND_TO_ALL);
		nmeaProducer.send(exchange);
	}


	/**
	 * Round to specified decimals
	 * @param val
	 * @param places
	 * @return
	 */
	public double round(double val, int places){
		return Util.round(val, places);
	}


	public static Pattern regexPath(String newPath) {
		// regex it
		String regex = newPath.replaceAll(".", "[$0]").replace("[*]", ".*").replace("[?]", ".");
		return Pattern.compile(regex);
	}


	public static String sanitizePath(String newPath) {
		newPath = newPath.replace('/', '.');
		if (newPath.startsWith(SignalkProcessor.DOT))
			newPath = newPath.substring(1);
		if (SignalkProcessor.VESSELS_DOT_SELF.equals(newPath)){
			newPath = JsonConstants.VESSELS + SignalkProcessor.DOT + JsonConstants.SELF;
		}
		newPath = newPath.replace(SignalkProcessor.VESSELS_DOT_SELF + SignalkProcessor.DOT, JsonConstants.VESSELS + SignalkProcessor.DOT + JsonConstants.SELF + SignalkProcessor.DOT);
		return newPath;
	}
	
	public ProducerTemplate getExportProducer() {
		return outProducer;
	}

	public void setExportProducer(ProducerTemplate outProducer) {
		this.outProducer = outProducer;
	}


	public void populateTree(SignalKModel temp, String p) {
		NavigableMap<String, Object> node = signalkModel.getSubMap(p);
		if(logger.isDebugEnabled())logger.debug("Found node:" + p + " = " + node);
		temp.putAll(node);
		
	}

}
