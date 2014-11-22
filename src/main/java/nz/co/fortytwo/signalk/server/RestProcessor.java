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
package nz.co.fortytwo.signalk.server;

import mjson.Json;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.restlet.RestletConstants;
import org.apache.log4j.Logger;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

/**
 * Processes REST requests for Signal K data
 * By the time we get here it safe to do whatever is requested
 * Its safe to return whatever is requested, its filtered later.
 * 
 * @author robert
 *
 */
public class RestProcessor extends FreeboardProcessor implements Processor{

	private static Logger logger = Logger.getLogger(RestProcessor.class);
	@Override
	public void process(Exchange exchange) throws Exception {
		// the Restlet request should be available if neeeded
        Request request = exchange.getIn().getHeader(RestletConstants.RESTLET_REQUEST, Request.class);
        if(checkAuth(request)){
	        if(Method.GET==request.getMethod()) processGet(request, exchange);
	        if(Method.PUT==request.getMethod()) processPut(request, exchange);
	        if(Method.POST==request.getMethod()) processPost(request, exchange);
	        if(Method.DELETE==request.getMethod()) processDelete(request, exchange);
        }else{
        	Response response = exchange.getIn().getHeader(RestletConstants.RESTLET_RESPONSE, Response.class);
        	response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        }
     
	}
	
	private boolean checkAuth(Request request) {
		logger.debug(request.getCookies());
		 String cookie = request.getCookies().getFirstValue("signalk.auth");
		 logger.debug("Cookie = "+cookie);
		 if("demoPass".equals(cookie))return true;
		return false;
	}

	private void processDelete(Request request, Exchange exchange) {
		// TODO Auto-generated method stub
		
	}

	private void processPost(Request request, Exchange exchange) {
		// TODO Auto-generated method stub
		
	}

	private void processPut(Request request, Exchange exchange) {
		// TODO Auto-generated method stub
		
	}

	private void processGet(Request request, Exchange exchange) {
		// use Restlet API to create the response
        Response response = exchange.getIn().getHeader(RestletConstants.RESTLET_RESPONSE, Response.class);
        String path =  request.getOriginalRef().getPath();
        logger.debug("We are processing the restlet:"+path);
        int len = request.getRootRef().getPath().length();
        //check valid request.
        if(path.length()<len){
        	response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Bad Request");
        	return;
        }
        path=path.substring(request.getRootRef().getPath().length());
        Json json = signalkModel.atPath(path.split("/"));
        if(json==null){
        	response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Not found");
        	return;
        }
        
        logger.debug("We are processing the extension:"+path);
        response.setEntity(json.toString(),MediaType.APPLICATION_JSON);
        
        // SEND RESPONSE
        exchange.getOut().setBody(response.getEntityAsText());
        response.setStatus(Status.SUCCESS_OK);
		
	}

}
