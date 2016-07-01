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

import static nz.co.fortytwo.signalk.util.SignalKConstants.LIST;

import javax.servlet.http.HttpServletResponse;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.util.JsonSerializer;
import nz.co.fortytwo.signalk.util.Util;

/**
 * Trim the path from the json for REST api calls
 * @author robert
 *
 */
public class RestPathFilterProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = LogManager.getLogger(RestPathFilterProcessor.class);
	JsonSerializer ser = new JsonSerializer();
	
	
	public void process(Exchange exchange) throws Exception {
		
		if (exchange.getIn().getBody()==null)
			return;
		if(logger.isDebugEnabled())logger.debug("Processing, class="+exchange.getIn().getBody().getClass());
		//TODO: add more filters here
		Json reply = null;
		if (exchange.getIn().getBody() instanceof String){			
			reply = Json.read(exchange.getIn().getBody(String.class));
		}else if (exchange.getIn().getBody() instanceof SignalKModel){
			SignalKModel model = (SignalKModel)exchange.getIn().getBody();
			reply = ser.writeJson(model);
		}else if(exchange.getIn().getBody() instanceof Json){
			reply = (Json)exchange.getIn().getBody();
			
		}
		//trim leading path
		String path = exchange.getIn().getHeader(Exchange.HTTP_PATH, String.class);
		//not for /list POST PUT
		if(!exchange.getIn().getHeader(Exchange.HTTP_METHOD).equals("GET") || path.startsWith("/"+LIST))
			return;
		path = sanitizePath(path);
		if(path.endsWith("*")){
			path=path.substring(0, path.lastIndexOf("."));
		}
		
		if(logger.isDebugEnabled()){
			logger.debug("Trimming:"+exchange.getIn().getHeaders());
			logger.debug("Trimming by "+path+" : "+reply);
		}
		if(StringUtils.isNotBlank(path)&& reply !=null && !reply.toString().equals("{}")){
			reply = Util.findNode(reply, path); 
			
		}
		if(reply==null){
			exchange.getIn().setBody("Bad Request");
			exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/plain");
			exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE,
					HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		//all ok
		exchange.getIn().setBody(reply.toString());
		
		
		if(logger.isDebugEnabled()){
			logger.debug("Outputting:"+exchange.getIn().getHeaders());
			logger.debug("Outputting:"+exchange.getIn());
		}
	}

}
