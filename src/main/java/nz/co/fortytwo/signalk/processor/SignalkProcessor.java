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

import java.util.regex.Pattern;

import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.CamelContextFactory;
import nz.co.fortytwo.signalk.server.RouteManager;
import nz.co.fortytwo.signalk.server.SubscriptionManager;
import nz.co.fortytwo.signalk.server.SubscriptionManagerFactory;
import nz.co.fortytwo.signalk.util.SignalKConstants;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
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
	protected static final SignalKModel signalkModel = SignalKModelFactory.getInstance();
	protected  SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
	//@Produce(uri = RouteManager.SEDA_NMEA )
    ProducerTemplate nmeaProducer;
    ProducerTemplate outProducer;
    ProducerTemplate inProducer;
	//public static final String DOT = ".";
	public static final String VESSELS_DOT_SELF = SignalKConstants.vessels + ".self";
    
	
	
	public SignalkProcessor(){
		nmeaProducer= new DefaultProducerTemplate(CamelContextFactory.getInstance());
		nmeaProducer.setDefaultEndpointUri(RouteManager.SEDA_NMEA );
		outProducer= new DefaultProducerTemplate(CamelContextFactory.getInstance());
		outProducer.setDefaultEndpointUri(RouteManager.SEDA_COMMON_OUT );
		inProducer= new DefaultProducerTemplate(CamelContextFactory.getInstance());
		inProducer.setDefaultEndpointUri(RouteManager.SEDA_INPUT );
		try {
			nmeaProducer.start();
			outProducer.start();
			inProducer.start();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}
	
	
	/**
	 * If a processor generates an NMEA string, then this method is a convenient way to send it to the NMEA stream
	 * 
	 * @param ex
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
		if (newPath.startsWith(SignalKConstants.dot))
			newPath = newPath.substring(1);
		if (SignalkProcessor.VESSELS_DOT_SELF.equals(newPath)){
			newPath = SignalKConstants.vessels_dot_self;
		}
		newPath = newPath.replace(SignalkProcessor.VESSELS_DOT_SELF + SignalKConstants.dot, SignalKConstants.vessels_dot_self_dot);
		return newPath;
	}
}
