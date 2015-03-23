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
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.fortytwo.signalk.processor;

import static nz.co.fortytwo.signalk.util.JsonConstants.FORMAT_DELTA;
import static nz.co.fortytwo.signalk.util.JsonConstants.POLICY_FIXED;
import static nz.co.fortytwo.signalk.util.JsonConstants.SELF;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import nz.co.fortytwo.signalk.server.RouteManager;
import nz.co.fortytwo.signalk.server.RouteManagerFactory;
import nz.co.fortytwo.signalk.server.Subscription;
import nz.co.fortytwo.signalk.server.SubscriptionManager;
import nz.co.fortytwo.signalk.server.SubscriptionManagerFactory;
import nz.co.fortytwo.signalk.util.JsonConstants;

import org.apache.camel.model.RouteDefinition;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RestSubscribeProcessorTest {

	private static Logger logger = Logger.getLogger(RestSubscribeProcessorTest.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldSubscribeWithSessionId() throws Exception {
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		String wsSession = UUID.randomUUID().toString();
		manager.removeSessionId("sess"+wsSession);
		RestSubscribeProcessor subscribe = new RestSubscribeProcessor();
		subscribe.subscribe(JsonConstants.SIGNALK_SUBSCRIBE + "/vessels/" + SELF + "/navigation", 1000,0,FORMAT_DELTA, POLICY_FIXED, "sess"+wsSession);
		List<Subscription> subs = manager.getSubscriptions("sess"+wsSession);
		assertEquals(1, subs.size());
		Subscription s = subs.get(0);
		assertEquals("Subscription [wsSession=sess"+wsSession+", path=vessels." + SELF + ".navigation, period=1000, format=delta, active=false]", s.toString());
	}

	@Test
	public void shouldRemoveSubsWithSessionId() throws Exception {
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		String wsSession = UUID.randomUUID().toString();
		manager.removeSessionId("sess"+wsSession);
		RestSubscribeProcessor subscribe = new RestSubscribeProcessor();
		subscribe.subscribe(JsonConstants.SIGNALK_SUBSCRIBE + "/vessels/" + SELF + "/navigation", 500, 0,FORMAT_DELTA, POLICY_FIXED,"sess"+wsSession);
		List<Subscription> subs = manager.getSubscriptions("sess"+wsSession);
		assertEquals(1, subs.size());
		Subscription s = subs.get(0);
		assertEquals("Subscription [wsSession=sess"+wsSession+", path=vessels." + SELF + ".navigation, period=500, format=delta, active=false]", s.toString());
		manager.removeSessionId("sess"+wsSession);
		subs = manager.getSubscriptions("sess"+wsSession);
		assertEquals(0, subs.size());
	}

	@Test
	public void shouldSubscribeWithPeriod() throws Exception {
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		String wsSession = UUID.randomUUID().toString();
		manager.removeSessionId("sess"+wsSession);
		RestSubscribeProcessor subscribe = new RestSubscribeProcessor();
		subscribe.subscribe(JsonConstants.SIGNALK_SUBSCRIBE + "/vessels/" + SELF + "/navigation", 500, 0,FORMAT_DELTA, POLICY_FIXED,"sess"+wsSession);
		List<Subscription> subs = manager.getSubscriptions("sess"+wsSession);
		assertEquals(1, subs.size());
		Subscription s = subs.get(0);
		assertEquals("Subscription [wsSession=sess"+wsSession+", path=vessels." + SELF + ".navigation, period=500, format=delta, active=false]", s.toString());
	}

	@Test
	public void shouldSubscribeWithWsSession() throws Exception {
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		String wsSession = UUID.randomUUID().toString();
		manager.removeSessionId("sess"+wsSession);
		manager.add("sess"+wsSession, wsSession);
		RestSubscribeProcessor subscribe = new RestSubscribeProcessor();
		subscribe.subscribe(JsonConstants.SIGNALK_SUBSCRIBE + "/vessels/" + SELF + "/navigation", 1000, 0,FORMAT_DELTA, POLICY_FIXED,"sess"+wsSession);
		List<Subscription> subs = manager.getSubscriptions("sess"+wsSession);
		assertEquals(0, subs.size());

		subs = manager.getSubscriptions(wsSession);
		assertEquals(1, subs.size());

		Subscription s = subs.get(0);
		assertEquals("Subscription [wsSession="+wsSession+", path=vessels." + SELF + ".navigation, period=1000, format=delta, active=true]", s.toString());
	}

	@Test
	public void shouldSubscribeAndUpdateId() throws Exception {
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		String wsSession = UUID.randomUUID().toString();
		manager.removeSessionId("sess"+wsSession);
		RestSubscribeProcessor subscribe = new RestSubscribeProcessor();
		subscribe.subscribe(JsonConstants.SIGNALK_SUBSCRIBE + "/vessels/" + SELF + "/navigation", 1000, 0,FORMAT_DELTA, POLICY_FIXED,"sess"+wsSession);
		List<Subscription> subs = manager.getSubscriptions("sess"+wsSession);
		// sub under sessionId
		assertEquals(1, subs.size());
		Subscription s = subs.get(0);
		assertEquals("Subscription [wsSession=sess"+wsSession+", path=vessels." + SELF + ".navigation, period=1000, format=delta, active=false]", s.toString());
		// now add webSocket
		manager.add("sess"+wsSession, wsSession);
		// sub under sessionId gone
		subs = manager.getSubscriptions("sess"+wsSession);
		assertEquals(0, subs.size());
		// sub now under wsSession
		subs = manager.getSubscriptions(wsSession);
		assertEquals(1, subs.size());
		s = subs.get(0);
		assertEquals("Subscription [wsSession="+wsSession+", path=vessels." + SELF + ".navigation, period=1000, format=delta, active=true]", s.toString());
	}

	@Test
	public void shouldUnSubscribe() throws Exception {
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		String wsSession = UUID.randomUUID().toString();
		
		manager.removeSessionId("sess"+wsSession);
		
		// now add webSocket
		manager.add("sess"+wsSession, wsSession);
		RestSubscribeProcessor subscribe = new RestSubscribeProcessor();
		subscribe.subscribe(JsonConstants.SIGNALK_SUBSCRIBE + "/vessels/" + SELF + "/navigation", 500, 0,FORMAT_DELTA, POLICY_FIXED,"sess"+wsSession);
		List<Subscription> subs = manager.getSubscriptions(wsSession);
		assertEquals(1, subs.size());
		Subscription s = subs.get(0);
		assertEquals("Subscription [wsSession="+wsSession+", path=vessels." + SELF + ".navigation, period=500, format=delta, active=true]", s.toString());
		// see if its created a route
		RouteManager routeManager = RouteManagerFactory.getInstance(null);
		for (RouteDefinition route : routeManager.getRouteCollection().getRoutes()) {
			logger.debug("Checking route " + route.getId());
			// assertEquals("sub_wsSession4_/vessels/motu/navigation", route.getId());
		}
		manager.removeSubscription(s);
		subs = manager.getSubscriptions(wsSession);
		assertEquals(0, subs.size());
		// assertEquals(0,routeManager.getRouteCollection().getRoutes().size());
	}

	@Test
	public void shouldUnSubscribeOne() throws Exception {
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		RouteManager routeManager = RouteManagerFactory.getInstance(null);
		String wsSession = UUID.randomUUID().toString();
		
		manager.removeSessionId("sess"+wsSession);
		int routes = routeManager.getRouteCollection().getRoutes().size();
		// now add webSocket
		manager.add("sess"+wsSession, wsSession);
		RestSubscribeProcessor subscribe = new RestSubscribeProcessor();
		subscribe.subscribe(JsonConstants.SIGNALK_SUBSCRIBE + "/vessels/" + SELF + "/navigation", 500,0,FORMAT_DELTA, POLICY_FIXED, "sess"+wsSession);
		subscribe.subscribe(JsonConstants.SIGNALK_SUBSCRIBE + "/vessels/" + SELF + "/environment", 500, 0,FORMAT_DELTA, POLICY_FIXED,"sess"+wsSession);
		List<Subscription> subs = manager.getSubscriptions(wsSession);
		assertEquals(2, subs.size());
		// see if its created a route
		
		assertEquals(routes+1, routeManager.getRouteCollection().getRoutes().size());
		Subscription s = subs.get(0);

		for (RouteDefinition route : routeManager.getRouteCollection().getRoutes()) {
			logger.debug("Checking route " + route.getId());
			// assertEquals("subscribe:wsSession5:vessels/motu/navigation", route.getId());
		}
		manager.removeSubscription(s);
		subs = manager.getSubscriptions(wsSession);
		assertEquals(1, subs.size());
		// assertEquals(1,routeManager.getRouteCollection().getRoutes().size());
	}

	@Test
	public void shouldUnSubscribeAllByWsSession() throws Exception {
		RouteManager routeManager = RouteManagerFactory.getInstance(null);
		int routes = routeManager.getRouteCollection().getRoutes().size();
		String wsSession = UUID.randomUUID().toString();
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		manager.removeSessionId("sess"+wsSession);
		// now add webSocket
		manager.add("sess"+wsSession, wsSession);
		RestSubscribeProcessor subscribe = new RestSubscribeProcessor();
		subscribe.subscribe(JsonConstants.SIGNALK_SUBSCRIBE + "/vessels/" + SELF + "/navigation", 500, 0,FORMAT_DELTA, POLICY_FIXED,"sess"+wsSession);
		subscribe.subscribe(JsonConstants.SIGNALK_SUBSCRIBE + "/vessels/" + SELF + "/environment", 1500, 0,FORMAT_DELTA, POLICY_FIXED,"sess"+wsSession);
		List<Subscription> subs = manager.getSubscriptions(wsSession);
		assertEquals(2, subs.size());
		// see if its created a route

		assertEquals(routes + 2, routeManager.getRouteCollection().getRoutes().size());

		for (RouteDefinition route : routeManager.getRouteCollection().getRoutes()) {
			logger.debug("Checking route " + route.getId());
			// assertEquals("subscribe:wsSession6:vessels/motu/navigation", route.getId());
		}
		manager.removeWsSession(wsSession);
		subs = manager.getSubscriptions(wsSession);
		assertEquals(0, subs.size());
		// assertEquals(routes,routeManager.getRouteCollection().getRoutes().size());
	}

	@Test
	public void shouldUnSubscribeAllBySessionId() throws Exception {
		RouteManager routeManager = RouteManagerFactory.getInstance(null);
		int routes = routeManager.getRouteCollection().getRoutes().size();
		String wsSession = UUID.randomUUID().toString();
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		manager.removeSessionId("sess"+wsSession);
		// now add webSocket
		manager.add("sess"+wsSession, wsSession);
		RestSubscribeProcessor subscribe = new RestSubscribeProcessor();
		subscribe.subscribe(JsonConstants.SIGNALK_SUBSCRIBE + "/vessels/" + SELF + "/navigation", 500, 0,FORMAT_DELTA, POLICY_FIXED,"sess"+wsSession);
		subscribe.subscribe(JsonConstants.SIGNALK_SUBSCRIBE + "/vessels/" + SELF + "/environment", 500, 0,FORMAT_DELTA, POLICY_FIXED,"sess"+wsSession);
		List<Subscription> subs = manager.getSubscriptions(wsSession);
		assertEquals(2, subs.size());
		// see if its created a route
		assertEquals(routes + 1, routeManager.getRouteCollection().getRoutes().size());

		for (RouteDefinition route : routeManager.getRouteCollection().getRoutes()) {
			logger.debug("Checking route " + route.getId());
			// assertEquals("subscribe:wsSession7:vessels/motu/navigation", route.getId());
		}
		manager.removeSessionId("sess"+wsSession);
		subs = manager.getSubscriptions(wsSession);
		assertEquals(0, subs.size());

		// assertEquals(routes,routeManager.getRouteCollection().getRoutes().size());
	}

	@Test
	public void shouldFailOnBadPath1() throws Exception {
		String wsSession = UUID.randomUUID().toString();
		RestSubscribeProcessor subscribe = new RestSubscribeProcessor();
		int status = subscribe.subscribe(JsonConstants.SIGNALK_SUBSCRIBE + "" + SELF + "/navigation", 1000,0,FORMAT_DELTA, POLICY_FIXED, wsSession);
		assertEquals(HttpServletResponse.SC_BAD_REQUEST, status);
	}

}
