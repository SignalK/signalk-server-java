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

import static nz.co.fortytwo.signalk.server.util.JsonConstants.CONTEXT;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.FORMAT_DELTA;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.PATH;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.SIGNALK_FORMAT;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.SOURCE;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.UPDATES;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.VALUE;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.VALUES;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.VESSELS;
import mjson.Json;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Convert the full format to delta format
 * 
 * @author robert
 * 
 */
public class FullToDeltaProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = Logger.getLogger(FullToDeltaProcessor.class);
	private static DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

	public void process(Exchange exchange) throws Exception {

		try {
			if (exchange.getIn().getBody() == null || !(exchange.getIn().getBody() instanceof Json))
				return;
			if (FORMAT_DELTA.equals(exchange.getIn().getHeader(SIGNALK_FORMAT))) {
				Json json = handle(exchange.getIn().getBody(Json.class));
				if(logger.isDebugEnabled())logger.debug("Converted to delta :" + json);
				exchange.getIn().setBody(json);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/*
	 * {
	 * "context": "vessels.motu.navigation",
	 * "updates":[
	 * {
	 * "source": {
	 * "device" : "/dev/actisense",
	 * "timestamp":"2014-08-15-16:00:00.081",
	 * "src":"115",
	 * "pgn":"128267"
	 * },
	 * "values": [
	 * { "path": "courseOverGroundTrue","value": 172.9 },
	 * { "path": "speedOverGround","value": 3.85 }
	 * ]
	 * },
	 * {
	 * "source": {
	 * "device" : "/dev/actisense",
	 * "timestamp":"2014-08-15-16:00:00.081",
	 * "src":"115",
	 * "pgn":"128267"
	 * },
	 * "values": [
	 * { "path": "courseOverGroundTrue","value": 172.9 },
	 * { "path": "speedOverGround","value": 3.85 }
	 * ]
	 * }
	 * ]
	 * 
	 * }
	 */

	// @Override
	public Json handle(Json node) {
		// avoid full signalk syntax
		if (node.has(CONTEXT))
			return node;
		// deal with diff format
		if (node.has(VESSELS)) {
			if(logger.isDebugEnabled())logger.debug("processing full format  " + node);
			// find the first branch that splits
			Json ctx = getContext(node);
			String context = ctx.getPath();
			// process it

			// add values
			Json updates = Json.array();
			getEntries(updates, ctx, context.length() + 1);

			if (updates.asList().size() == 0)
				return null;

			Json delta = Json.object();
			delta.set(CONTEXT, context);
			delta.set(UPDATES, updates);

			return delta;
		}
		// misc types
		return node;
	}

	/**
	 * Find the first node with more than one child.
	 * 
	 * @param node
	 * @return
	 */
	private Json getContext(Json node) {
		// look down the tree until we get more than one branch, thats the context
		if (node.asJsonMap().size() > 1)
			return node;
		for (Json j : node.asJsonMap().values()) {
			return getContext(j);
		}
		return node;
	}

	private void getEntries(Json updates, Json j, int prefix) {
		if (!j.isObject())
			return;

		for (Json js : j.asJsonMap().values()) {
			if (js == null)
				continue;
			Json entry = Json.object();
			if (js.has(SOURCE)) {
				Json jsSrc = js.at(SOURCE);
				entry.set(SOURCE, jsSrc.getValue());
				if (jsSrc.isString()) {
					// recurse
					Json ref = js.at(jsSrc.asString());
					if (ref != null) {
						ref.delAt(VALUE);
						entry.set(jsSrc.asString(), ref);
					}
				}
			}
			if (js.has(VALUE)) {
				String path = js.getPath().substring(prefix);

				Json value = Json.object();
				value.set(PATH, path);
				value.set(VALUE, js.at(VALUE).getValue());

				Json values = Json.array();
				values.add(value);
				entry.set(VALUES, values);
				updates.add(entry);
			} else if (js.isObject()) {
				getEntries(updates, js, prefix);
			}
		}

	}

}
