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

import java.util.ArrayList;
import java.util.List;

import mjson.Json;
import nz.co.fortytwo.signalk.util.Constants;
import nz.co.fortytwo.signalk.util.JsonConstants;
import nz.co.fortytwo.signalk.util.SignalKConstants;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

/**
 * Parse the signalkModel json and remove anything that violates security
 * 
 * @author robert
 * 
 */
public class IncomingSecurityFilter extends SignalkProcessor implements
		Processor {

	private static Logger logger = Logger
			.getLogger(IncomingSecurityFilter.class);
	private List<String> whiteList = new ArrayList<String>();
	private List<String> configAcceptList = new ArrayList<String>();
	private List<String> denyList = new ArrayList<String>();

	public IncomingSecurityFilter() {
		// load lists now
		Json deny = Util.getConfigJsonArray(JsonConstants.CONFIG + DOT
				+ "server.security.deny.ip");
		Json white = Util.getConfigJsonArray(JsonConstants.CONFIG + DOT
				+ "server.security.white.ip");
		Json config = Util.getConfigJsonArray(JsonConstants.CONFIG + DOT
				+ "server.security.config.ip");
		if (deny != null) {
			for (Object o : deny.asList()) {
				denyList.add((String) o);
			}
		}
		if (white != null) {
			for (Object o : white.asList()) {
				whiteList.add((String) o);
			}
		}
		if (config != null) {
			for (Object o : config.asList()) {
				configAcceptList.add((String) o);
			}
		}
	}

	public void process(Exchange exchange) throws Exception {

		try {
			// we trust local serial
			String type = exchange.getIn().getHeader(JsonConstants.MSG_TYPE,
					String.class);
			if (JsonConstants.SERIAL.equals(type))
				return;

			// we filter on ip
			String srcIp = exchange.getIn().getHeader(JsonConstants.MSG_SRC_IP,
					String.class);
			if (logger.isDebugEnabled())
				logger.debug("Checking src ip:" + srcIp);
			if (srcIp == null) {
				logger.debug(exchange);
				return;
			}
			// denied - drop now
			if (denyList.contains(srcIp)) {
				if (logger.isDebugEnabled())
					logger.debug("Message DENIED for src ip(denyList):" + srcIp);
				exchange.getIn().setBody(null);
				return;
			}

			// save config only from allowed ips
			String configSave = exchange.getIn().getHeader(
					JsonConstants.CONFIG_ACTION, String.class);
			if (JsonConstants.CONFIG_ACTION_SAVE.equals(configSave)) {
				// must be in the configAcceptlist!
				if (configAcceptList.contains(srcIp)) {
					if (logger.isDebugEnabled())
						logger.debug("Config save allowed for src ip:" + srcIp);
					return;
				} else {
					if (logger.isDebugEnabled())
						logger.debug("Config save DENIED for src ip:" + srcIp);
					exchange.getIn().setBody(null);
					return;
				}
			}
			// we trust INTERNAL_IP
			if (JsonConstants.INTERNAL_IP.equals(type)) {
				if (logger.isDebugEnabled())
					logger.debug("Message allowed for src ip (internal):"
							+ srcIp);
				return;
			}

			// we trust our whitelist
			if (whiteList.contains(srcIp)) {
				if (logger.isDebugEnabled())
					logger.debug("Message allowed for src ip (whitelist):"
							+ srcIp);
				return;
			}

			// now we look for anomalies

			// new incoming, so flag for acceptance
			// exchange.getIn().setHeader(JsonConstants.MSG_APPROVAL,
			// JsonConstants.REQUIRED);
			// filter for evil
			Json node = exchange.getIn().getBody(Json.class);

			if (node.at(JsonConstants.UPDATES) != null
					|| node.at(JsonConstants.PUT) != null) {
				// cant be an update or put for this vessel since its external
				if (node.at(JsonConstants.CONTEXT).asString()
						.contains(SignalKConstants.self)) {
					if (logger.isDebugEnabled())
						logger.debug("Message DENIED for src ip (spoofing self):"
								+ srcIp);
					exchange.getIn().setBody(null);
					return;
				}
			}
			// filter(node);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void filter(Json node) {
		// apply rules to this object

		// recurse into object
		for (Json n : node.asJsonMap().values()) {
			filter(n);
		}

	}

}
