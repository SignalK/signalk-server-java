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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.event.JsonEvent;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.CamelContextFactory;
import nz.co.fortytwo.signalk.server.RouteManager;
import nz.co.fortytwo.signalk.server.Subscription;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;
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
	protected String wsSession = null;
	protected ConcurrentHashMap<String, Json> map = new ConcurrentHashMap<String, Json>();
	private ProducerTemplate exportProducer = null;
	
	public DeltaExportProcessor(String wsSession){
		super();
		this.wsSession=wsSession;
		exportProducer= new DefaultProducerTemplate(CamelContextFactory.getInstance());
		exportProducer.setDefaultEndpointUri(RouteManager.SEDA_COMMON_OUT);
		try {
			exportProducer.start();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		signalkModel.getEventBus().register(this);
		
		
	}
	public void process(Exchange exchange) throws Exception {
		
		try {
			logger.info("process delta queue ("+map.size()+") for "+exchange.getFromRouteId());
			//get the accumulated delta nodes.
			exchange.getIn().setBody(createTree(exchange.getFromRouteId()));
			logger.debug("Body set to :"+exchange.getIn().getBody());
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		}
	}
	

	private SignalKModel createTree(String routeId) {
		//we need the routeId.
		//Multimap<String,String> vesselList = TreeMultimap.create();
		SignalKModel temp = SignalKModelFactory.getCleanInstance();
		for(Subscription sub : manager.getSubscriptions(wsSession)){
			if(sub==null || !sub.isActive() || !routeId.equals(sub.getRouteId())) continue;
			for(String p : sub.getSubscribed(null)){
				Json node = signalkModel.findNode(p);
				logger.debug("Found node:"+p+" = "+node);
				if(node!=null){
					Json n = temp.addNode((Json) temp, node.up().getPath());
					if(node.isPrimitive()){
						n.set(node.getParentKey(), node.getValue());
					}else{
						logger.debug("Object at end of path! : "+ node);
					}
				}
			}
		}
		return temp;
		
	}
	
	/*
	 *  
<pre>
{
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
	</pre>
	*
*/
	
	
	private void createDelta(Json j) {
		if(j==null)return;
		// create element
		String path = j.getPath();
		logger.debug("Delta for:"+path);
		String vessel = getVessel(path);
		if(vessel==null)return;
		
		//recurse the objects to the leaves
		Json updates = Json.array();
		getEntries(updates, j);
		if(updates.asList().size()==0)return;
		
		synchronized (map) {
			Json delta = map.get(vessel);
			if(delta==null){
				delta=getEmptyDelta();
				delta.set(CONTEXT,vessel);
				map.put(vessel, delta);
			}
			if(delta.has(UPDATES)){
				for(Json update:updates.asJsonList()){
					delta.at(UPDATES).add(update);
				}
			}else{
				delta.set(UPDATES,updates);
			}
		}
	}
	
	private void getEntries(Json updates, Json j) {
		if(!j.isObject())return;
		String path = j.getPath();
		//do we want this one, remember we may have a wildcard subscribe
		List<String> pathList = new ArrayList<String>();
		logger.debug("Checking subs for session="+wsSession+" for:"+path);
		for(Subscription s: manager.getSubscriptions(wsSession)){
			logger.debug("Checking prefix="+s.getPath()+" for:"+path);
			if(s.isActive()){
				pathList.addAll(s.getSubscribed(path));
				logger.debug("Found for:"+s + ", pathlist="+pathList.size());
			}
		}
		
		if(pathList.size()==0)return;
		//load objects
		
		for(String p: pathList){
			Json js = signalkModel.getFromNodeMap(p); 
			logger.debug("Found path:"+p +" = "+js);
			if(js==null)continue;
			if( js.isObject() && js.has(VALUE)){
				//path=path.substring(vessel.length()+1);
				logger.debug("Found with value:"+js);
				Json entry = Json.object();
				Json source = Json.object();
				if(js.up().has(SOURCE)){
					source.set(SOURCE, js.up().at(SOURCE).getValue());
				}
				if(js.up().has(TIMESTAMP)){
					source.set(TIMESTAMP, js.up().at(TIMESTAMP).getValue());
				}
				entry.set(SOURCE, source);
				
				Json value = Json.object();
				value.set(PATH,p.substring(p.indexOf(".",VESSELS.length()+1)+1));
				value.set(VALUE, js.at(VALUE).getValue());
				
				Json values = Json.array();
				values.add(value);
				entry.set(VALUES, values);
				
				updates.add(entry);
			}
		}
		
	}
	private String getVessel(String path) {
		if(!path.startsWith(VESSELS)) return null;
		int pos=path.indexOf(".")+1;
		//could be just 'vessels'
		if(pos<1)return path;
		
		pos=path.indexOf(".",pos);
		//could be just one .\dot. vessels.123456789
		if(pos<0)return path;
		return path.substring(0,pos);
	}
	
	
	
	private Json getEmptyDelta(){
		Json delta = Json.object();
		//delta.set("context","vessels.motu.navigation");
		
		return delta;
	}
	

	@Subscribe
	public void recordEvent(JsonEvent jsonEvent){
		if(jsonEvent==null)return;
		if(jsonEvent.getJson()==null)return;
		if(logger.isDebugEnabled()) logger.debug(this.hashCode()+ " received event "+jsonEvent.getJson().toString());
		
		//do we care?
		for(Subscription s: manager.getSubscriptions(wsSession)){
			if(s.isActive() && !POLICY_FIXED.equals(s.getPolicy())){
				createDelta(jsonEvent.getJson());
				break;
			}
		}
		//now send
		List<Json> deltas = null;
		synchronized (map) {
			deltas = ImmutableList.copyOf(map.values());
			map.clear();
		}
		if(deltas.size()>0){
			//dont send empty updates
			for(Json d:deltas){
				exportProducer.sendBodyAndHeader(d, WebsocketConstants.CONNECTION_KEY, wsSession);
			}
		}
	}
	
	@Subscribe
	public void recordEvent(DeadEvent e){
		logger.debug("Received dead event"+e);
	}
	
	public ProducerTemplate getExportProducer(){
		return exportProducer;
	}
	public void setExportProducer(ProducerTemplate exportProducer) {
		this.exportProducer = exportProducer;
	}
	
}
