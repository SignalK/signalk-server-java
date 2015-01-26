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

import static nz.co.fortytwo.signalk.server.util.JsonConstants.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import mjson.Json;
import nz.co.fortytwo.signalk.processor.DeclinationProcessor;
import nz.co.fortytwo.signalk.processor.WindProcessor;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class SignalKSubscriptionOutputTest extends SignalKCamelTestSupport {
 
    static final String DIRECT_INPUT = "seda:input";
	static Logger logger = Logger.getLogger(SignalKSubscriptionOutputTest.class);

	MockEndpoint output = null;
	//@Produce(uri = RouteManager.SEDA_INPUT)
    protected ProducerTemplate template;
	
    String jsonString = "{\"vessels\":{\""
			+ SELF
			+ "\":{\"navigation\":{\"courseOverGroundTrue\": {\"value\":11.9600000381},\"courseOverGroundMagnetic\": {\"value\":93.0000000000},\"headingMagnetic\": {\"value\":0.0000000000},\"magneticVariation\": {\"value\":0.0000000000},\"headingTrue\": {\"value\":0.0000000000},\"pitch\": {\"value\":0.0000000000},\"rateOfTurn\": {\"value\":0.0000000000},\"roll\": {\"value\":0.0000000000},\"speedOverGround\": {\"value\":0.0399999980},\"speedThroughWater\": {\"value\":0.0000000000},\"state\": {\"value\":\"Not defined (example)\"},\"anchor\":{\"alarmRadius\": {\"value\":0.0000000000},\"maxRadius\": {\"value\":0.0000000000},\"position\":{\"latitude\": {\"value\":-41.2936935424},\"longitude\": {\"value\":173.2470855712},\"altitude\": {\"value\":0.0000000000}}},\"position\":{\"latitude\": {\"value\":-41.2936935424},\"longitude\": {\"value\":173.2470855712},\"altitude\": {\"value\":0.0000000000}}},\"alarm\":{\"anchorAlarmMethod\": {\"value\":\"sound\"},\"anchorAlarmState\": {\"value\":\"disabled\"},\"autopilotAlarmMethod\": {\"value\":\"sound\"},\"autopilotAlarmState\": {\"value\":\"disabled\"},\"engineAlarmMethod\": {\"value\":\"sound\"},\"engineAlarmState\": {\"value\":\"disabled\"},\"fireAlarmMethod\": {\"value\":\"sound\"},\"fireAlarmState\": {\"value\":\"disabled\"},\"gasAlarmMethod\": {\"value\":\"sound\"},\"gasAlarmState\": {\"value\":\"disabled\"},\"gpsAlarmMethod\": {\"value\":\"sound\"},\"gpsAlarmState\": {\"value\":\"disabled\"},\"maydayAlarmMethod\": {\"value\":\"sound\"},\"maydayAlarmState\": {\"value\":\"disabled\"},\"panpanAlarmMethod\": {\"value\":\"sound\"},\"panpanAlarmState\": {\"value\":\"disabled\"},\"powerAlarmMethod\": {\"value\":\"sound\"},\"powerAlarmState\": {\"value\":\"disabled\"},\"silentInterval\": {\"value\":300},\"windAlarmMethod\": {\"value\":\"sound\"},\"windAlarmState\": {\"value\":\"disabled\"},\"genericAlarmMethod\": {\"value\":\"sound\"},\"genericAlarmState\": {\"value\":\"disabled\"},\"radarAlarmMethod\": {\"value\":\"sound\"},\"radarAlarmState\": {\"value\":\"disabled\"},\"mobAlarmMethod\": {\"value\":\"sound\"},\"mobAlarmState\": {\"value\":\"disabled\"}},\"steering\":{\"rudderAngle\": {\"value\":0.0000000000},\"rudderAngleTarget\": {\"value\":0.0000000000},\"autopilot\":{\"state\": {\"value\":\"off\"},\"mode\": {\"value\":\"powersave\"},\"targetHeadingNorth\": {\"value\":0.0000000000},\"targetHeadingMagnetic\": {\"value\":0.0000000000},\"alarmHeadingXte\": {\"value\":0.0000000000},\"headingSource\": {\"value\":\"compass\"},\"deadZone\": {\"value\":0.0000000000},\"backlash\": {\"value\":0.0000000000},\"gain\": {\"value\":0},\"maxDriveAmps\": {\"value\":0.0000000000},\"maxDriveRate\": {\"value\":0.0000000000},\"portLock\": {\"value\":0.0000000000},\"starboardLock\": {\"value\":0.0000000000}}},\"environment\":{\"airPressureChangeRateAlarm\": {\"value\":0.0000000000},\"airPressure\": {\"value\":1024.0000000000},\"waterTemp\": {\"value\":0.0000000000},\"wind\":{\"speedAlarm\": {\"value\":0.0000000000},\"directionChangeAlarm\": {\"value\":0.0000000000},\"angleApparent\": {\"value\":0.0000000000},\"directionTrue\": {\"value\":0.0000000000},\"speedApparent\": {\"value\":0.0000000000},\"speedTrue\": {\"value\":7.68}}}}}}";
	@Before
	public void before() throws Exception {

		
	}
	
	public void init() throws Exception{
		
		template= new DefaultProducerTemplate(routeManager.getContext());
		template.setDefaultEndpointUri(DIRECT_INPUT);
		template.start();
	}

	
	@Test
    public void shouldOutputSubscribedMessage() throws Exception {
		init();
		String wsSession = UUID.randomUUID().toString();
        assertNotNull(template);
         output.reset();
         output.expectedMessageCount(1);
         template.sendBody(DIRECT_INPUT,jsonString);
         latch.await(2,TimeUnit.SECONDS);
		 logger.debug("SignalKModel:"+signalkModel);
		 assertEquals(11.96d,signalkModel.findValue(signalkModel.atPath(VESSELS,SELF), nav_courseOverGroundTrue).asDouble(),0.00001);
		 logger.debug("Lat :"+signalkModel.findValue(signalkModel.atPath(VESSELS,SELF), nav_position_latitude));
		 
		 //add a sub
		 Json sub = getJson("vessels." + SELF,"navigation", 500, 0,FORMAT_FULL, POLICY_FIXED);
		 template.sendBodyAndHeader(DIRECT_INPUT, sub.toString(),WebsocketConstants.CONNECTION_KEY, wsSession);
		 
		 output.assertIsSatisfied();
      
    }
	
	@Test
    public void shouldOutputPositionMessage() throws Exception {
		init();
		String wsSession = UUID.randomUUID().toString();
        assertNotNull(template);
         output.reset();
         output.expectedMessageCount(1);
         template.sendBody(DIRECT_INPUT,jsonString);
         latch.await(2,TimeUnit.SECONDS);
		 logger.debug("SignalKModel:"+signalkModel);
		 assertEquals(11.96d,signalkModel.findValue(signalkModel.atPath(VESSELS,SELF), nav_courseOverGroundTrue).asDouble(),0.00001);
		 logger.debug("Lat :"+signalkModel.findValue(signalkModel.atPath(VESSELS,SELF), nav_position_latitude));
		 
		 //add a sub
		 Json sub = getJson("vessels." + SELF,nav_position, 500, 0,FORMAT_FULL, POLICY_FIXED);
		 template.sendBodyAndHeader(DIRECT_INPUT, sub.toString(),WebsocketConstants.CONNECTION_KEY, wsSession);
		 output.assertIsSatisfied();
		 Json out = output.getReceivedExchanges().get(0).getIn().getBody(Json.class);
		 logger.debug("Received msg: "+out);
		 Json expected = Json.read("{\"vessels\":{\"motu\":{\"navigation\":{\"position\":{\"longitude\":{\"timestamp\":\"2015-01-26T16:19:21.686+13:00\",\"source\":\"unknown\",\"value\":173.2470855712},\"latitude\":{\"timestamp\":\"2015-01-26T16:19:21.782+13:00\",\"source\":\"unknown\",\"value\":-41.2936935424},\"altitude\":{\"timestamp\":\"2015-01-26T16:19:21.782+13:00\",\"source\":\"unknown\",\"value\":0.0}}}}}}");
		 assertEquals(-41.2936935424d,out.at(VESSELS).at(SELF).at(nav).at("position").at("latitude").at("value").asDouble(), 0.000d);
		 assertFalse(out.at(VESSELS).at(SELF).has(env));
    }
	@Test
    public void shouldOutputPositionWindMessage() throws Exception {
		init();
		String wsSession = UUID.randomUUID().toString();
        assertNotNull(template);
         output.reset();
         output.expectedMessageCount(1);
         template.sendBody(DIRECT_INPUT,jsonString);
         latch.await(2,TimeUnit.SECONDS);
		 logger.debug("SignalKModel:"+signalkModel);
		 assertEquals(11.96d,signalkModel.findValue(signalkModel.atPath(VESSELS,SELF), nav_courseOverGroundTrue).asDouble(),0.00001);
		 logger.debug("Lat :"+signalkModel.findValue(signalkModel.atPath(VESSELS,SELF), nav_position_latitude));
		 
		 //add a sub
		 Json sub = getJson("vessels." + SELF,nav_position, 500, 0,FORMAT_FULL, POLICY_FIXED);
		 template.sendBodyAndHeader(DIRECT_INPUT, sub.toString(),WebsocketConstants.CONNECTION_KEY, wsSession);
		 sub = getJson("vessels." + SELF,env_wind, 500, 0,FORMAT_FULL, POLICY_FIXED);
		 template.sendBodyAndHeader(DIRECT_INPUT, sub.toString(),WebsocketConstants.CONNECTION_KEY, wsSession);
		 
		 output.assertIsSatisfied();
		 Json out = output.getReceivedExchanges().get(0).getIn().getBody(Json.class);
		 logger.debug("Received msg: "+out);
		 assertEquals(-41.2936935424d,out.at(VESSELS).at(SELF).at(nav).at("position").at("latitude").at("value").asDouble(), 0.000d);
		 assertEquals(-0.0d,out.at(VESSELS).at(SELF).at(env).at("wind").at("speedApparent").at("value").asDouble(), 0.000d);
    }
	
	private Json getJson(String context, String path, int period, int minPeriod, String format, String policy) {
		Json json = Json.read("{\"context\":\""+context+"\", \"subscribe\": []}");
		Json sub = Json.object();
		sub.set("path",path);
		sub.set("period",period);
		sub.set("minPeriod",minPeriod);
		sub.set("format",format);
		sub.set("policy",policy);
		json.at("subscribe").add(sub);
		logger.debug("Created json sub: "+json);
		return json;
	}

	@Override
	public void configureRouteBuilder(RouteBuilder routeBuilder) {
		output = (MockEndpoint) routeBuilder.getContext().getEndpoint("mock:output");
		SignalkRouteFactory.configureInputRoute(routeBuilder, DIRECT_INPUT);
		routeBuilder.from(RouteManager.SEDA_COMMON_OUT).to(output);
		
	}

}
