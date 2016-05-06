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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nz.co.fortytwo.signalk.handler.GitHandler;
import nz.co.fortytwo.signalk.util.SignalKConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.http.HttpMessage;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

/*
 * Processes REST requests for Signal K client installs
 * 
 * @author robert
 */
public class ClientAppProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = LogManager.getLogger(ClientAppProcessor.class);

	private GitHandler handler = new GitHandler();

	@Override
	public void process(Exchange exchange) throws Exception {
		// the Restlet request should be available if neeeded
		HttpServletRequest request = exchange.getIn(HttpMessage.class).getRequest();
		HttpSession session = request.getSession();
		if (logger.isDebugEnabled())
			logger.debug("Session = " + session.getId());
		if (request.getSession() != null && allowInstall(request.getSession())) {
			if (request.getMethod().equals("GET")) {
				HttpServletResponse response = exchange.getIn(HttpMessage.class).getResponse();
				String path = request.getPathInfo();
				// String path = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
				if (logger.isDebugEnabled())
					logger.debug("We are processing the path = " + path);
				// check addresses request
				if (path.startsWith(SignalKConstants.SIGNALK_INSTALL)) {
					handler.processInstall(request, response);
					return;
				}
				if (path.startsWith(SignalKConstants.SIGNALK_UPGRADE)) {
					handler.processUpgrade(request, response);
					return;
				}
				// response codes are set here, so all good now.
			}

		} else {
			HttpServletResponse response = exchange.getIn(HttpMessage.class).getResponse();
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.sendRedirect(SignalKConstants.SIGNALK_AUTH);
			exchange.getIn().setBody(null);
		}

	}

	private boolean allowInstall(HttpSession session) {
		// TODO Need install security here
		return true;
	}

}
