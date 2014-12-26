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
import javax.servlet.http.HttpSession;

import nz.co.fortytwo.signalk.server.Subscription;
import nz.co.fortytwo.signalk.server.util.Constants;
import nz.co.fortytwo.signalk.server.util.JsonConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.http.HttpMessage;
import org.apache.camel.component.restlet.RestletConstants;
import org.apache.log4j.Logger;


/**
 * Handles subscription requests and removes.
 * use params ?period=1000 (ms) as send interval
 * @author robert
 *
 */
public class SubscribeProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = Logger.getLogger(SubscribeProcessor.class);
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
		HttpServletRequest request = exchange.getIn(HttpMessage.class).getRequest();
		 HttpSession session = request.getSession();
		 logger.debug("Session:"+session.getId());
       if(request.getSession()!=null){
	        if(request.getMethod().equals("GET")) processGet(request, exchange);
       }else{
	       	HttpServletResponse response = exchange.getIn(HttpMessage.class).getResponse();
	       	response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	       	response.sendRedirect(JsonConstants.SIGNALK_AUTH);
       }
	}

	

	private void processGet(HttpServletRequest request, Exchange exchange) {
		// use Restlet API to create the response
		HttpServletResponse response = exchange.getIn(HttpMessage.class).getResponse();
        
		String path =  exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
        logger.debug("We are processing the path = "+path);
        
        //check valid request.
        if(path.length()<JsonConstants.SIGNALK_SUBSCRIBE.length()){
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	return;
        }
        path=path.substring(JsonConstants.SIGNALK_SUBSCRIBE.length());
        //get period, default 1000.
        long period = 1000;
        if(exchange.getIn().getHeader("period")!=null){
        	period = (long) exchange.getIn().getHeader("period");
        }
        
        String sessionId = request.getSession().getId();
        
        int status = subscribe(path, period, sessionId);
        
        // SEND RESPONSE
        exchange.getOut().setBody("");
        response.setStatus(status);
		
	}

	protected int subscribe(String path, long period, String sessionId) {
		//String path =  request.getOriginalRef().getPath();
        logger.debug("We are processing the path = "+path);
        int len = JsonConstants.SIGNALK_SUBSCRIBE.length();
        //check valid request.
        
        if(path.length()<len || !path.startsWith(JsonConstants.SIGNALK_SUBSCRIBE+JsonConstants.VESSELS)){
        	return HttpServletResponse.SC_BAD_REQUEST;
        }
        path=path.substring(JsonConstants.SIGNALK_SUBSCRIBE.length());
       //TODO: add decent Client Info here
        Subscription sub = new Subscription(manager.getWsSession(sessionId), path, period);
        if(sessionId.equals(manager.getWsSession(sessionId))){
        	sub.setActive(false);
        }
        manager.addSubscription(sub);
        logger.debug("Subscribed  = "+sub.toString());
        return HttpServletResponse.SC_ACCEPTED;
	}
}
