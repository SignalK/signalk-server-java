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
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.fortytwo.signalk.server.SubscriptionManagerFactory;
import nz.co.fortytwo.signalk.util.Constants;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpException;
import org.eclipse.jetty.http.HttpParser;
import org.eclipse.jetty.io.ConnectedEndPoint;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.BlockingHttpConnection;
import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.websocket.Extension;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketBuffers;
import org.eclipse.jetty.websocket.WebSocketFactory;
import org.eclipse.jetty.websocket.WebSocketServletConnection;
import org.eclipse.jetty.websocket.WebSocketServletConnectionD00;
import org.eclipse.jetty.websocket.WebSocketServletConnectionD06;
import org.eclipse.jetty.websocket.WebSocketServletConnectionD08;
import org.eclipse.jetty.websocket.WebSocketServletConnectionRFC6455;

public class SignalkWebSocketServlet extends WebsocketComponentServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(SignalkWebSocketServlet.class);
	private WebSocketFactory _webSocketFactory;

	public SignalkWebSocketServlet(NodeSynchronization sync) {
		super(sync);
	}

	public void init() throws ServletException {
		try {
			String bs = getInitParameter("bufferSize");
			if(logger.isDebugEnabled())logger.debug("Upgrade ws, create factory:");
			this._webSocketFactory = new WebSocketFactory(this, (bs == null) ? 8192 : Integer.parseInt(bs)) {
				private WebSocketBuffers _buffers = new WebSocketBuffers(8192);

				public void upgrade(HttpServletRequest request, HttpServletResponse response, WebSocket websocket, String protocol) throws IOException {
					String sessionId = request.getRequestedSessionId();
					if(logger.isDebugEnabled())logger.debug("Upgrade ws, requested sessionId:" + sessionId);
					if(StringUtils.isBlank(sessionId)){
						sessionId=request.getSession().getId();
						if(logger.isDebugEnabled())logger.debug("Request.sessionId:"+sessionId);
						
					}
					if(StringUtils.isBlank(sessionId)){
						sessionId=((DefaultWebsocket) websocket).getConnectionKey();
						if(logger.isDebugEnabled())logger.debug("Request.wsSessionId:"+sessionId);
						
					}
					
					if (!("websocket".equalsIgnoreCase(request.getHeader("Upgrade"))))
						throw new IllegalStateException("!Upgrade:websocket");
					if (!("HTTP/1.1".equals(request.getProtocol()))) {
						throw new IllegalStateException("!HTTP/1.1");
					}
					int draft = request.getIntHeader("Sec-WebSocket-Version");
					if (draft < 0) {
						draft = request.getIntHeader("Sec-WebSocket-Draft");
					}

					int requestedVersion = draft;
					AbstractHttpConnection http = AbstractHttpConnection.getCurrentConnection();
					if (http instanceof BlockingHttpConnection)
						throw new IllegalStateException("Websockets not supported on blocking connectors");
					ConnectedEndPoint endp = (ConnectedEndPoint) http.getEndPoint();

					List<String> extensions_requested = new ArrayList<>();

					Enumeration<String> e = request.getHeaders("Sec-WebSocket-Extensions");
					while (e.hasMoreElements()) {
						QuotedStringTokenizer tok = new QuotedStringTokenizer((String) e.nextElement(), ",");
						while (tok.hasMoreTokens()) {
							extensions_requested.add(tok.nextToken());
						}

					}

					if (draft < getMinVersion())
						draft = 2147483647;
					
					WebSocketServletConnection connection;
					switch (draft) {
					case -1:
					case 0:
						connection = new WebSocketServletConnectionD00(this, websocket, endp, this._buffers, http.getTimeStamp(), (int) getMaxIdleTime(),
								protocol);
						break;
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
						connection = new WebSocketServletConnectionD06(this, websocket, endp, this._buffers, http.getTimeStamp(), (int) getMaxIdleTime(),
								protocol);
						break;
					case 7:
					case 8:
						List<Extension> extensions = initExtensions(extensions_requested, 5, 5, 3);
						connection = new WebSocketServletConnectionD08(this, websocket, endp, this._buffers, http.getTimeStamp(), (int) getMaxIdleTime(),
								protocol, extensions, draft);
						break;
					case 13:
						List<Extension> extensions1 = initExtensions(extensions_requested, 5, 5, 3);
						connection = new WebSocketServletConnectionRFC6455(this, websocket, endp, this._buffers, http.getTimeStamp(), (int) getMaxIdleTime(),
								protocol, extensions1, draft);
						break;
					case 9:
					case 10:
					case 11:
					case 12:
					default:
						String versions = "13";
						if (getMinVersion() <= 8)
							versions = new StringBuilder().append(versions).append(", 8").toString();
						if (getMinVersion() <= 6)
							versions = new StringBuilder().append(versions).append(", 6").toString();
						if (getMinVersion() <= 0) {
							versions = new StringBuilder().append(versions).append(", 0").toString();
						}
						response.setHeader("Sec-WebSocket-Version", versions);

						StringBuilder err = new StringBuilder();
						err.append("Unsupported websocket client version specification ");
						if (requestedVersion >= 0)
							err.append("[").append(requestedVersion).append("]");
						else {
							err.append("<Unspecified, likely a pre-draft version of websocket>");
						}
						err.append(", configured minVersion [").append(getMinVersion()).append("]");
						err.append(", reported supported versions [").append(versions).append("]");
						// LOG.warn(err.toString(), new Object[0]);

						throw new HttpException(400, "Unsupported websocket version specification");
					}

					addConnection(connection);

					connection.getConnection().setMaxBinaryMessageSize(getMaxBinaryMessageSize());
					connection.getConnection().setMaxTextMessageSize(getMaxTextMessageSize());

					connection.handshake(request, response, protocol);
					response.flushBuffer();

					connection.fillBuffersFrom(((HttpParser) http.getParser()).getHeaderBuffer());
					connection.fillBuffersFrom(((HttpParser) http.getParser()).getBodyBuffer());
					
					//if(logger.isDebugEnabled())logger.debug("Upgraded session " + request.getSession().getId() + " to ws " + ((DefaultWebsocket) websocket).getConnectionKey());
					if(logger.isDebugEnabled())logger.debug("Upgraded session " + sessionId + " to ws " + ((DefaultWebsocket) websocket).getConnectionKey());
					try {
						SubscriptionManagerFactory.getInstance().add(sessionId, ((DefaultWebsocket) websocket).getConnectionKey(), Constants.OUTPUT_WS);
					} catch (Exception e1) {
						logger.error(e1.getMessage(),e1);
						throw new IOException(e1);
					}
					// LOG.debug("Websocket upgrade {} {} {} {}", new Object[] { request.getRequestURI(), Integer.valueOf(draft), protocol, connection });
					request.setAttribute("org.eclipse.jetty.io.Connection", connection);
				}
			};
			this._webSocketFactory.start();

			String max = getInitParameter("maxIdleTime");
			if (max != null) {
				this._webSocketFactory.setMaxIdleTime(Integer.parseInt(max));
			}
			max = getInitParameter("maxTextMessageSize");
			if (max != null) {
				this._webSocketFactory.setMaxTextMessageSize(Integer.parseInt(max));
			}
			max = getInitParameter("maxBinaryMessageSize");
			if (max != null) {
				this._webSocketFactory.setMaxBinaryMessageSize(Integer.parseInt(max));
			}
			String min = getInitParameter("minVersion");
			if (min != null)
				this._webSocketFactory.setMinVersion(Integer.parseInt(min));
		} catch (ServletException x) {
			throw x;
		} catch (Exception x) {
			throw new ServletException(x);
		}
	}

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if ((this._webSocketFactory.acceptWebSocket(request, response)) || (response.isCommitted()))
			return;
		super.service(request, response);
	}

	public boolean checkOrigin(HttpServletRequest request, String origin) {
		return true;
	}

	public void destroy() {
		try {
			this._webSocketFactory.stop();
		} catch (Exception x) {
			logger.warn(x.getMessage());
		}
	}
}
