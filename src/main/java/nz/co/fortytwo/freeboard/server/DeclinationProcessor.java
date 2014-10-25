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

import mjson.Json;
import nz.co.fortytwo.freeboard.server.util.JsonConstants;
import nz.co.fortytwo.freeboard.server.util.Magfield;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

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
			double declination = -1
					* Magfield.SGMagVar(Magfield.deg_to_rad(lat.at("value").asDouble()), -1 * Magfield.deg_to_rad(lon.at("value").asDouble()), 0, Magfield.yymmdd_to_julian_days(13, 1, 1), 7, new double[6]);
			declination = Magfield.rad_to_deg(declination) * -1;// declination is positive when true N is west of MagN, eg subtract the declination
			declination = round(declination, 1);
			logger.debug("Declination = " + declination);
			signalkModel.putWith(signalkModel.self(), JsonConstants.nav_magneticVariation, declination, JsonConstants.SELF);	
		}
		
	}

}
