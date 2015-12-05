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

import static nz.co.fortytwo.signalk.util.JsonConstants.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.processor.DeclinationProcessor;
import nz.co.fortytwo.signalk.processor.WindProcessor;
import nz.co.fortytwo.signalk.util.JsonSerializer;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SignalKListOutputTest extends SignalKCamelTestSupport {
 
    static final String DIRECT_INPUT = "seda:input";
	static Logger logger = Logger.getLogger(SignalKListOutputTest.class);

	MockEndpoint output = null;
	//@Produce(uri = RouteManager.SEDA_INPUT)
    protected ProducerTemplate template;
	
    String jsonString = null;
    @BeforeClass
	public static void setClass() throws Exception {
		Util.getConfig();
		Util.setSelf("motu");
		
	}
    @Before
	public void before() throws Exception {
    	//generate json
    			SignalKModel temp = SignalKModelFactory.getCleanInstance();
    			//add data
    			temp.putValue(vessels_dot_self_dot+nav_courseOverGroundTrue, 11.96d);
    			temp.putValue(vessels_dot_self_dot+nav_courseOverGroundMagnetic, 93.00d);
    			temp.putValue(vessels_dot_self_dot+nav_position_latitude, -41.2936935424d);
    			temp.putValue(vessels_dot_self_dot+nav_position_longitude, 11.96d);
    			temp.putValue(vessels_dot_self_dot+nav_position_altitude, 0.0d);
    			temp.putValue(vessels_dot_self_dot+nav_position+dot+source, "test");
    			
    			jsonString = ser.write(temp);
		
	}
	
	public void init() throws Exception{
		
		template= new DefaultProducerTemplate(routeManager.getContext());
		template.setDefaultEndpointUri(DIRECT_INPUT);
		template.start();
	}

	
	@Test
    public void shouldOutputListMessage() throws Exception {
		init();
		String wsSession = UUID.randomUUID().toString();
        assertNotNull(template);
         output.reset();
         output.expectedMessageCount(1);
         template.sendBody(DIRECT_INPUT,jsonString);
         latch.await(2,TimeUnit.SECONDS);
		 logger.debug("SignalKModel:"+signalkModel);
		 assertEquals(11.96d,(double)signalkModel.getValue(vessels_dot_self_dot + nav_courseOverGroundTrue),0.00001);
		 logger.debug("Lat :"+signalkModel.getValue(vessels_dot_self_dot + nav_position_latitude));
		 
		 //request list
		 Json sub = getList("vessels." + SELF,"navigation.position.*");
		 template.sendBodyAndHeader(DIRECT_INPUT, sub.toString(),WebsocketConstants.CONNECTION_KEY, wsSession);
		 output.assertIsSatisfied();
		 Exchange exch = output.getReceivedExchanges().get(0);
		 Json reply = exch.getIn().getBody(Json.class);
		 logger.debug("Reply = "+reply);
		 assertNotNull(reply);
		 assertTrue(reply.at("pathlist").asList().size()==3);
		 
    }
	

	@Override
	public void configureRouteBuilder(RouteBuilder routeBuilder) {
		output = (MockEndpoint) routeBuilder.getContext().getEndpoint("mock:output");
		try{
			SignalkRouteFactory.configureInputRoute(routeBuilder, DIRECT_INPUT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		routeBuilder.from(RouteManager.SEDA_COMMON_OUT).to(output);
		
	}

}
