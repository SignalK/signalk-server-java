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

import java.io.IOException;

import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.util.JsonConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import mjson.Json;

/**
 * Updates the signalkModel with the current json
 * 
 * @author robert
 * 
 */
public class SignalkModelProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = Logger.getLogger(SignalkModelProcessor.class);

	public void process(Exchange exchange) throws Exception {

		try {

			if (exchange.getIn().getBody() instanceof SignalKModel) {
				handle(exchange.getIn().getBody(SignalKModel.class));
				//we want to stop here
				exchange.getIn().setBody(null);
			}else{
				if(logger.isDebugEnabled())logger.debug("Ignored, not update:"+exchange.getIn().getBody(Json.class));
			}

		} catch (IllegalArgumentException e) {
			logger.info(e.getMessage());
		}catch (Exception e) {
				logger.error(e.getMessage(), e);
		}
	}

	// @Override
	public void handle(SignalKModel node) throws IOException {
		if (node.getData().size() == 0)
			return;
		if (logger.isDebugEnabled())
			logger.debug("SignalkModelProcessor  updating " + node);

		signalkModel.putAll(node.getData());
		if(node.getSubMap(JsonConstants.CONFIG)!=null){
			SignalKModelFactory.saveConfig(signalkModel);
		}
		if (logger.isDebugEnabled())
			logger.debug(signalkModel);
	}

}
