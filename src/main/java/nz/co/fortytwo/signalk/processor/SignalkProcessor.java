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

import java.util.Set;

import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.CamelContextFactory;
import nz.co.fortytwo.signalk.server.SessionManager;
import nz.co.fortytwo.signalk.server.SessionManagerFactory;
import nz.co.fortytwo.signalk.server.util.Util;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.session.HashSessionManager;


/**
 * Holder for some useful methods for processors
 * @author robert
 *
 */
public class SignalkProcessor {
	
	private static Logger logger = Logger.getLogger(SignalkProcessor.class);
	static protected SignalKModel signalkModel = SignalKModelFactory.getInstance();
	static protected  SessionManager manager = SessionManagerFactory.getInstance();
	@Produce(uri = "seda:nmeaOutput")
    ProducerTemplate producer;
	
	
	public SignalkProcessor() {
		
	}
	
	
	/**
	 * If a processor generates an NMEA string, then this method is a convenient way to send it to the NMEA stream
	 * 
	 * @param nmea
	 */
	public void sendNmea(String nmea){
		producer.sendBody(nmea);
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
	
	
}
