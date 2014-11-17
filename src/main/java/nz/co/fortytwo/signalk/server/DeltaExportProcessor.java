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
public class DeltaExportProcessor extends FreeboardProcessor implements Processor{

	private static Logger logger = Logger.getLogger(DeltaExportProcessor.class);

	protected ConcurrentHashMap<String, Json> map = new ConcurrentHashMap<String, Json>();
	public DeltaExportProcessor(){
		super();
		signalkModel.getEventBus().register(this);
		
	}
	public void process(Exchange exchange) throws Exception {
		
		try {
			logger.debug("process delta queue");
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
		synchronized (map) {
			Json delta = map.get(vessel);
			if(delta==null){
				delta=getEmptyDelta();
				delta.set("context",vessel);
				map.put(vessel, delta);
			}
			delta.at(UPDATES).add(entry);
		}
	}
	
	private String getVessel(String path) {
		if(!path.startsWith(VESSELS)) return null;
		int pos=path.indexOf(".")+1;
		pos=path.indexOf(".",pos);
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
		Json updates = Json.array();
		delta.set("updates",updates);
		return delta;
	}
	

	@Subscribe
	public void recordEvent(JsonEvent jsonEvent){
		logger.debug("Received event"+jsonEvent.getJson().toString());
		if(jsonEvent.getJson()==null)return;
		createDelta(jsonEvent.getJson());
	}
	
	@Subscribe
	public void recordEvent(DeadEvent e){
		logger.debug("Received dead event"+e);
	}
	
}
