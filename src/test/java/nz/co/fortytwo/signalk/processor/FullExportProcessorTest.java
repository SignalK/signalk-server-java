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

import static nz.co.fortytwo.signalk.util.SignalKConstants.dot;
import static nz.co.fortytwo.signalk.util.SignalKConstants.env_wind_angleApparent;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_courseOverGroundTrue;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels;
//import static nz.co.fortytwo.signalk.util.SignalKConstants.*;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels_dot_self_dot;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.NavigableSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.event.PathEvent;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.CamelContextFactory;
import nz.co.fortytwo.signalk.server.RouteManagerFactory;
import nz.co.fortytwo.signalk.server.Subscription;
import nz.co.fortytwo.signalk.server.SubscriptionManagerFactory;
import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.SignalKConstants;
import nz.co.fortytwo.signalk.util.TestHelper;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

public class FullExportProcessorTest {
	private static Logger logger = Logger.getLogger(FullExportProcessorTest.class);

	@BeforeClass
	public static void setClass() throws Exception {
		Util.getConfig();
		Util.setSelf("motu");
	}
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldPopulateTree() throws Exception {
		CamelContext ctx = RouteManagerFactory.getMotuTestInstance().getContext();
		SignalKModel model = SignalKModelFactory.getMotuTestInstance();
		model.putAll(TestHelper.getBasicModel().getFullData());
		model.putAll(TestHelper.getOtherModel().getFullData());
				
		SignalkProcessor processor = new FullExportProcessor("gsggsgs");
		SignalKModel m = SignalKModelFactory.getCleanInstance();
		Util.setSelf("motu");
		processor.populateTree(m, "vessels.self.navigation");
		logger.debug("Output SignalKModel:"+m);
		assertNull(m.get("vessels.navigation"));
		assertNotNull(m.getValue(vessels_dot_self_dot+nav_courseOverGroundTrue));
	}
	
	
	@Test
	public void shouldCreateInstantDelta() throws Exception {
		CamelContext ctx = RouteManagerFactory.getMotuTestInstance().getContext();

		SignalKModel model = SignalKModelFactory.getMotuTestInstance();
		model.putAll(TestHelper.getBasicModel().getFullData());
		model.putAll(TestHelper.getOtherModel().getFullData());
		
		testScenario(1,UUID.randomUUID().toString(), "vessels.self.navigation", SignalKConstants.FORMAT_DELTA, SignalKConstants.POLICY_IDEAL, 1, 0, 0, getJsonForEvent( SignalKConstants.self, nav_courseOverGroundTrue));
		testScenario(2,UUID.randomUUID().toString(), "vessels.self.navigation", SignalKConstants.FORMAT_DELTA, SignalKConstants.POLICY_INSTANT, 1, 0, 0, getJsonForEvent( SignalKConstants.self, nav_courseOverGroundTrue));
		
		testScenario(3,UUID.randomUUID().toString(), "vessels.self.navigation", SignalKConstants.FORMAT_DELTA, SignalKConstants.POLICY_FIXED, 0, 0, 0, getJsonForEvent( SignalKConstants.self, nav_courseOverGroundTrue));
		
		testScenario(4,UUID.randomUUID().toString(), "vessels.self.invalid", SignalKConstants.FORMAT_DELTA, SignalKConstants.POLICY_IDEAL, 0, 0, 0, getJsonForEvent( SignalKConstants.self, nav_courseOverGroundTrue));
		testScenario(5,UUID.randomUUID().toString(), "vessels.self.environment", SignalKConstants.FORMAT_DELTA, SignalKConstants.POLICY_IDEAL, 1, 0, 0, getJsonForEvent( SignalKConstants.self, env_wind_angleApparent));
		
		testScenario(6,UUID.randomUUID().toString(), "vessels.*.environment", SignalKConstants.FORMAT_DELTA, SignalKConstants.POLICY_IDEAL, 1, 0, 0, getJsonForEvent( SignalKConstants.self, env_wind_angleApparent));
		testScenario(7,UUID.randomUUID().toString(), "vessels.*.environment", SignalKConstants.FORMAT_DELTA, SignalKConstants.POLICY_IDEAL, 1, 0, 0, getJsonForEvent( "other", env_wind_angleApparent));
		
		testScenario(8,UUID.randomUUID().toString(), "vessels.other.environment", SignalKConstants.FORMAT_DELTA, SignalKConstants.POLICY_IDEAL, 1, 0, 0, getJsonForEvent( "other", env_wind_angleApparent));
		testScenario(9,UUID.randomUUID().toString(), "vessels.other.environment", SignalKConstants.FORMAT_DELTA, SignalKConstants.POLICY_IDEAL, 0, 0, 0, getJsonForEvent( SignalKConstants.self, env_wind_angleApparent));
		ConcurrentSkipListSet<String> event = new ConcurrentSkipListSet<String>(getJsonForEvent(SignalKConstants.self, nav_courseOverGroundTrue));
		event.addAll(getJsonForEvent("other", env_wind_angleApparent));
		testScenario(10,UUID.randomUUID().toString(), "vessels.*.environment", SignalKConstants.FORMAT_DELTA, SignalKConstants.POLICY_IDEAL, 1, 0, 0, event);
		testScenario(11,UUID.randomUUID().toString(), "vessels.*", SignalKConstants.FORMAT_DELTA, SignalKConstants.POLICY_IDEAL, 1, 0, 0, event);
		
		final CountDownLatch latch = new CountDownLatch(1);
		latch.await(10, TimeUnit.SECONDS);
	}
	

