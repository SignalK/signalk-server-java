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

import static nz.co.fortytwo.signalk.server.util.JsonConstants.*;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.jayway.jsonpath.JsonPath;

/**
 * Updates the convert n2k json to signalk tree
 * 
 * @author robert
 * 
 */
public class N2KProcessor extends SignalkProcessor implements Processor{

	private static final String FILTER = "filter";
	private static final String NODE = "node";
	private static final String SOURCE = "source";
	private static final String self = VESSELS+"."+SELF+".";
	private static Logger logger = Logger.getLogger(N2KProcessor.class);
	private static DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	private NumberFormat numberFormat = NumberFormat.getInstance();
	private Json mappings=null;
	
	public N2KProcessor() throws IOException{
		File mappingFile = new File("./conf/n2kMappings.json");
		mappings = Json.read(FileUtils.readFileToString(mappingFile));
	}
	public void process(Exchange exchange) throws Exception {
		
		try {
			if(exchange.getIn().getHeader(N2K_MESSAGE)==null) return;
			if(exchange.getIn().getBody()==null ||!(exchange.getIn().getBody() instanceof Json)) return;
			
			Json json = handle(exchange.getIn().getHeader(N2K_MESSAGE, String.class));
			logger.debug("Converted to:"+json);
			exchange.getIn().setBody(json);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	/*
	 *  {
    "timestamp": "2013-10-08-15:47:28.264",
    "prio": "2",
    "src": "2",
    "dst": "255",
    "pgn": "129025",
    "description": "Position, Rapid Update",
    "fields": {
        "Latitude": "60.1445540",
        "Longitude": "24.7921348"
    }
}
*/
	 
	//@Override
	public Json  handle(String n2kmsg) {
		//avoid full signalk syntax
		String pgn = JsonPath.read(n2kmsg,"$.pgn");
		logger.debug("processing n2k pgn "+pgn );
		if(mappings.has(PGN)){
			//process it
			
			//go to context
			Json mapping = mappings.at(pgn);
			if( mapping==null)return null;			
			SignalKModel temp =  SignalKModelFactory.getCleanInstance();
			for(Json map : mapping.asJsonList()){
				String var = JsonPath.read(n2kmsg, map.at(FILTER).asString()+"."+map.at(SOURCE).asString());
				Object obj = resolve(var);
				String node = map.at(NODE).asString();
				if(node!=null && var!=null){
					//put in signalk tree
					temp.putWith(self+node, obj);
					
				}
			}
			logger.debug("N2KProcessor output  "+temp );
			return (Json) temp;
		}
		return null;
		
	}
	private Object resolve(String var) {
		if(var==null) return null;
		try {
			return numberFormat.parse(var);
		} catch (ParseException e) {
			return var;
		}
		
	}

	
}
