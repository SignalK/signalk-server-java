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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mjson.Json;
import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.SignalKConstants;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.StreamCache;
import org.apache.camel.component.http.HttpMessage;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

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
				exchange.getIn().setHeader(SignalKConstants.MSG_TYPE, SignalKConstants.INTERNAL_IP);
			}else{
				exchange.getIn().setHeader(SignalKConstants.MSG_TYPE, SignalKConstants.EXTERNAL_IP);
			}
			exchange.getIn().setHeader(SignalKConstants.MSG_SRC_IP, remoteAddress);
			exchange.getIn().setHeader(SignalKConstants.MSG_SRC_IP_PORT, request.getRemotePort());
			
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
			exchange.getIn().setHeader("Location", SignalKConstants.SIGNALK_AUTH);
			exchange.getIn().setBody("Authentication Required");
		}

	}

	private void processPut(Exchange exchange, String path) {
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
		Json json = Json.object().set(SignalKConstants.CONTEXT, context);
		Json array = Json.array();
		json.set(SignalKConstants.PUT, array);
		Json entry = Json.object();
		// add the source
		entry.set(SignalKConstants.source, Json.object());
		// add the value
		Json values = Json.array();
		entry.set(SignalKConstants.values, values);
		values.set(SignalKConstants.PATH, path);
		values.set(SignalKConstants.value,
				Json.read(exchange.getIn().getBody(String.class)));

		exchange.getIn().setBody(json.toString());

		if (logger.isDebugEnabled())
			logger.debug("Processing the PUT request:"
					+ exchange.getIn().getBody());

	}

	private boolean isValidPath(String path) {
		if (StringUtils.isBlank(path))
			return false;
		if (path.equals(SignalKConstants.SIGNALK_DISCOVERY))
			return true;
		if (path.startsWith(SignalKConstants.SIGNALK_API))
			return true;
		return false;
	}

	private void processGet(Exchange exchange, String path) throws UnknownHostException {

		// discovery request
		if (path.equals(SignalKConstants.SIGNALK_DISCOVERY)) {
			Message in = exchange.getIn();

			String hostname = Util.getConfigProperty(ConfigConstants.HOSTNAME);
			if (StringUtils.isBlank(hostname)) {
				try {
					String header = (String) in.getHeader(Exchange.HTTP_URL);
					hostname = new URI(header).getHost();
				} catch (URISyntaxException e) {
					// Should not happen as we expect Camel to return a valid URI.
					logger.warn("Invalid URI returned from Exchange: " + in.getHeader(Exchange.HTTP_URL));
					hostname = "localhost";
				}
			}

			in.setHeader(Exchange.CONTENT_TYPE, "application/json");
			in.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_OK);
			in.setBody(discovery(hostname).toString());
			return;
		}

		path = standardizePath(path);

		String context = Util.getContext(path);
		if (logger.isDebugEnabled())
			logger.debug("Processing the context:" + context);
		if (path.length() > context.length() && context.length()>0) {
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
			Json json = Json.object().set(SignalKConstants.CONTEXT,
					context.substring(LIST.length() + 1));
			Json array = Json.array().add(
					Json.object().set(SignalKConstants.PATH, path));
			json.set(SignalKConstants.LIST, array);
			exchange.getIn().setBody(json.toString());
			if (logger.isDebugEnabled())
				logger.debug("Processing the LIST request:"
						+ exchange.getIn().getBody());
			return;
		}
		// make GET obj
		// "{\"context\":\"vessels.*\",\"get\":[{\"path\":\"navigation.*\"}]}";
		exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
		Json json = Json.object().set(SignalKConstants.CONTEXT, context);
		Json array = Json.array().add(
				Json.object().set(SignalKConstants.PATH, path));
		json.set(SignalKConstants.GET, array);
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

	// TODO: This should come from the configuration used to start the endpoints.
	static Json discovery(String hostname) {
		Json endpoints = Json.object();
		endpoints.set(SignalKConstants.websocketUrl, "ws://" + hostname + ":"
				+ Util.getConfigPropertyInt(ConfigConstants.WEBSOCKET_PORT)
				+ SignalKConstants.SIGNALK_WS);
		endpoints.set(SignalKConstants.restUrl, "http://" + hostname + ":"
				+ Util.getConfigPropertyInt(ConfigConstants.REST_PORT)
				+ SignalKConstants.SIGNALK_API + "/");
		endpoints.set(SignalKConstants.signalkTcpPort, "tcp://" + hostname + ":"
				+ Util.getConfigPropertyInt(ConfigConstants.TCP_PORT));
		endpoints.set(SignalKConstants.signalkUdpPort, "udp://" + hostname + ":"
				+ Util.getConfigPropertyInt(ConfigConstants.UDP_PORT));
		endpoints.set(SignalKConstants.nmeaTcpPort, "tcp://" + hostname + ":"
				+ Util.getConfigPropertyInt(ConfigConstants.TCP_NMEA_PORT));
		endpoints.set(SignalKConstants.nmeaUdpPort, "udp://" + hostname + ":"
				+ Util.getConfigPropertyInt(ConfigConstants.UDP_NMEA_PORT));
		if (Util.getConfigPropertyBoolean(ConfigConstants.START_STOMP))
			endpoints.set(SignalKConstants.stompPort, "stomp+nio://" + hostname + ":"
					+ Util.getConfigPropertyInt(ConfigConstants.STOMP_PORT));
		if (Util.getConfigPropertyBoolean(ConfigConstants.START_MQTT))
			endpoints.set(SignalKConstants.mqttPort, "mqtt://" + hostname + ":"
					+ Util.getConfigPropertyInt(ConfigConstants.MQTT_PORT));
		return Json.object().set("endpoints", endpoints);
	}

	private String standardizePath(String path) {

		// check valid request.

		path = path.substring(SignalKConstants.SIGNALK_API.length());
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
