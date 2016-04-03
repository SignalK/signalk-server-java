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

import nz.co.fortytwo.signalk.handler.NMEAHandler;
import nz.co.fortytwo.signalk.model.SignalKModel;

import static nz.co.fortytwo.signalk.util.SignalKConstants.MSG_SERIAL_PORT;
import static nz.co.fortytwo.signalk.util.SignalKConstants.MSG_SRC_BUS;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;



/**
 * Processes NMEA sentences in the body of a message, firing events to interested listeners
 * Converts the NMEA messages to signalk
 * 
 * @author robert
 * 
 */
public class NMEAProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = Logger.getLogger(NMEAProcessor.class);
	

	private NMEAHandler nmea = new NMEAHandler();

	

	public void process(Exchange exchange) throws Exception {
		if (exchange.getIn().getBody() == null || !(exchange.getIn().getBody() instanceof String)) {
			return;
		}
		
		String body = exchange.getIn().getBody(String.class);
		String src = exchange.getIn().getHeader(MSG_SRC_BUS, String.class);
		SignalKModel model = nmea.handle(body, src);
		if(model!=null){
			exchange.getIn().setBody(model);
		}
	}

	

}
