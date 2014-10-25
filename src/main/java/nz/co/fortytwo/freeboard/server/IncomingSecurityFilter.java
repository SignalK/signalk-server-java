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

import java.util.ArrayList;
import java.util.List;

import mjson.Json;
import nz.co.fortytwo.freeboard.server.util.JsonConstants;

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
public class IncomingSecurityFilter extends FreeboardProcessor implements Processor{

	private static Logger logger = Logger.getLogger(IncomingSecurityFilter.class);
	private List<String> acceptList = new ArrayList<String>();
	private List<String> denyList = new ArrayList<String>();
	
	public void process(Exchange exchange) throws Exception {
		
		try {
			//we trust serial
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
