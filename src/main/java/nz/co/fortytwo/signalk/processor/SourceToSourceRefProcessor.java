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

import static nz.co.fortytwo.signalk.util.SignalKConstants.MSG_TYPE;
import static nz.co.fortytwo.signalk.util.SignalKConstants.dot;
import static nz.co.fortytwo.signalk.util.SignalKConstants.label;
import static nz.co.fortytwo.signalk.util.SignalKConstants.source;
import static nz.co.fortytwo.signalk.util.SignalKConstants.sourceRef;
import static nz.co.fortytwo.signalk.util.SignalKConstants.sources;

import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nz.co.fortytwo.signalk.model.SignalKModel;

/**
 * Replaces source with the actual $sourceRef object and stores source in sources.*
 * 
 * @author robert
 *
 */
public class SourceToSourceRefProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = LogManager.getLogger(SourceToSourceRefProcessor.class);
	
	
	public void process(Exchange exchange) throws Exception {
		
		if (exchange.getIn().getBody()==null)
			return;
		if(logger.isDebugEnabled())logger.debug("Processing:"+exchange.getIn().getBody().getClass());
		
		if (exchange.getIn().getBody() instanceof SignalKModel){
			SignalKModel model = exchange.getIn().getBody(SignalKModel.class);
			if(logger.isDebugEnabled())logger.debug("Processing:"+model);
			for(String key : model.getKeys()){
				//get the source.type key
				//if(logger.isDebugEnabled())logger.debug("Key:"+key);
				if(key.endsWith(dot+source+dot+label)){
					if(logger.isDebugEnabled())logger.debug("Convert key:"+key);
					int pos = key.indexOf(dot+source+dot);
					String typeVal = exchange.getIn().getHeader(MSG_TYPE,String.class);
					
					int pos1 = pos+source.length()+2;
					String refKey = key.substring(0,pos);
					if(logger.isDebugEnabled())logger.debug("refKey:"+refKey+", bus:"+typeVal);
					//get the label
					String lbl = (String) model.get(refKey+dot+source+dot+label);
					if(logger.isDebugEnabled())logger.debug("refKey:"+refKey+", label:"+lbl);
					//set sourceRef
					model.getFullData().put(refKey+dot+sourceRef, typeVal+dot+lbl);
					//put in sources
					NavigableMap<String, Object> node = signalkModel.getSubMap(refKey+dot+source);
					if(logger.isDebugEnabled())logger.debug("Found keys:"+node.size());
					//node is the source object 
					if(node!=null){
						for(Entry<String, Object> entry:node.entrySet()){
							String nodeKey = entry.getKey().substring(pos1);
							model.getFullData().put(sources+dot+typeVal+dot+lbl+dot+nodeKey, entry.getValue());
							if(logger.isDebugEnabled())logger.debug("Added key:"+sources+dot+typeVal+dot+lbl+dot+nodeKey+"="+entry.getValue());
						}
					
						//drop the source key and all subkeys
						for(Entry<String, Object> entry:node.entrySet()){
							String nodeKey = entry.getKey();
							model.getFullData().remove(nodeKey);
							if(logger.isDebugEnabled())logger.debug("Remove key:"+nodeKey);
						}
					}
					
				}
			}
			exchange.getIn().setBody(model);
			if(logger.isDebugEnabled())logger.debug("Outputting:"+exchange.getIn());
		}
		
		
	}



}
