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
import org.joda.time.DateTime;

/**
 * Validate the signalkModel json.
 * Make sure it has timestamp and source
 * 
 * @author robert
 * 
 */
public class ValidationProcessor extends SignalkProcessor implements Processor{

	private static Logger logger = Logger.getLogger(ValidationProcessor.class);
	
	public void process(Exchange exchange) throws Exception {
		
		try {
			if(!(exchange.getIn().getBody() instanceof Json)){
				logger.debug("Invalid object found:" + exchange.getIn().getBody());
				exchange.getIn().setBody(null);
				return;
			}
			validate(exchange.getIn().getBody(Json.class));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	
	public void validate(Json node){
		//is this a leaf?
		if(logger.isDebugEnabled())	logger.debug(node.toString());
		if(node.isNull()||!node.isObject())return;
		if(node.has("value")){
			//it should have timestamp and source
			if(!node.has("timestamp"))node.set("timestamp",new DateTime().toString());
			if(!node.has("source"))node.set("source","unknown");
		}else{
			for(Json n : node.asJsonMap().values()){
				validate(n);
			}
		}
	}

}
