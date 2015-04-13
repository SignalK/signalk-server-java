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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.fortytwo.signalk.util.Constants;
import nz.co.fortytwo.signalk.util.JsonConstants;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.http.HttpMessage;
import org.apache.log4j.Logger;


/**
 * Handles subscription requests and removes.
 * use params ?period=1000 (ms) as send interval
 * @author robert
 *
 */
public class WsUrlProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = Logger.getLogger(WsUrlProcessor.class);
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
		HttpServletRequest request = exchange.getIn(HttpMessage.class).getRequest();
		if(logger.isDebugEnabled()){
			logger.debug("Session:"+request.getSession().getId());
			logger.debug("Protocol:"+request.getProtocol());
		}
       if(request.getSession()!=null){
	        if(request.getMethod().equals("GET")) processGet(request, exchange);
       }else{
	       	HttpServletResponse response = exchange.getIn(HttpMessage.class).getResponse();
	       	response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	       	response.sendRedirect(JsonConstants.SIGNALK_AUTH);
       }
	}

	

	
	/**
	 * Returns the websocket url
	 * @param request
	 * @param exchange
	 * @throws Exception
	 */
	private void processGet(HttpServletRequest request, Exchange exchange) throws Exception {
		// use Restlet API to create the response
		HttpServletResponse response = exchange.getIn(HttpMessage.class).getResponse();
        
		String context =  exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
		if(logger.isDebugEnabled())logger.debug("We are processing the path = "+context);
        
        //check valid request.
        if(context.length() < JsonConstants.SIGNALK_WS_URL.length()){
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	if(logger.isDebugEnabled())logger.debug("Returning SC_BAD_REQUEST");
        	return;
        }
        //avoid primus.js requests!
        if(context.endsWith(".js")||context.endsWith(".html")){
        	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        	if(logger.isDebugEnabled())logger.debug("Returning SC_NOT_FOUND");
        	return;
        }
        String uri = request.isSecure()?"wss://":"ws://" +   // "ws" + "://
                request.getServerName() +       // "myhost"
                ":" +                           // ":"
                Util.getConfigProperty(Constants.WEBSOCKET_PORT) +       // "3000"
                JsonConstants.SIGNALK_WS;
        // SEND RESPONSE
        exchange.getOut().setBody(uri);
        response.setStatus(HttpServletResponse.SC_OK);
		
	}

}
