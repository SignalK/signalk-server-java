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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import mjson.Json;
import nz.co.fortytwo.signalk.processor.DeclinationProcessor;
import nz.co.fortytwo.signalk.processor.WindProcessor;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class SignalKListOutputTest extends SignalKCamelTestSupport {
 
    static final String DIRECT_INPUT = "seda:input";
	static Logger logger = Logger.getLogger(SignalKListOutputTest.class);

	MockEndpoint output = null;
	//@Produce(uri = RouteManager.SEDA_INPUT)
    protected ProducerTemplate template;
	
    String jsonString = "{\"vessels\":{\""
			+ SELF
			+ "\":{\"navigation\":{\"courseOverGroundTrue\": {\"value\":11.9600000381},\"courseOverGroundMagnetic\": {\"value\":93.0000000000},\"headingMagnetic\": {\"value\":0.0000000000},\"magneticVariation\": {\"value\":0.0000000000},\"headingTrue\": {\"value\":0.0000000000},\"pitch\": {\"value\":0.0000000000},\"rateOfTurn\": {\"value\":0.0000000000},\"roll\": {\"value\":0.0000000000},\"speedOverGround\": {\"value\":0.0399999980},\"speedThroughWater\": {\"value\":0.0000000000},\"state\": {\"value\":\"Not defined (example)\"},\"anchor\":{\"alarmRadius\": {\"value\":0.0000000000},\"maxRadius\": {\"value\":0.0000000000},\"position\":{\"latitude\": -41.2936935424,\"longitude\": 173.247085571,\"altitude\": 0.0000000000}},\"position\":{\"latitude\":-41.2936935424,\"longitude\": 173.2470855712,\"altitude\": 0.000000000}},\"alarm\":{\"anchorAlarmMethod\": {\"value\":\"sound\"},\"anchorAlarmState\": {\"value\":\"disabled\"},\"autopilotAlarmMethod\": {\"value\":\"sound\"},\"autopilotAlarmState\": {\"value\":\"disabled\"},\"engineAlarmMethod\": {\"value\":\"sound\"},\"engineAlarmState\": {\"value\":\"disabled\"},\"fireAlarmMethod\": {\"value\":\"sound\"},\"fireAlarmState\": {\"value\":\"disabled\"},\"gasAlarmMethod\": {\"value\":\"sound\"},\"gasAlarmState\": {\"value\":\"disabled\"},\"gpsAlarmMethod\": {\"value\":\"sound\"},\"gpsAlarmState\": {\"value\":\"disabled\"},\"maydayAlarmMethod\": {\"value\":\"sound\"},\"maydayAlarmState\": {\"value\":\"disabled\"},\"panpanAlarmMethod\": {\"value\":\"sound\"},\"panpanAlarmState\": {\"value\":\"disabled\"},\"powerAlarmMethod\": {\"value\":\"sound\"},\"powerAlarmState\": {\"value\":\"disabled\"},\"silentInterval\": {\"value\":300},\"windAlarmMethod\": {\"value\":\"sound\"},\"windAlarmState\": {\"value\":\"disabled\"},\"genericAlarmMethod\": {\"value\":\"sound\"},\"genericAlarmState\": {\"value\":\"disabled\"},\"radarAlarmMethod\": {\"value\":\"sound\"},\"radarAlarmState\": {\"value\":\"disabled\"},\"mobAlarmMethod\": {\"value\":\"sound\"},\"mobAlarmState\": {\"value\":\"disabled\"}},\"steering\":{\"rudderAngle\": {\"value\":0.0000000000},\"rudderAngleTarget\": {\"value\":0.0000000000},\"autopilot\":{\"state\": {\"value\":\"off\"},\"mode\": {\"value\":\"powersave\"},\"targetHeadingNorth\": {\"value\":0.0000000000},\"targetHeadingMagnetic\": {\"value\":0.0000000000},\"alarmHeadingXte\": {\"value\":0.0000000000},\"headingSource\": {\"value\":\"compass\"},\"deadZone\": {\"value\":0.0000000000},\"backlash\": {\"value\":0.0000000000},\"gain\": {\"value\":0},\"maxDriveAmps\": {\"value\":0.0000000000},\"maxDriveRate\": {\"value\":0.0000000000},\"portLock\": {\"value\":0.0000000000},\"starboardLock\": {\"value\":0.0000000000}}},\"environment\":{\"airPressureChangeRateAlarm\": {\"value\":0.0000000000},\"airPressure\": {\"value\":1024.0000000000},\"waterTemp\": {\"value\":0.0000000000},\"wind\":{\"speedAlarm\": {\"value\":0.0000000000},\"directionChangeAlarm\": {\"value\":0.0000000000},\"angleApparent\": {\"value\":0.0000000000},\"directionTrue\": {\"value\":0.0000000000},\"speedApparent\": {\"value\":0.0000000000},\"speedTrue\": {\"value\":7.68}}}}}}";
	@Before
	public void before() throws Exception {

		
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
		 assertTrue(reply.at("pathlist").asList().size()==4);
		 
    }
	

	@Override
	public void configureRouteBuilder(RouteBuilder routeBuilder) {
		output = (MockEndpoint) routeBuilder.getContext().getEndpoint("mock:output");
		SignalkRouteFactory.configureInputRoute(routeBuilder, DIRECT_INPUT);
		routeBuilder.from(RouteManager.SEDA_COMMON_OUT).to(output);
		
	}

}
