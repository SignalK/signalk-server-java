/*
 *
 * Copyright (C) 2012-2014 R T Huitema. All Rights Reserved.
 * Web: www.42.co.nz
 * Email: robert@42.co.nz
 * Author: R T Huitema
 *
 * This file is part of the signalk-server-java project
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

import java.io.File;
import java.io.IOException;
import java.util.NavigableMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.http.HttpMessage;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import mjson.Json;
import nz.co.fortytwo.signalk.util.JsonPrinter;
import nz.co.fortytwo.signalk.util.JsonSerializer;
import nz.co.fortytwo.signalk.util.SignalKConstants;
import nz.co.fortytwo.signalk.util.Util;

public class LoggerProcessor extends SignalkProcessor implements Processor {
	private static Logger logger = LogManager.getLogger(LoggerProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		
		logger.debug("LoggerProcessor starts");
		HttpServletRequest request = exchange.getIn(HttpMessage.class).getRequest();
		logger.debug("Session = "+request.getSession().getId());
		HttpSession session = request.getSession();
		if (logger.isDebugEnabled()) {

			logger.debug("Request = " + exchange.getIn().getHeader(Exchange.HTTP_SERVLET_REQUEST).getClass());
			logger.debug("Session = " + session.getId());
		}

		if (session.getId() != null) {

			String remoteAddress = request.getRemoteAddr();
			String localAddress = request.getLocalAddr();
			if (!Util.sameNetwork(localAddress, remoteAddress)) {
				exchange.getIn().setHeader(SignalKConstants.MSG_TYPE, SignalKConstants.INTERNAL_IP);
			}

			if (exchange.getIn().getHeader(Exchange.HTTP_METHOD).equals("GET")) {
				processGet(exchange);
			}
			if (exchange.getIn().getHeader(Exchange.HTTP_METHOD).equals("POST")) {
				processPost(exchange);
			}
		} else {
			exchange.getIn().setHeader("Location", SignalKConstants.SIGNALK_AUTH);
			exchange.getIn().setBody("Authentication Required");
		}
	}

	private void processPost(Exchange exchange) throws IOException {
		String conf = exchange.getIn().getBody(String.class);
		//Json confJson = Json.read(conf);
		logger.debug("POST Log4j2 = " + conf);
		FileUtils.writeStringToFile(new File(Util.getRootPath()+"./conf/log4j2.json"), conf);

	}

	private void processGet(Exchange exchange) throws IOException {
		// get and return the current log4j2.json
		String conf = FileUtils.readFileToString(new File(Util.getRootPath()+"./conf/log4j2.json"));
		logger.debug("GET Log4j2 = " + conf);
		exchange.getIn().setBody(conf);
	}

}
