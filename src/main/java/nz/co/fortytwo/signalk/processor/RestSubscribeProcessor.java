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
import nz.co.fortytwo.signalk.util.JsonConstants;

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
public class RestSubscribeProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = Logger.getLogger(RestSubscribeProcessor.class);
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
		HttpServletRequest request = exchange.getIn(HttpMessage.class).getRequest();
		if(logger.isDebugEnabled())logger.debug("Session:"+request.getSession().getId());
       if(request.getSession()!=null){
	        if(request.getMethod().equals("GET")) processGet(request, exchange);
       }else{
	       	HttpServletResponse response = exchange.getIn(HttpMessage.class).getResponse();
	       	response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	       	response.sendRedirect(JsonConstants.SIGNALK_AUTH);
       }
	}

	

	/**
	 * <ul>
    * <li> NOTE: disabled. // path=[path.to.key] is appended to the context to specify subsets of the context. The path value can use jsonPath syntax.
    * <li> period=[millisecs] becomes the transmission rate, eg every period/1000 seconds.
    * <li> format=[delta|full] specifies delta or full format. Delta format is provided by default
    * <li> policy=[immediate|maximum|periodic]
        immediate means send all changes as fast as they are received, but no faster than minPeriod. By default the reply to this policy will contain the current data for the subscription so that the client has an immediate copy of the current state of the server.
        maximum means use immediate policy, but if no changes are received before period, then resend the last known values.
        periodic means simply send the last known values every period. This is the default.
    * <li> minPeriod=[millisecs] becomes the fastest transmission rate allowed, eg every minPeriod/1000 seconds. This is only relevant for policy='immediate' below to avoid swamping the client
	* </ul>
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
        if(context.length() < JsonConstants.SIGNALK_SUBSCRIBE.length()){
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
        
        //get period, default 1000.
        long period = 1000;
        if(exchange.getIn().getHeader("period")!=null){
        	period = (long) exchange.getIn().getHeader("period");
        }
        long minPeriod = 0;
        if(exchange.getIn().getHeader("minPeriod")!=null){
        	minPeriod = (long) exchange.getIn().getHeader("minPeriod");
        }
        String format = JsonConstants.FORMAT_DELTA;
        if(exchange.getIn().getHeader("format")!=null){
        	format = (String) exchange.getIn().getHeader("format");
        }
        String policy = JsonConstants.POLICY_FIXED;
        if(exchange.getIn().getHeader("policy")!=null){
        	policy = (String) exchange.getIn().getHeader("policy");
        }
        
        
        String sessionId = request.getSession().getId();
        
        int status = subscribe(context, period, minPeriod, format, policy, sessionId);
        
        // SEND RESPONSE
        //exchange.getOut().setBody("");
        response.setStatus(status);
		
	}

	protected int subscribe(String path, long period, long minPeriod, String format, String policy, String sessionId) throws Exception {
		path=path.substring(JsonConstants.SIGNALK_SUBSCRIBE.length());
		if(logger.isDebugEnabled())logger.debug("We are processing trimmed path = "+path);
		if(logger.isDebugEnabled())logger.debug("sessionId = "+sessionId);
		if(logger.isDebugEnabled())logger.debug("wsSession = "+manager.getWsSession(sessionId));
        //check valid request.
        
        if( !path.startsWith("/"+JsonConstants.VESSELS)){
        	return HttpServletResponse.SC_BAD_REQUEST;
        }
        
       //TODO: add decent Client Info here
       Subscription sub = new Subscription(manager.getWsSession(sessionId), path, period, minPeriod,format, policy);
       if(sessionId.equals(manager.getWsSession(sessionId))){
        	sub.setActive(false);
        }
        manager.addSubscription(sub);
        if(logger.isDebugEnabled())logger.debug("Subscribed  = "+sub.toString());
        return HttpServletResponse.SC_ACCEPTED;
	}
}