	private NavigableSet<String> getJsonForEvent( String mmsi, String ref) {
		SignalKModel model = SignalKModelFactory.getInstance();
		
		return model.getTree(vessels+dot+ mmsi+dot+ ref);
	}
	

	private void testScenario(int pos,String session, String subKey, String format, String policy, int rcvdCounter, int mapSizeBefore, int mapSizeAfter, NavigableSet<String> eventSet) throws Exception {
			
		CamelContext ctx = CamelContextFactory.getInstance();//RouteManagerFactory.getMotuTestInstance().getContext();
			try{
				Subscription sub = new Subscription(session, subKey, 10, 1000, format, policy);
				SubscriptionManagerFactory.getInstance().add("ses"+session, session, ConfigConstants.OUTPUT_WS, "127.0.0.1","127.0.0.1");
				SubscriptionManagerFactory.getInstance().addSubscription(sub);
				
				//make a mock Endpoint
				MockEndpoint resultEndpoint = (MockEndpoint) ctx.getEndpoint("mock:resultEnd");
				//drain queue
				resultEndpoint.await(2, TimeUnit.SECONDS);
				resultEndpoint.reset();
				resultEndpoint.expectedMessageCount(rcvdCounter);
				
				FullExportProcessor processor = new FullExportProcessor(session);
				ProducerTemplate exportProducer= new DefaultProducerTemplate(ctx);
				exportProducer.setDefaultEndpointUri("mock:resultEnd");
				try {
					exportProducer.start();
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
				processor.setExportProducer(exportProducer);
	
				assertEquals(mapSizeBefore, processor.queue.size());
				EventBus bus = new EventBus();
				bus.register(processor);
	
				for(String key : eventSet){
					bus.post(new PathEvent(key,0, nz.co.fortytwo.signalk.model.event.PathEvent.EventType.ADD));
					logger.debug("Posted path event:"+key);
				}
				
				resultEndpoint.assertIsSatisfied();
				//assertEquals(pos+":rcvdCounter != actual received counter",rcvdCounter, resultEndpoint.getReceivedCounter());
				
				resultEndpoint.reset();
				
				assertEquals(pos+":MapSizeAfter != queue size", mapSizeAfter, processor.queue.size());
				for(Exchange e: resultEndpoint.getExchanges()){
					logger.debug(e.getIn().getBody());
				}
				SubscriptionManagerFactory.getInstance().removeSubscription(sub);
			}finally{
				SubscriptionManagerFactory.getInstance().removeWsSession(session);
				//final CountDownLatch latch = new CountDownLatch(1);
				//latch.await(2, TimeUnit.SECONDS);
			}
			
		}
	
}
