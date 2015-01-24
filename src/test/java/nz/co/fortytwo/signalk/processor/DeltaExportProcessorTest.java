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

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.event.JsonEvent;
import nz.co.fortytwo.signalk.model.event.JsonEvent.EventType;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.model.impl.SignalKModelImpl;
import nz.co.fortytwo.signalk.processor.DeltaExportProcessor;
import nz.co.fortytwo.signalk.server.CamelContextFactory;
import nz.co.fortytwo.signalk.server.NettyServer;
import nz.co.fortytwo.signalk.server.RouteManager;
import nz.co.fortytwo.signalk.server.RouteManagerFactory;
import nz.co.fortytwo.signalk.server.SignalkRouteFactory;
import nz.co.fortytwo.signalk.server.Subscription;
import nz.co.fortytwo.signalk.server.SubscriptionManagerFactory;
import nz.co.fortytwo.signalk.server.util.JsonConstants;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.*;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.websocket.SignalkWebsocketComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

public class DeltaExportProcessorTest {
	private static Logger logger = Logger.getLogger(DeltaExportProcessorTest.class);

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldHandleEventsFromBus() throws Exception {

		DeltaExportProcessor processor = new DeltaExportProcessor("wsSession1");
		assertEquals(0, processor.map.size());
		EventBus bus = new EventBus();
		bus.register(processor);

		JsonEvent event = new JsonEvent(getJsonForEvent( SELF, nav_courseOverGroundTrue), EventType.EDIT);
		bus.post(event);
		assertEquals(1, processor.map.size());

	}

