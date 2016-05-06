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

import nz.co.fortytwo.signalk.handler.NMEA0183Producer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

/**
 * Processes the signalk model for alarm condidtions and sets *.alarm.* keys
 * 
 * @author robert
 * 
 */
public class NMEA0183ExportProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = LogManager.getLogger(NMEA0183ExportProcessor.class);
	
	private NMEA0183Producer handler = new NMEA0183Producer();
	
	public void process(Exchange exchange) throws Exception {
		
		try {
			sendNmea(handler.createRMC(signalkModel));
			sendNmea(handler.createDBT(signalkModel));	
			sendNmea(handler.createGLL(signalkModel));		
			sendNmea(handler.createVTG(signalkModel));	
			sendNmea(handler.createHDG(signalkModel));	
			sendNmea(handler.createHDT(signalkModel));
			sendNmea(handler.createHDM(signalkModel));
			sendNmea(handler.createMWVApparent(signalkModel));	
			sendNmea(handler.createMWVTrue(signalkModel));	
			sendNmea(handler.createVHW(signalkModel));
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.debug(e.getMessage(),e);
			logger.debug("SignalkModel:"+signalkModel);
		}
	}

	private void sendNmea(String nmea) {
		if(logger.isDebugEnabled())logger.debug("Sending: "+nmea);
		if(nmea!=null){
			nmeaProducer.sendBodyAndHeader(nmea, WebsocketConstants.CONNECTION_KEY, WebsocketConstants.SEND_TO_ALL);
		}
		
	}

}
