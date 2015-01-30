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

import static nz.co.fortytwo.signalk.server.util.JsonConstants.CONTEXT;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.DEVICE;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.PATH;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.PGN;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.SOURCE;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.SRC;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.TIMESTAMP;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.UPDATES;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.VALUE;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.VALUES;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.VESSELS;
import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Updates the signalkModel with the current json
 * 
 * @author robert
 * 
 */
public class DeltaImportProcessor extends SignalkProcessor implements Processor{

	private static Logger logger = Logger.getLogger(DeltaImportProcessor.class);
	private static DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	
	public void process(Exchange exchange) throws Exception {
		
		try {
			if(exchange.getIn().getBody()==null ||!(exchange.getIn().getBody() instanceof Json)) return;
			
			Json json = handle(exchange.getIn().getBody(Json.class));
			if(logger.isDebugEnabled())logger.debug("Converted to:"+json);
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
			if(logger.isDebugEnabled())logger.debug("processing delta  "+node );
			//process it
			SignalKModel temp =  SignalKModelFactory.getCleanInstance();
			
			//go to context
			String path = node.at(CONTEXT).asString();
			Json pathNode = temp.addNode(path);
			Json updates = node.at(UPDATES);
			if(updates==null)return (Json) temp;
			if(updates.isArray()){
				for(Json update: updates.asJsonList()){
					parseUpdate(temp, update, pathNode);
				}
			}else{
				parseUpdate(temp, updates.at(UPDATES), pathNode);
			}
			
			if(logger.isDebugEnabled())logger.debug("SignalkModelProcessor processed diff  "+temp );
			return (Json) temp;
		}
		return node;
		
	}

	private void parseUpdate(SignalKModel temp, Json update, Json pathNode) {
		String device = update.at(SOURCE).at(DEVICE).asString();
		String ts = update.at(SOURCE).at(TIMESTAMP).asString();
		
		DateTime timestamp = DateTime.parse(ts,fmt);
		
		device = device + "-N2K-"+ update.at(SOURCE).at(SRC).asString();
		device = device + "-"+update.at(SOURCE).at(PGN).asString();
	//grab values and add
		Json array = update.at(VALUES);
		for(Json e : array.asJsonList()){
			String key = e.at(PATH).asString();
			Json n = temp.addNode(pathNode, key);
			int pos = key.lastIndexOf(".");
			if(pos>0){
				key = key.substring(pos+1);
			}
			temp.putWith(n.up(),key, e.at(VALUE).getValue(),device,timestamp);
		}
		
	}

	
}