	@Test
	public void shouldCreateInstantDelta() throws Exception {
		CamelContext ctx = RouteManagerFactory.getInstance(null).getContext();

		SignalKModel model = SignalKModelFactory.getInstance();
		Json data = Json
				.read("{\"vessels\":{\""
						+ SELF
						+ "\":{\"navigation\":{\"courseOverGroundTrue\": {\"value\":11.9600000381},\"courseOverGroundMagnetic\": {\"value\":93.0000000000},\"headingMagnetic\": {\"value\":0.0000000000},\"magneticVariation\": {\"value\":0.0000000000},\"headingTrue\": {\"value\":0.0000000000},\"pitch\": {\"value\":0.0000000000},\"rateOfTurn\": {\"value\":0.0000000000},\"roll\": {\"value\":0.0000000000},\"speedOverGround\": {\"value\":0.0399999980},\"speedThroughWater\": {\"value\":0.0000000000},\"state\": {\"value\":\"Not defined (example)\"},\"anchor\":{\"alarmRadius\": {\"value\":0.0000000000},\"maxRadius\": {\"value\":0.0000000000},\"position\":{\"latitude\": {\"value\":-41.2936935424},\"longitude\": {\"value\":173.2470855712},\"altitude\": {\"value\":0.0000000000}}},\"position\":{\"latitude\": {\"value\":-41.2936935424},\"longitude\": {\"value\":173.2470855712},\"altitude\": {\"value\":0.0000000000}}},\"alarm\":{\"anchorAlarmMethod\": {\"value\":\"sound\"},\"anchorAlarmState\": {\"value\":\"disabled\"},\"autopilotAlarmMethod\": {\"value\":\"sound\"},\"autopilotAlarmState\": {\"value\":\"disabled\"},\"engineAlarmMethod\": {\"value\":\"sound\"},\"engineAlarmState\": {\"value\":\"disabled\"},\"fireAlarmMethod\": {\"value\":\"sound\"},\"fireAlarmState\": {\"value\":\"disabled\"},\"gasAlarmMethod\": {\"value\":\"sound\"},\"gasAlarmState\": {\"value\":\"disabled\"},\"gpsAlarmMethod\": {\"value\":\"sound\"},\"gpsAlarmState\": {\"value\":\"disabled\"},\"maydayAlarmMethod\": {\"value\":\"sound\"},\"maydayAlarmState\": {\"value\":\"disabled\"},\"panpanAlarmMethod\": {\"value\":\"sound\"},\"panpanAlarmState\": {\"value\":\"disabled\"},\"powerAlarmMethod\": {\"value\":\"sound\"},\"powerAlarmState\": {\"value\":\"disabled\"},\"silentInterval\": {\"value\":300},\"windAlarmMethod\": {\"value\":\"sound\"},\"windAlarmState\": {\"value\":\"disabled\"},\"genericAlarmMethod\": {\"value\":\"sound\"},\"genericAlarmState\": {\"value\":\"disabled\"},\"radarAlarmMethod\": {\"value\":\"sound\"},\"radarAlarmState\": {\"value\":\"disabled\"},\"mobAlarmMethod\": {\"value\":\"sound\"},\"mobAlarmState\": {\"value\":\"disabled\"}},\"steering\":{\"rudderAngle\": {\"value\":0.0000000000},\"rudderAngleTarget\": {\"value\":0.0000000000},\"autopilot\":{\"state\": {\"value\":\"off\"},\"mode\": {\"value\":\"powersave\"},\"targetHeadingNorth\": {\"value\":0.0000000000},\"targetHeadingMagnetic\": {\"value\":0.0000000000},\"alarmHeadingXte\": {\"value\":0.0000000000},\"headingSource\": {\"value\":\"compass\"},\"deadZone\": {\"value\":0.0000000000},\"backlash\": {\"value\":0.0000000000},\"gain\": {\"value\":0},\"maxDriveAmps\": {\"value\":0.0000000000},\"maxDriveRate\": {\"value\":0.0000000000},\"portLock\": {\"value\":0.0000000000},\"starboardLock\": {\"value\":0.0000000000}}},\"environment\":{\"airPressureChangeRateAlarm\": {\"value\":0.0000000000},\"airPressure\": {\"value\":1024.0000000000},\"waterTemp\": {\"value\":0.0000000000},\"wind\":{\"speedAlarm\": {\"value\":0.0000000000},\"directionChangeAlarm\": {\"value\":0.0000000000},\"angleApparent\": {\"value\":0.0000000000},\"directionTrue\": {\"value\":0.0000000000},\"speedApparent\": {\"value\":0.0000000000},\"speedTrue\": {\"value\":7.68}}}}}}");
		model.merge(data);

		data = Json
				.read("{\"vessels\":{\""
						+ "other"
						+ "\":{\"navigation\":{\"courseOverGroundTrue\": {\"value\":11.9600000381},\"courseOverGroundMagnetic\": {\"value\":93.0000000000},\"headingMagnetic\": {\"value\":0.0000000000},\"magneticVariation\": {\"value\":0.0000000000},\"headingTrue\": {\"value\":0.0000000000},\"pitch\": {\"value\":0.0000000000},\"rateOfTurn\": {\"value\":0.0000000000},\"roll\": {\"value\":0.0000000000},\"speedOverGround\": {\"value\":0.0399999980},\"speedThroughWater\": {\"value\":0.0000000000},\"state\": {\"value\":\"Not defined (example)\"},\"anchor\":{\"alarmRadius\": {\"value\":0.0000000000},\"maxRadius\": {\"value\":0.0000000000},\"position\":{\"latitude\": {\"value\":-41.2936935424},\"longitude\": {\"value\":173.2470855712},\"altitude\": {\"value\":0.0000000000}}},\"position\":{\"latitude\": {\"value\":-41.2936935424},\"longitude\": {\"value\":173.2470855712},\"altitude\": {\"value\":0.0000000000}}},\"alarm\":{\"anchorAlarmMethod\": {\"value\":\"sound\"},\"anchorAlarmState\": {\"value\":\"disabled\"},\"autopilotAlarmMethod\": {\"value\":\"sound\"},\"autopilotAlarmState\": {\"value\":\"disabled\"},\"engineAlarmMethod\": {\"value\":\"sound\"},\"engineAlarmState\": {\"value\":\"disabled\"},\"fireAlarmMethod\": {\"value\":\"sound\"},\"fireAlarmState\": {\"value\":\"disabled\"},\"gasAlarmMethod\": {\"value\":\"sound\"},\"gasAlarmState\": {\"value\":\"disabled\"},\"gpsAlarmMethod\": {\"value\":\"sound\"},\"gpsAlarmState\": {\"value\":\"disabled\"},\"maydayAlarmMethod\": {\"value\":\"sound\"},\"maydayAlarmState\": {\"value\":\"disabled\"},\"panpanAlarmMethod\": {\"value\":\"sound\"},\"panpanAlarmState\": {\"value\":\"disabled\"},\"powerAlarmMethod\": {\"value\":\"sound\"},\"powerAlarmState\": {\"value\":\"disabled\"},\"silentInterval\": {\"value\":300},\"windAlarmMethod\": {\"value\":\"sound\"},\"windAlarmState\": {\"value\":\"disabled\"},\"genericAlarmMethod\": {\"value\":\"sound\"},\"genericAlarmState\": {\"value\":\"disabled\"},\"radarAlarmMethod\": {\"value\":\"sound\"},\"radarAlarmState\": {\"value\":\"disabled\"},\"mobAlarmMethod\": {\"value\":\"sound\"},\"mobAlarmState\": {\"value\":\"disabled\"}},\"steering\":{\"rudderAngle\": {\"value\":0.0000000000},\"rudderAngleTarget\": {\"value\":0.0000000000},\"autopilot\":{\"state\": {\"value\":\"off\"},\"mode\": {\"value\":\"powersave\"},\"targetHeadingNorth\": {\"value\":0.0000000000},\"targetHeadingMagnetic\": {\"value\":0.0000000000},\"alarmHeadingXte\": {\"value\":0.0000000000},\"headingSource\": {\"value\":\"compass\"},\"deadZone\": {\"value\":0.0000000000},\"backlash\": {\"value\":0.0000000000},\"gain\": {\"value\":0},\"maxDriveAmps\": {\"value\":0.0000000000},\"maxDriveRate\": {\"value\":0.0000000000},\"portLock\": {\"value\":0.0000000000},\"starboardLock\": {\"value\":0.0000000000}}},\"environment\":{\"airPressureChangeRateAlarm\": {\"value\":0.0000000000},\"airPressure\": {\"value\":1024.0000000000},\"waterTemp\": {\"value\":0.0000000000},\"wind\":{\"speedAlarm\": {\"value\":0.0000000000},\"directionChangeAlarm\": {\"value\":0.0000000000},\"angleApparent\": {\"value\":0.0000000000},\"directionTrue\": {\"value\":0.0000000000},\"speedApparent\": {\"value\":0.0000000000},\"speedTrue\": {\"value\":7.68}}}}}}");
		model.merge(data);
		
		testScenario(UUID.randomUUID().toString(), "vessels.self.navigation", JsonConstants.FORMAT_DELTA, JsonConstants.POLICY_IDEAL, 1, 0, 0, getJsonForEvent( SELF, nav_courseOverGroundTrue));
		testScenario(UUID.randomUUID().toString(), "vessels.self.navigation", JsonConstants.FORMAT_DELTA, JsonConstants.POLICY_INSTANT, 1, 0, 0, getJsonForEvent( SELF, nav_courseOverGroundTrue));
		
		testScenario(UUID.randomUUID().toString(), "vessels.self.navigation", JsonConstants.FORMAT_DELTA, JsonConstants.POLICY_FIXED, 0, 0, 0, getJsonForEvent( SELF, nav_courseOverGroundTrue));
		
		testScenario(UUID.randomUUID().toString(), "vessels.self.invalid", JsonConstants.FORMAT_DELTA, JsonConstants.POLICY_IDEAL, 0, 0, 0, getJsonForEvent( SELF, nav_courseOverGroundTrue));
		testScenario(UUID.randomUUID().toString(), "vessels.self.environment", JsonConstants.FORMAT_DELTA, JsonConstants.POLICY_IDEAL, 1, 0, 0, getJsonForEvent( SELF, env_wind_angleApparent));
		
		testScenario(UUID.randomUUID().toString(), "vessels.*.environment", JsonConstants.FORMAT_DELTA, JsonConstants.POLICY_IDEAL, 1, 0, 0, getJsonForEvent( SELF, env_wind_angleApparent));
		testScenario(UUID.randomUUID().toString(), "vessels.*.environment", JsonConstants.FORMAT_DELTA, JsonConstants.POLICY_IDEAL, 1, 0, 0, getJsonForEvent( "other", env_wind_angleApparent));
		testScenario(UUID.randomUUID().toString(), "vessels.other.environment", JsonConstants.FORMAT_DELTA, JsonConstants.POLICY_IDEAL, 1, 0, 0, getJsonForEvent( "other", env_wind_angleApparent));
		testScenario(UUID.randomUUID().toString(), "vessels.other.environment", JsonConstants.FORMAT_DELTA, JsonConstants.POLICY_IDEAL, 0, 0, 0, getJsonForEvent( SELF, env_wind_angleApparent));
		Json event = Json.array();
		event.add(getJsonForEvent(SELF, nav_courseOverGroundTrue));
		event.add(getJsonForEvent("other", env_wind_angleApparent));
		testScenario(UUID.randomUUID().toString(), "vessels.*.environment", JsonConstants.FORMAT_DELTA, JsonConstants.POLICY_IDEAL, 1, 0, 0, event);
		testScenario(UUID.randomUUID().toString(), "vessels.*", JsonConstants.FORMAT_DELTA, JsonConstants.POLICY_IDEAL, 2, 0, 0, event);
		
	}
	

