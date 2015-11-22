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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mjson.Json;
import nz.co.fortytwo.signalk.handler.RestApiHandler;
import nz.co.fortytwo.signalk.util.JsonConstants;
import nz.co.fortytwo.signalk.util.JsonSerializer;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.StreamCache;
import org.apache.camel.component.http.HttpMessage;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.converter.stream.InputStreamCache;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.restlet.engine.io.IoUtils;

import com.ctc.wstx.util.StringUtil;

/*
 * Processes REST requests for Signal K data
 * By the time we get here it safe to do whatever is requested
 * Its safe to return whatever is requested, its filtered later.
 * 
 * @author robert
 *
 */
public class RestApiProcessor extends SignalkProcessor implements Processor {

	public static final String REST_REQUEST = "REST_REQUEST";
	//private static final String SLASH = "/";
	private static final String LIST = "list";
	public static final String REST_WILDCARD = "REST_WILDCARD";
	private static Logger logger = Logger.getLogger(RestApiProcessor.class);

	public RestApiProcessor() throws IOException {

	}

	@Override
	public void process(Exchange exchange) throws Exception {
		// the Restlet request should be available if needed
		HttpServletRequest request = exchange.getIn(HttpMessage.class)
				.getRequest();
		HttpSession session = request.getSession();
		if (logger.isDebugEnabled()) {

			logger.debug("Request = "
					+ exchange.getIn().getHeader(Exchange.HTTP_SERVLET_REQUEST)
							.getClass());
			logger.debug("Session = " + session.getId());
		}

		if (session.getId() != null) {
			exchange.getIn().setHeader(REST_REQUEST, "true");
			String remoteAddress = request.getRemoteAddr();
			String localAddress = request.getLocalAddr();
			if(Util.sameNetwork(localAddress, remoteAddress)){
				exchange.getIn().setHeader(JsonConstants.MSG_TYPE, JsonConstants.INTERNAL_IP);
			}else{
				exchange.getIn().setHeader(JsonConstants.MSG_TYPE, JsonConstants.EXTERNAL_IP);
			}
			exchange.getIn().setHeader(JsonConstants.MSG_SRC_IP, remoteAddress);
			exchange.getIn().setHeader(JsonConstants.MSG_SRC_IP_PORT, request.getRemotePort());
			
			exchange.getIn().setHeader(WebsocketConstants.CONNECTION_KEY,
					session.getId());
			
			String path = (String) exchange.getIn()
					.getHeader(Exchange.HTTP_URI);
			if (logger.isDebugEnabled()) {
				logger.debug(exchange.getIn().getHeaders());
				logger.debug(path);
			}

			if (logger.isDebugEnabled())
				logger.debug("Processing the path = " + path);
			if (!isValidPath(path)) {
				exchange.getIn().setBody("Bad Request");
				exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/plain");
				exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE,
						HttpServletResponse.SC_BAD_REQUEST);
				// response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			if (exchange.getIn().getHeader(Exchange.HTTP_METHOD).equals("GET")) {
				processGet(exchange, path);
			}
			if (exchange.getIn().getHeader(Exchange.HTTP_METHOD).equals("PUT")) {
				processPut(exchange, path);
			}
			if (exchange.getIn().getHeader(Exchange.HTTP_METHOD).equals("POST")) {
				if (exchange.getIn().getBody() instanceof StreamCache) {
					StreamCache cache = exchange.getIn().getBody(
							StreamCache.class);
					ByteArrayOutputStream writer = new ByteArrayOutputStream();
					cache.writeTo(writer);
					logger.debug("Reading the POST request:"+writer.toString());
					exchange.getIn().setBody(writer.toString());

					// POST here
					if (logger.isDebugEnabled())
						logger.debug("Processing the POST request:"
								+ exchange.getIn().getBody());
				}else{
					if (logger.isDebugEnabled())
						logger.debug("Skipping processing the POST request:"
								+ exchange.getIn().getBody().getClass());
				}
			}

		} else {
			// HttpServletResponse response =
			// exchange.getIn(HttpMessage.class).getResponse();
			exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE,
					HttpServletResponse.SC_MOVED_TEMPORARILY);
			// constant("http://somewhere.com"))
			exchange.getIn().setHeader("Location", JsonConstants.SIGNALK_AUTH);
			exchange.getIn().setBody("Authentication Required");
		}

	}

	private void processPut(Exchange exchange, String path) {
		if (path.startsWith(JsonConstants.SIGNALK_ENDPOINTS)) {
			// cant PUT here
			return;
		}
		path = standardizePath(path);

		String context = Util.getContext(path);
		if (logger.isDebugEnabled())
			logger.debug("Processing the PUT context:" + context);
		if (path.length() > context.length()) {
			path = path.substring(context.length() + 1);
		}
		// make PUT object
		// "{\"context\":\"vessels.*\",\"put\":[{"values":{\"path\":\"navigation.courseOverGroundTrue\", "value": 172.3}, "source": {"device": "/dev/actisense", "timestamp": "2014-08-15-16:00:00.081","src": "115", "pgn": "128267" }]}";
		exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
		Json json = Json.object().set(JsonConstants.CONTEXT, context);
		Json array = Json.array();
		json.set(JsonConstants.PUT, array);
		Json entry = Json.object();
		// add the source
		entry.set(JsonConstants.source, Json.object());
		// add the value
		Json values = Json.array();
		entry.set(JsonConstants.VALUES, values);
		values.set(JsonConstants.PATH, path);
		values.set(JsonConstants.VALUE,
				Json.read(exchange.getIn().getBody(String.class)));

		exchange.getIn().setBody(json.toString());

		if (logger.isDebugEnabled())
			logger.debug("Processing the PUT request:"
					+ exchange.getIn().getBody());

	}

	private boolean isValidPath(String path) {
		if (StringUtils.isBlank(path))
			return false;
		if (path.startsWith(JsonConstants.SIGNALK_API))
			return true;
		if (path.startsWith(JsonConstants.SIGNALK_ENDPOINTS))
			return true;
		return false;
	}

	private void processGet(Exchange exchange, String path)
			throws UnknownHostException {
		// check addresses request
		if (path.startsWith(JsonConstants.SIGNALK_ENDPOINTS)) {
			exchange.getIn().setHeader(Exchange.CONTENT_TYPE,
					"application/json");
			// SEND RESPONSE
			exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE,
					HttpServletResponse.SC_OK);
			path = path.substring(JsonConstants.SIGNALK_ENDPOINTS.length());
			String host = (String) exchange.getIn()
					.getHeader(Exchange.HTTP_URL);
			// could be http://localhost:8080
			int pos1 = host.indexOf("//") + 2;
			int pos2 = host.indexOf(":", pos1);
			if (pos2 < 0)
				pos2 = host.indexOf("/", pos1);
			host = host.substring(pos1, pos2);
			Json json = Util.getEndpoints(host);
			exchange.getIn().setBody(json.toString());
			return;
		}
		path = standardizePath(path);

		String context = Util.getContext(path);
		if (logger.isDebugEnabled())
			logger.debug("Processing the context:" + context);
		if (path.length() > context.length()) {
			path = path.substring(context.length() + 1);
		} else {
			path = "*";
		}
		// list
		if (context.startsWith(LIST)) {
			// make LIST obj
			// "{\"context\":\"vessels.*\",\"list\":[{\"path\":\"navigation.*\"}]}";
			exchange.getIn().setHeader(Exchange.CONTENT_TYPE,
					"application/json");
			Json json = Json.object().set(JsonConstants.CONTEXT,
					context.substring(LIST.length() + 1));
			Json array = Json.array().add(
					Json.object().set(JsonConstants.PATH, path));
			json.set(JsonConstants.LIST, array);
			exchange.getIn().setBody(json.toString());
			if (logger.isDebugEnabled())
				logger.debug("Processing the LIST request:"
						+ exchange.getIn().getBody());
			return;
		}
		// make GET obj
		// "{\"context\":\"vessels.*\",\"get\":[{\"path\":\"navigation.*\"}]}";
		exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
		Json json = Json.object().set(JsonConstants.CONTEXT, context);
		Json array = Json.array().add(
				Json.object().set(JsonConstants.PATH, path));
		json.set(JsonConstants.GET, array);
		exchange.getIn().setBody(json.toString());
		// If a GET is an absolute object return only the requested object
		// If its a wildcard, return a full tree
		if (containsWildcard(context) || containsWildcard(path)) {
			exchange.getIn().setHeader(REST_WILDCARD, "true");
		} else {
			exchange.getIn().setHeader(REST_WILDCARD, "false");
		}
		if (logger.isDebugEnabled())
			logger.debug("Processing the GET request:"
					+ exchange.getIn().getBody());

	}

	private String standardizePath(String path) {

		// check valid request.

		path = path.substring(JsonConstants.SIGNALK_API.length());
		if (path.startsWith("/"))
			path = path.substring(1);
		if (path.endsWith("/"))
			path = path.substring(0, path.length() - 1);

		path = path.replace("/", ".");
		if (logger.isDebugEnabled())
			logger.debug("Processing the path extension:" + path);
		return path;
	}

	/**
	 * true if the path contains any * or ? for a wildcard match
	 * 
	 * @param path
	 * @return
	 */
	private boolean containsWildcard(String path) {
		if (StringUtils.isBlank(path))
			return false;
		if (path.contains("*") || path.contains("?"))
			return true;
		return false;
	}

}
