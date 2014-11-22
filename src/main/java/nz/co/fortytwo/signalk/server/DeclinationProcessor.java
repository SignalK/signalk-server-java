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
package nz.co.fortytwo.signalk.server;

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
public class DeclinationProcessor extends FreeboardProcessor implements Processor {

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
			logger.debug("Declination  for "+lat.at("value")+", "+lon.at("value") );
			
			double declination = geoMag.getDeclination(lat.at("value").asDouble(), lon.at("value").asDouble(), DateTime.now().getYear(), 0.0d);
			
			declination = round(declination, 1);
			logger.debug("Declination = " + declination);
			signalkModel.putWith(signalkModel.self(), JsonConstants.nav_magneticVariation, declination, JsonConstants.SELF);	
		}
		
	}

}
