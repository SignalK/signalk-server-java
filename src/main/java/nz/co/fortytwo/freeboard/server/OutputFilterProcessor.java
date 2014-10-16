/*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
 * 
 * This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
 *
 *  FreeBoard is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  FreeBoard is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with FreeBoard.  If not, see <http://www.gnu.org/licenses/>.
 */

package nz.co.fortytwo.freeboard.server;

import mjson.Json;
import nz.co.fortytwo.freeboard.server.util.JsonConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Converts the hashmap of key/values back to a string
 * @author robert
 *
 */
public class OutputFilterProcessor extends FreeboardProcessor implements Processor {

	public void process(Exchange exchange) throws Exception {
		if (exchange.getIn().getBody()==null)
			return;
		//TODO: add more filters here
		Json json = (Json)exchange.getIn().getBody();
		//remove _arduino
		json.at(JsonConstants.VESSELS).at(JsonConstants.SELF).delAt("_arduino");
		//remove _config
		json.at(JsonConstants.VESSELS).at(JsonConstants.SELF).delAt("_config");
		
		exchange.getIn().setBody(json.toString());
	}

}
