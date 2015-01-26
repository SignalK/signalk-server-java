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

import mjson.Json;
import nz.co.fortytwo.signalk.server.util.JsonConstants;
import nz.co.fortytwo.signalk.server.util.TSAGeoMag;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

/**
 * Processes messages, if it finds a Magnetic bearing, and has seen a LAT and LON, it calculates declination,
 * and appends declination to the mag heading message. Since the calculation of declination is expensive we only do it once,
 * and only redo it if the integer LAT or LON changes
 * 
 * @author robert
 * 
 */
public class DeclinationProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = Logger.getLogger(DeclinationProcessor.class);
	
	private TSAGeoMag geoMag = new TSAGeoMag();
	
	public void process(Exchange exchange) throws Exception {
		
		try {
		
			handle();
			
		} catch (Exception e) {
			logger.error(e);
		}
	}

	//@Override
	public void handle() {
		logger.debug("Declination  calculation fired " );
		Json lat = signalkModel.findNode(signalkModel.self(), JsonConstants.nav_position_latitude);
		Json lon = signalkModel.findNode(signalkModel.self(), JsonConstants.nav_position_longitude);
		
		if (lat!=null && lon!=null) {
			if(logger.isDebugEnabled())logger.debug("Declination  for "+lat.at("value")+", "+lon.at("value") );
			
			double declination = geoMag.getDeclination(lat.at("value").asDouble(), lon.at("value").asDouble(), DateTime.now().getYear(), 0.0d);
			
			declination = round(declination, 1);
			if(logger.isDebugEnabled())logger.debug("Declination = " + declination);
			signalkModel.putWith(signalkModel.self(), JsonConstants.nav_magneticVariation, declination, JsonConstants.SELF);	
		}
		
	}

}
