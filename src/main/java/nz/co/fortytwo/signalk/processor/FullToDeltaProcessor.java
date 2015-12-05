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
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.fortytwo.signalk.processor;

import static nz.co.fortytwo.signalk.util.SignalKConstants.FORMAT_DELTA;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_FORMAT;

import java.util.List;

import mjson.Json;
import nz.co.fortytwo.signalk.handler.FullToDeltaConverter;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

/**
 * Convert the full format to delta format
 * 
 * @author robert
 * 
 */
public class FullToDeltaProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = Logger.getLogger(FullToDeltaProcessor.class);

	private FullToDeltaConverter fullToDelta = new FullToDeltaConverter();
	
	public void process(Exchange exchange) throws Exception {

		try {
			if (exchange.getIn().getBody() == null) return;
			if(logger.isDebugEnabled())logger.debug("Processing :" + exchange.getIn());
			if(!(exchange.getIn().getBody() instanceof Json))return;
			if (FORMAT_DELTA.equals(exchange.getIn().getHeader(SIGNALK_FORMAT))) {
				List<Json> json = fullToDelta.handle(exchange.getIn().getBody(Json.class));
				if(logger.isDebugEnabled())logger.debug("Converted to delta :" + json);
				exchange.getIn().setBody(json);
			}
			if(logger.isDebugEnabled())logger.debug("Outputting :" + exchange.getIn());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
