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
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.util.JsonSerializer;
import nz.co.fortytwo.signalk.util.SignalKConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

/**
 * Converts the hashmap of key/values back to a string
 * @author robert
 *
 */
public class ConfigFilterProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = LogManager.getLogger(ConfigFilterProcessor.class);
	JsonSerializer ser = new JsonSerializer();
	private boolean allow;
	
	public ConfigFilterProcessor(boolean allow){
		this.allow=allow;
	}
	
	public void process(Exchange exchange) throws Exception {
		
		if (exchange.getIn().getBody()==null)
			return;
		if(logger.isDebugEnabled())logger.debug("Processing, class="+exchange.getIn().getBody().getClass());
		//TODO: add more filters here
		if(exchange.getIn().getBody() instanceof Json){
			Json json = (Json)exchange.getIn().getBody();
			//remove _arduino
			try{
				if(allow){
					json.delAt(SignalKConstants.vessels);
				}else{
					json.delAt(SignalKConstants.CONFIG);
				}
			}catch(NullPointerException npe){}
			
			exchange.getIn().setBody(json.toString());
		}
		if (exchange.getIn().getBody() instanceof SignalKModel){
			SignalKModel model = (SignalKModel)exchange.getIn().getBody();
			//remove _arduino
			try{
				if(allow){
					model.getFullData().remove(SignalKConstants.vessels);
				}else{
					model.getFullData().remove(SignalKConstants.CONFIG);
				}
			}catch(NullPointerException npe){}
			
			exchange.getIn().setBody(ser.write(model));
		}
		if(logger.isDebugEnabled()){
			logger.debug("Outputting:"+exchange.getIn().getHeaders());
			logger.debug("Outputting:"+exchange.getIn());
		}
	}

}