	private Json getJsonForEvent( String mmsi, String ref) {
		SignalKModel model = SignalKModelFactory.getInstance();
		
		return model.atPath(VESSELS, mmsi, ref);
	}
	

	private void testScenario(String session, String subKey, String format, String policy, int rcvdCounter, int mapSizeBefore, int mapSizeAfter, Json jsonEvent) throws Exception {
			
		CamelContext ctx = RouteManagerFactory.getInstance(null).getContext();
			
			Subscription sub = new Subscription(session, subKey, 10, 1000, format, policy);
			SubscriptionManagerFactory.getInstance().add("ses"+session, session);
			SubscriptionManagerFactory.getInstance().addSubscription(sub);
			
			//make a mock Endpoint
			MockEndpoint resultEndpoint = (MockEndpoint) ctx.getEndpoint("mock:resultEnd");
			 
			resultEndpoint.expectedMessageCount(rcvdCounter);
			
			DeltaExportProcessor processor = new DeltaExportProcessor(session);
			ProducerTemplate exportProducer= new DefaultProducerTemplate(CamelContextFactory.getInstance());
			exportProducer.setDefaultEndpointUri("mock:resultEnd");
			try {
				exportProducer.start();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			processor.setExportProducer(exportProducer);

			assertEquals(mapSizeBefore, processor.map.size());
			EventBus bus = new EventBus();
			bus.register(processor);

			if(jsonEvent.isArray()){
				for(Json event : jsonEvent.asJsonList()){
					bus.post(new JsonEvent(event, EventType.EDIT));
				}
			}else{
				JsonEvent event = new JsonEvent(jsonEvent, EventType.EDIT);
				bus.post(event);
			}
			resultEndpoint.assertIsSatisfied();
			assertEquals(rcvdCounter, resultEndpoint.getReceivedCounter());
			resultEndpoint.reset();
			assertEquals(mapSizeAfter, processor.map.size());
			SubscriptionManagerFactory.getInstance().removeSubscription(sub);
		}
	
}
