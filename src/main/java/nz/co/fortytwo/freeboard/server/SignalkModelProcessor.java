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
package nz.co.fortytwo.freeboard.server;

import mjson.Json;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

/**
 * Updates the signalkModel with the current json
 * 
 * @author robert
 * 
 */
public class SignalkModelProcessor extends FreeboardProcessor implements Processor{

	private static Logger logger = Logger.getLogger(SignalkModelProcessor.class);
	

	public void init() {
		this.producer = CamelContextFactory.getInstance().createProducerTemplate();
		producer.setDefaultEndpointUri("direct:command");
	}
	
	public void process(Exchange exchange) throws Exception {
		
		try {
			handle(exchange.getIn().getBody(Json.class));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	//@Override
	public void handle(Json node) {
		//TODO: is the node valid?
		logger.debug("SignalkModelProcessor  updating "+node );
		signalkModel.merge(node);
		
	}

}
