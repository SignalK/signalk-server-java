/*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
 * 
 * This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
 *
 *  FreeBoard is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  FreeBoard is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with FreeBoard.  If not, see <http://www.gnu.org/licenses/>.
 */

package nz.co.fortytwo.freeboard.server;

import nz.co.fortytwo.freeboard.server.util.Util;
import nz.co.fortytwo.freeboard.signalk.SignalKModel;
import nz.co.fortytwo.freeboard.signalk.impl.SignalKModelFactory;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;


/**
 * Holder for some useful methods for processors
 * @author robert
 *
 */
public class FreeboardProcessor {
	
	private static Logger logger = Logger.getLogger(FreeboardProcessor.class);
	static protected SignalKModel signalkModel = SignalKModelFactory.getInstance();
	
	@Produce(uri = "seda:nmeaOutput")
    ProducerTemplate producer;
	
	public FreeboardProcessor() {
	
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
