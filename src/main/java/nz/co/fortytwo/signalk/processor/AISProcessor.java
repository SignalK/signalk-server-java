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

import nz.co.fortytwo.signalk.handler.AISHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

/**
 * Churns through incoming nav data and looking for AIVDM messages
 * Translates the VDMs into AisMessages and sends the AisPositionMessages on to the browser.
 * Mostly we need 1,2,3,5, 18,19
 * 
 * @author robert
 * 
 */
public class AISProcessor extends SignalkProcessor implements Processor {


	private static Logger logger = Logger.getLogger(AISProcessor.class);

    /** Reader to parse lines and deliver complete AIS packets.
     * Updates them into model, and removes the key from the map. */
   
	
    private AISHandler aisHandler = new AISHandler(); 
	

	public void process(Exchange exchange) {
		if (exchange.getIn().getBody() == null)
			return;
		if (exchange.getIn().getBody() instanceof String){
				
			String bodyStr = exchange.getIn().getBody(String.class);
			try{
				Object json = aisHandler.handle(bodyStr);
				exchange.getIn().setBody(json);
			} catch (Exception e) {
				logger.debug(e.getMessage(),e);
				logger.error(e.getMessage()+" : "+bodyStr);
				exchange.getIn().setBody(bodyStr);
			}
			
			
		}

	}

   
}
