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

import java.util.ArrayList;
import java.util.List;

import mjson.Json;
import nz.co.fortytwo.signalk.util.JsonConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

/**
 * Parse the signalkModel json and remove anything that violates security
 * 
 * @author robert
 * 
 */
public class IncomingSecurityFilter extends SignalkProcessor implements Processor{

	private static Logger logger = Logger.getLogger(IncomingSecurityFilter.class);
	private List<String> acceptList = new ArrayList<String>();
	private List<String> denyList = new ArrayList<String>();
	
	public void process(Exchange exchange) throws Exception {
		
		try {
			//we trust local serial
			String type = exchange.getIn().getHeader(JsonConstants.MSG_TYPE, String.class);
			if(JsonConstants.SERIAL.equals(type)) return;
			//we trust INTERNAL_IP
			if(JsonConstants.INTERNAL_IP.equals(type)) return;
			//we filter EXTERNAL_IP
			String srcIp = exchange.getIn().getHeader(JsonConstants.MSG_PORT, String.class);
			if(denyList.contains(srcIp)){
				exchange.getIn().setBody(null);
				return;
			}
			if(acceptList.contains(srcIp))return;
			
			//new incoming, so flag for acceptance
			exchange.getIn().setHeader(JsonConstants.MSG_APPROVAL, JsonConstants.REQUIRED);
			//filter for evil
			Json node = exchange.getIn().getBody(Json.class);
			//cant be for this vessel since its external
			if(node.at(JsonConstants.VESSELS).at(JsonConstants.SELF)!=null){
				exchange.getIn().setBody(null);
				return;
			}
			//filter(node);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	
	public void filter(Json node){
		//apply rules to this object
		
		//recurse into object
		for(Json n : node.asJsonMap().values()){
			filter(n);
		}
		
	}

}
