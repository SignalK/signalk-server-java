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

import static nz.co.fortytwo.signalk.server.util.JsonConstants.PATH;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.SOURCE;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.TIMESTAMP;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.UPDATES;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.VALUE;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.VALUES;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.VESSELS;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import mjson.Json;
import nz.co.fortytwo.signalk.model.event.JsonEvent;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;

/**
 * Exports the signalkModel as a json object
 * 
 * @author robert
 * 
 */
public class DeltaExportProcessor extends SignalkProcessor implements Processor{

	private static Logger logger = Logger.getLogger(DeltaExportProcessor.class);

	protected ConcurrentHashMap<String, Json> map = new ConcurrentHashMap<String, Json>();
	public DeltaExportProcessor(){
		super();
		signalkModel.getEventBus().register(this);
		
	}
	public void process(Exchange exchange) throws Exception {
		
		try {
			logger.debug("process delta queue ("+map.size()+") for "+this.hashCode());
			//get the accumulated delta nodes.
			List<Json> deltas = null;
			synchronized (map) {
				deltas = ImmutableList.copyOf(map.values());
				map.clear();
			}
			exchange.getIn().setBody(deltas);
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
	private void createDelta(Json j) {
		// create element
		String path = getPath(j);
		logger.debug("Delta for:"+path);
		String vessel = getVessel(path);
		if(vessel==null)return;
		
		//recurse the objects to the leaves
		Json updates = Json.array();
		getEntries(updates, j, vessel);
		
		synchronized (map) {
			Json delta = map.get(vessel);
			if(delta==null){
				delta=getEmptyDelta();
				delta.set("context",vessel);
				map.put(vessel, delta);
			}
			delta.set(UPDATES,updates);
		}
	}
	
	private void getEntries(Json updates, Json j, String vessel) {
		if(!j.isObject())return;
		//recurse objects
		if( j.has(VALUE)){
			String path = getPath(j);
			path=path.substring(vessel.length()+1);
			
			Json entry = Json.object();
			Json source = Json.object();
			source.set(SOURCE, j.at(SOURCE).getValue());
			source.set(TIMESTAMP, j.at(TIMESTAMP).getValue());
			entry.set(SOURCE, source);
			
			Json value = Json.object();
			value.set(PATH,path);
			value.set(VALUE, j.at(VALUE).getValue());
			
			Json values = Json.array();
			values.add(value);
			entry.set(VALUES, values);
			
			updates.add(entry);
		}else{
			for (Json child : j.asJsonMap().values()){
				getEntries(updates, child, vessel);
			}
		}
		
	}
	private String getVessel(String path) {
		if(!path.startsWith(VESSELS)) return null;
		int pos=path.indexOf(".")+1;
		//could be just one .\dot. vessels.123456789
		if(pos<0)return null;
		
		pos=path.indexOf(".",pos);
		if(pos<0)return path;
		return path.substring(0,pos);
	}
	
	private String getPath(Json j) {
		StringBuffer path = new StringBuffer();
		String tmp = j.getParentKey();
		path.insert(0,tmp );

		while ((j=j.up())!=null){
			logger.trace(j.toString());
			tmp=j.getParentKey();
			if(tmp!=null && tmp.length()>1){
				path.insert(0,tmp+".");
			}
		}
		return path.toString();
	}
	
	private Json getEmptyDelta(){
		Json delta = Json.object();
		//delta.set("context","vessels.motu.navigation");
		
		return delta;
	}
	

	@Subscribe
	public void recordEvent(JsonEvent jsonEvent){
		logger.debug(this.hashCode()+ " received event "+jsonEvent.getJson().toString());
		if(jsonEvent.getJson()==null)return;
		createDelta(jsonEvent.getJson());
	}
	
	@Subscribe
	public void recordEvent(DeadEvent e){
		logger.debug("Received dead event"+e);
	}
	
}
