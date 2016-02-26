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

import static nz.co.fortytwo.signalk.util.SignalKConstants.UNKNOWN;
import static nz.co.fortytwo.signalk.util.SignalKConstants.dot;
import static nz.co.fortytwo.signalk.util.SignalKConstants.sourceRef;
import static nz.co.fortytwo.signalk.util.SignalKConstants.timestamp;
import static nz.co.fortytwo.signalk.util.SignalKConstants.value;
import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Validate the signalkModel .
 * Make sure it has timestamp and source
 * 
 * @author robert
 * 
 */
public class ValidationProcessor extends SignalkProcessor implements Processor{

	private static Logger logger = Logger.getLogger(ValidationProcessor.class);
	
	public void process(Exchange exchange) throws Exception {
		
		try {
			//if we have a REST_REPLY send it now
			
			if(exchange.getIn().getBody() instanceof SignalKModel){
				validate(exchange.getIn().getBody(SignalKModel.class));
			}else{
				if(logger.isDebugEnabled())logger.debug("Ignored, not update:"+exchange.getIn().getBody(Json.class));
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	
	/**
	 * @param model
	 */
	public void validate(SignalKModel model){
		//is this a leaf?
		if(logger.isDebugEnabled())	logger.debug(model.toString());
		if(model==null||model.getKeys().size()==0)return;
		//check the values
		for(String key: model.getKeys()){
			if(logger.isDebugEnabled())	logger.debug("Checking key="+key);
			if(key.endsWith(dot+value)){
				if(logger.isDebugEnabled())	logger.debug("Processing key="+key);
				String tmpKey = key.substring(0,key.length()-value.length());
				//it should have timestamp and source
				if(model.getSubMap(tmpKey+timestamp).size()==0){
					model.getFullData().put(tmpKey+timestamp,Util.getIsoTimeString());
				}
				if(model.getSubMap(tmpKey+sourceRef).size()==0){
					model.getFullData().put(tmpKey+sourceRef,UNKNOWN);
				}
			}
		
		}
		
	}

}
