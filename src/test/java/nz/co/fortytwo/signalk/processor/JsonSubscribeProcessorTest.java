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

import static nz.co.fortytwo.signalk.util.SignalKConstants.FORMAT_DELTA;
import static nz.co.fortytwo.signalk.util.SignalKConstants.POLICY_FIXED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import mjson.Json;
import nz.co.fortytwo.signalk.server.RouteManager;
import nz.co.fortytwo.signalk.server.RouteManagerFactory;
import nz.co.fortytwo.signalk.server.Subscription;
import nz.co.fortytwo.signalk.server.SubscriptionManager;
import nz.co.fortytwo.signalk.server.SubscriptionManagerFactory;
import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.SignalKConstants;

import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.model.RouteDefinition;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JsonSubscribeProcessorTest {

	private static Logger logger = Logger.getLogger(JsonSubscribeProcessorTest.class);

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
		manager.removeSessionId(wsSession);
		JsonSubscribeProcessor subscribe = new JsonSubscribeProcessor();
		HashMap<String, Object> headers = new HashMap<String, Object>();
		headers.put(WebsocketConstants.CONNECTION_KEY, wsSession);
		subscribe.handle(getJson("vessels." + SignalKConstants.self,"navigation", 1000,0,FORMAT_DELTA, POLICY_FIXED), headers);
		List<Subscription> subs = manager.getSubscriptions(wsSession);
		assertEquals(1, subs.size());
		Subscription s = subs.get(0);
		assertEquals("Subscription [wsSession="+wsSession+", path=vessels." + SignalKConstants.self + ".navigation, period=1000, format=delta, active=true, destination=null]", s.toString());
	}

	/**
	 * <pre>
	 {
    "context": "vessels.230099999",
    "subscribe": [
        {
                    "path": "navigation.speedThroughWater",
                    "period": 1000,
                    "format": "delta",
                    "policy": "ideal",
                    "minPeriod": 200
                },
         {
                    "path": "navigation.logTrip",
                    "period": 10000
                }
        ],
     "unsubscribe": [
        {
                    "path": "environment.depth.belowTransducer",
                }
         ]
}
	 * </pre>
	 * @param string
	 * @param i
	 * @param j
	 * @param formatDelta
	 * @param policyFixed
	 * @return
	 */
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

	@Test
	public void shouldRemoveSubsWithSessionId() throws Exception {
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		String wsSession = UUID.randomUUID().toString();
		manager.removeSessionId(wsSession);
		JsonSubscribeProcessor subscribe = new JsonSubscribeProcessor();
		HashMap<String, Object> headers = new HashMap<String, Object>();
		headers.put(WebsocketConstants.CONNECTION_KEY, wsSession);
		subscribe.handle(getJson("vessels." + SignalKConstants.self,"navigation", 500, 0,FORMAT_DELTA, POLICY_FIXED),headers);
		List<Subscription> subs = manager.getSubscriptions(wsSession);
		assertEquals(1, subs.size());
		Subscription s = subs.get(0);
		assertEquals("Subscription [wsSession="+wsSession+", path=vessels." + SignalKConstants.self + ".navigation, period=500, format=delta, active=true, destination=null]", s.toString());
		manager.removeSessionId(wsSession);
		subs = manager.getSubscriptions(wsSession);
		assertEquals(0, subs.size());
	}

	@Test
	public void shouldSubscribeWithPeriod() throws Exception {
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		String wsSession = UUID.randomUUID().toString();
		manager.removeSessionId(wsSession);
		JsonSubscribeProcessor subscribe = new JsonSubscribeProcessor();
		HashMap<String, Object> headers = new HashMap<String, Object>();
		headers.put(WebsocketConstants.CONNECTION_KEY, wsSession);
		subscribe.handle(getJson("vessels." + SignalKConstants.self,"navigation", 500, 0,FORMAT_DELTA, POLICY_FIXED),headers);
		List<Subscription> subs = manager.getSubscriptions(wsSession);
		assertEquals(1, subs.size());
		Subscription s = subs.get(0);
		assertEquals("Subscription [wsSession="+wsSession+", path=vessels." + SignalKConstants.self + ".navigation, period=500, format=delta, active=true, destination=null]", s.toString());
	}

	@Test
	public void shouldSubscribeWithIp() throws Exception {
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		String wsSession = UUID.randomUUID().toString();
		manager.removeSessionId(wsSession);
		// now add webSocket
		manager.add(wsSession, wsSession,ConfigConstants.OUTPUT_WS,"10.1.1.100","10.1.1.128");
		assertEquals("10.1.1.128", manager.getRemoteIpAddress(wsSession));
		assertEquals("10.1.1.100", manager.getLocalIpAddress(wsSession));
		manager.removeSessionId(wsSession);
		assertNull( manager.getRemoteIpAddress(wsSession));
		assertNull( manager.getLocalIpAddress(wsSession));
	}
	@Test
	public void shouldUnSubscribe() throws Exception {
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		String wsSession = UUID.randomUUID().toString();
		manager.removeSessionId(wsSession);
		// now add webSocket
		manager.add(wsSession, wsSession,ConfigConstants.OUTPUT_WS,"127.0.0.1","127.0.0.1");
		JsonSubscribeProcessor subscribe = new JsonSubscribeProcessor();
		HashMap<String, Object> headers = new HashMap<String, Object>();
		headers.put(WebsocketConstants.CONNECTION_KEY, wsSession);
		subscribe.handle(getJson("vessels." + SignalKConstants.self,"navigation", 500, 0,FORMAT_DELTA, POLICY_FIXED),headers);
		List<Subscription> subs = manager.getSubscriptions(wsSession);
		assertEquals(1, subs.size());
		Subscription s = subs.get(0);
		assertEquals("Subscription [wsSession="+wsSession+", path=vessels." + SignalKConstants.self + ".navigation, period=500, format=delta, active=true, destination=null]", s.toString());
		// see if its created a route
		RouteManager routeManager = RouteManagerFactory.getInstance();
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
	public void shouldCreateOneRoute() throws Exception {
		
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		RouteManager routeManager = RouteManagerFactory.getMotuTestInstance();
		String wsSession = UUID.randomUUID().toString();
		manager.removeSessionId(wsSession);
		int routes = routeManager.getRouteCollection().getRoutes().size();
		// now add webSocket
		manager.add(wsSession, wsSession,ConfigConstants.OUTPUT_WS,"127.0.0.1","127.0.0.1");
		JsonSubscribeProcessor subscribe = new JsonSubscribeProcessor();
		HashMap<String, Object> headers = new HashMap<String, Object>();
		headers.put(WebsocketConstants.CONNECTION_KEY, wsSession);
		subscribe.handle(getJson("vessels." + SignalKConstants.self,"navigation", 500,0,FORMAT_DELTA, POLICY_FIXED), headers);
		subscribe.handle(getJson("vessels." + SignalKConstants.self,"environment", 500, 0,FORMAT_DELTA, POLICY_FIXED),headers);
		List<Subscription> subs = manager.getSubscriptions(wsSession);
		assertEquals(2, subs.size());
		// see if its created a route
		
		assertEquals(routes+1, routeManager.getRouteCollection().getRoutes().size());
		manager.removeWsSession(wsSession);
	}
	
	@Test
	public void shouldCreateTwoRoutes() throws Exception {
		
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		RouteManager routeManager = RouteManagerFactory.getMotuTestInstance();
		String wsSession = UUID.randomUUID().toString();
		manager.removeSessionId(wsSession);
		int routes = routeManager.getRouteCollection().getRoutes().size();
		// now add webSocket
		manager.add(wsSession, wsSession,ConfigConstants.OUTPUT_WS,"127.0.0.1","127.0.0.1");
		JsonSubscribeProcessor subscribe = new JsonSubscribeProcessor();
		HashMap<String, Object> headers = new HashMap<String, Object>();
		headers.put(WebsocketConstants.CONNECTION_KEY, wsSession);
		subscribe.handle(getJson("vessels." + SignalKConstants.self,"navigation", 500,0,FORMAT_DELTA, POLICY_FIXED), headers);
		subscribe.handle(getJson("vessels." + SignalKConstants.self,"environment", 1500, 0,FORMAT_DELTA, POLICY_FIXED),headers);
		List<Subscription> subs = manager.getSubscriptions(wsSession);
		assertEquals(2, subs.size());
		// see if its created a route
		
		assertEquals(routes+2, routeManager.getRouteCollection().getRoutes().size());
		manager.removeWsSession(wsSession);
	}
	
	@Test
	public void shouldUnSubscribeOne() throws Exception {
		
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		String wsSession = UUID.randomUUID().toString();
		manager.removeSessionId(wsSession);
		// now add webSocket
		manager.add(wsSession, wsSession,ConfigConstants.OUTPUT_WS,"127.0.0.1","127.0.0.1");
		JsonSubscribeProcessor subscribe = new JsonSubscribeProcessor();
		HashMap<String, Object> headers = new HashMap<String, Object>();
		headers.put(WebsocketConstants.CONNECTION_KEY, wsSession);
		subscribe.handle(getJson("vessels." + SignalKConstants.self,"navigation", 500,0,FORMAT_DELTA, POLICY_FIXED), headers);
		subscribe.handle(getJson("vessels." + SignalKConstants.self,"environment", 1500, 0,FORMAT_DELTA, POLICY_FIXED),headers);
		List<Subscription> subs = manager.getSubscriptions(wsSession);
		assertEquals(2, subs.size());
		// see if its created a route
		RouteManager routeManager = RouteManagerFactory.getMotuTestInstance();
		assertEquals(2, routeManager.getRouteCollection().getRoutes().size());
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
		RouteManager routeManager = RouteManagerFactory.getMotuTestInstance();
		int routes = routeManager.getRouteCollection().getRoutes().size();
		String wsSession = UUID.randomUUID().toString();
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		manager.removeSessionId(wsSession);
		// now add webSocket
		manager.add(wsSession, wsSession,ConfigConstants.OUTPUT_WS,"127.0.0.1","127.0.0.1");
		JsonSubscribeProcessor subscribe = new JsonSubscribeProcessor();
		HashMap<String, Object> headers = new HashMap<String, Object>();
		headers.put(WebsocketConstants.CONNECTION_KEY, wsSession);
		subscribe.handle(getJson("vessels." + SignalKConstants.self,"navigation", 500, 0,FORMAT_DELTA, POLICY_FIXED),headers);
		subscribe.handle(getJson("vessels." + SignalKConstants.self, "environment", 1000, 0,FORMAT_DELTA, POLICY_FIXED),headers);
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
		RouteManager routeManager = RouteManagerFactory.getMotuTestInstance();
		int routes = routeManager.getRouteCollection().getRoutes().size();
		String wsSession = UUID.randomUUID().toString();
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		manager.removeSessionId(wsSession);
		// now add webSocket
		manager.add(wsSession, wsSession,ConfigConstants.OUTPUT_WS,"127.0.0.1","127.0.0.1");
		JsonSubscribeProcessor subscribe = new JsonSubscribeProcessor();
		HashMap<String, Object> headers = new HashMap<String, Object>();
		headers.put(WebsocketConstants.CONNECTION_KEY, wsSession);
		subscribe.handle(getJson("vessels." + SignalKConstants.self,"navigation", 500, 0,FORMAT_DELTA, POLICY_FIXED),headers);
		subscribe.handle(getJson("vessels." + SignalKConstants.self,"environment", 500, 0,FORMAT_DELTA, POLICY_FIXED),headers);
		List<Subscription> subs = manager.getSubscriptions(wsSession);
		assertEquals(2, subs.size());
		// see if its created a route
		assertEquals(routes + 1, routeManager.getRouteCollection().getRoutes().size());

		for (RouteDefinition route : routeManager.getRouteCollection().getRoutes()) {
			logger.debug("Checking route " + route.getId());
			// assertEquals("subscribe:wsSession7:vessels/motu/navigation", route.getId());
		}
		manager.removeSessionId(wsSession);
		subs = manager.getSubscriptions(wsSession);
		assertEquals(0, subs.size());

		// assertEquals(routes,routeManager.getRouteCollection().getRoutes().size());
	}

	@Test
	@Ignore
	public void shouldFailOnBadPath1() throws Exception {
		SubscriptionManager manager = SubscriptionManagerFactory.getInstance();
		String wsSession = UUID.randomUUID().toString();
		manager.removeSessionId(wsSession);
		// now add webSocket
		manager.add(wsSession, wsSession,ConfigConstants.OUTPUT_WS,"127.0.0.1","127.0.0.1");
		
		JsonSubscribeProcessor subscribe = new JsonSubscribeProcessor();
		HashMap<String, Object> headers = new HashMap<String, Object>();
		headers.put(WebsocketConstants.CONNECTION_KEY, wsSession);
		Json sub = getJson("vess." + SignalKConstants.self,"navigation",1000,0,FORMAT_DELTA, POLICY_FIXED);
		Json json = subscribe.handle(sub, headers);
		assertEquals(sub, json);
		List<Subscription> subs = manager.getSubscriptions(wsSession);
		assertEquals(0, subs.size());
	}

}
