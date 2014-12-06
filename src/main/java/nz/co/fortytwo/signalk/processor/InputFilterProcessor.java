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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

/**
 * Churns through incoming nav data and filters out misc debug and unnecessary messages from the other devices
 * Maps misc content to tags (AIS,NMEA,JSON)
 * 
 * @author robert
 *
 */
public class InputFilterProcessor extends FreeboardProcessor implements Processor {
	private static Logger logger = Logger.getLogger(InputFilterProcessor.class);

	
	public InputFilterProcessor(){
		
	}
	public void process(Exchange exchange) throws Exception {
		String msg = (String) exchange.getIn().getBody(String.class);
		if(msg !=null){
			msg=msg.trim();
			boolean ok = false;
			if(msg.startsWith("!AIVDM")){
				//AIS
				//!AIVDM,1,1,,B,15MwkRUOidG?GElEa<iQk1JV06Jd,0*6D
				//exchange.getIn().setBody(stringToHashMap(msg));
				ok = true;
			}else if(msg.startsWith("$")){
				//NMEA - good
				//System.out.println(msg);
				//exchange.getIn().setBody(stringToHashMap(msg));
				ok = true;
			}else if(msg.startsWith("{")&& msg.endsWith("}")){
				//json
				exchange.getIn().setBody(Json.read(msg));
				ok = true;
			}
			if(ok){
				return;
			}
			//uh-oh log it, squash it
			exchange.getUnitOfWork().done(exchange);
			//System.out.println("Dropped invalid message:"+msg);
			logger.info("Dropped invalid message:"+msg);
			exchange.getIn().setBody(null);
			//throw new CamelExchangeException("Invalid msg", exchange);
		}
		
	}

	
}
