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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.fortytwo.signalk.util.SignalKConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.http.HttpMessage;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.MimeTypes;


/**
 * Processes auth requests for Signal K data
 * 
 * @author robert
 *
 */
public class RestAuthProcessor extends SignalkProcessor implements Processor{

	private static Logger logger = LogManager.getLogger(RestAuthProcessor.class);
	
	@Override
	public void process(Exchange exchange) throws Exception {
		// the Restlet request should be available if neeeded
		HttpServletRequest request = exchange.getIn(HttpMessage.class).getRequest();
		
		 //HttpSession session = request.getSession();
		if(logger.isDebugEnabled())logger.debug("Session = "+request.getSession().getId());
       // Request request = exchange.getIn().getHeader(RestletConstants.RESTLET_REQUEST, Request.class);
        if("GET"==request.getMethod()){
        	processGet(request, exchange);
        }else{
        	exchange.getIn(HttpMessage.class).getResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
	}

	private void processGet(HttpServletRequest request, Exchange exchange) throws Exception {
		// use Restlet API to create the response
		
        HttpServletResponse response = exchange.getIn(HttpMessage.class).getResponse();
        String path =  exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
        if(logger.isDebugEnabled())logger.debug("We are processing the path = "+path);
        
        //check valid request.
        if(path.length()<=SignalKConstants.SIGNALK_AUTH.length()){
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	return;
        }
       // path=path.substring(request.getRootRef().getPath().length());
      
        //logger.debug("We are processing the extension:"+path);
       //TODO: sort out proper security here!
      
        response.setContentType(MimeTypes.TEXT_PLAIN );
        //String uuid = UUID.randomUUID().toString();
        //Cookie cookie = new Cookie(Constants.SESSIONID, uuid);
       // cookie.setPath("/signalk/");
       // response.addCookie(cookie);
        //String breadcrumb = exchange.getIn().getHeader(Exchange.BREADCRUMB_ID,String.class);
        //breadcrumb = breadcrumb.substring(0,breadcrumb.lastIndexOf("-",breadcrumb.lastIndexOf("-")));
        //manager.add(cookieSetting.getValue(), cookieSetting.getValue());
        if(logger.isDebugEnabled())logger.info("Adding session = "+request.getSession().getId());
        //manager.add(request.getSession().getId(), request.getSession().getId(), Constants.OUTPUT_WS);
       
        // SEND RESPONSE
        //exchange.getOut().setBody(response.getEntityAsText());
       
        response.setStatus(HttpServletResponse.SC_OK);
        //send back
        //response.redirectSeeOther(request.getReferrerRef());
		
	}

}
