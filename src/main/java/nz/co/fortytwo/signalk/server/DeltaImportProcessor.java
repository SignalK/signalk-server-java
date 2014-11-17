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
import static nz.co.fortytwo.signalk.server.util.JsonConstants.*;
import nz.co.fortytwo.signalk.model.event.JsonEvent;
import nz.co.fortytwo.signalk.server.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;

/**
 * Updates the signalkModel with the current json
 * 
 * @author robert
 * 
 */
public class DeltaImportProcessor extends FreeboardProcessor implements Processor{

	private static Logger logger = Logger.getLogger(DeltaImportProcessor.class);
	private static DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	
	public void process(Exchange exchange) throws Exception {
		
		try {
			if(exchange.getIn().getBody()==null ||!(exchange.getIn().getBody() instanceof Json)) return;
			
			Json json = handle(exchange.getIn().getBody(Json.class));
			logger.debug("Converted to:"+json);
			exchange.getIn().setBody(json);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	/*
	 *  {
    "context": "vessels.motu.navigation",
    "updates":[
	    	{
		    "source": {
		        "device" : "/dev/actisense",
		        "timestamp":"2014-08-15-16:00:00.081",
		        "src":"115",
		         "pgn":"128267"
		    },
		    "values": [
		         { "path": "courseOverGroundTrue","value": 172.9 },
		         { "path": "speedOverGround","value": 3.85 }
		      ]
		    },
		     {
		      "source": {
		        "device" : "/dev/actisense",
		        "timestamp":"2014-08-15-16:00:00.081",
		        "src":"115",
		         "pgn":"128267"
		    },
		    "values": [
		         { "path": "courseOverGroundTrue","value": 172.9 },
		         { "path": "speedOverGround","value": 3.85 }
		      ]
		    }
	    ]
	      
	}
*/
	 
	//@Override
	public Json  handle(Json node) {
		//avoid full signalk syntax
		if(node.has(VESSELS))return node;
		//deal with diff format
		if(node.has(CONTEXT)){
			logger.debug("processing delta  "+node );
			//process it
			Json temp = Util.getEmptyRootNode();
			
			//go to context
			String path = node.at(CONTEXT).asString();
			Json pathNode = signalkModel.addNode(temp, path);
			Json updates = node.at(UPDATES);
			if(updates.isArray()){
				for(Json update: updates.asJsonList()){
					parseUpdate(temp, update, pathNode);
				}
			}else{
				parseUpdate(temp, updates.at(UPDATES), pathNode);
			}
			
			logger.debug("SignalkModelProcessor processed diff  "+temp );
			return temp;
		}
		return node;
		
	}

	private void parseUpdate(Json temp, Json update, Json pathNode) {
		String device = update.at(SOURCE).at(DEVICE).asString();
		String ts = update.at(SOURCE).at(TIMESTAMP).asString();
		
		DateTime timestamp = DateTime.parse(ts,fmt);
		
		device = device + "-N2K-"+ update.at(SOURCE).at(SRC).asString();
		device = device + "-"+update.at(SOURCE).at(PGN).asString();
	//grab values and add
		Json array = update.at(VALUES);
		for(Json e : array.asJsonList()){
			String key = e.at(PATH).asString();
			Json n = signalkModel.addNode(pathNode, key);
			int pos = key.lastIndexOf(".");
			if(pos>0)
			key = key.substring(pos+1);
			signalkModel.putWith(n.up(),key, e.at(VALUE).getValue(),device,timestamp);
		}
		
	}

	
}
