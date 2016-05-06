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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Track and manage the sessionId's and corresponding webSocket identifiers and subscriptions for a consumer
 *
 * @author robert
 *
 */
public class SubscriptionManager {
	
	private static Logger logger = LogManager.getLogger(SubscriptionManager.class);
	
	//hold sessionid <> wsSessionId
	BiMap<String, String> wsSessionMap = HashBiMap.create();
	Map<String, String> outPutMap = new HashMap<String, String>();
	Map<String, String> ipMap = new HashMap<String, String>();
	//wsSessionId>Subscription
	List<Subscription> subscriptions = new ArrayList<Subscription>();
	List<String> heartbeats = new ArrayList<String>();
	
	/**
	 * Add a new subscription.
	 * @param sub
	 * @throws Exception
	 */
	public void addSubscription(Subscription sub) throws Exception{
		if(!subscriptions.contains(sub)){
			if(logger.isDebugEnabled())logger.debug("Adding sub "+sub);
			subscriptions.add(sub);
			//create a new route if we have too
			if(sub.isActive() && !hasExistingRoute(sub)){
				RouteManager routeManager = RouteManagerFactory.getInstance();
				SignalkRouteFactory.configureSubscribeTimer(routeManager, sub);
				heartbeats.remove(sub.getWsSession());
			}
			if(logger.isDebugEnabled())logger.debug("Subs size ="+subscriptions.size());
		}
		
	}
	
	/**
	 * True if another subcription has the same route and is active
	 * @param sub
	 * @return
	 */
	private boolean hasExistingRoute(Subscription sub) {
		for(Subscription s: getSubscriptions(sub.getWsSession())){
			if(sub.equals(s))continue;
			if(sub.isSameRoute(s)&&s.isActive()){
				sub.setRouteId(s.getRouteId());
				return true;
			}
		};
		return false;
	}

	/**
	 * Remove a subscription
	 * 
	 * @param sub
	 * @throws Exception
	 */
	public void removeSubscription(Subscription sub) throws Exception{
			subscriptions.remove(sub);
			if(sub.isActive()&& !hasExistingRoute(sub)){
				RouteManager routeManager = RouteManagerFactory.getInstance();
				SignalkRouteFactory.removeSubscribeTimer(routeManager, sub);
			}
			//if we have no subs, then we should put a sub for empty updates as heartbeat
			if(getSubscriptions(sub.getWsSession()).size()==0){
				heartbeats.add(sub.getWsSession());
			}
	}
	
	public List<Subscription> getSubscriptions(String wsSession){
		List<Subscription> subs = new ArrayList<Subscription>();
		for (Subscription s: subscriptions){
			if(s.getWsSession().equals(wsSession)){
				subs.add(s);
			}
		}
		return subs;
	}
	
	/**
	 * Returns the wsSessionId for the sessionId if it exists
	 * Returns the sessionId if not. This allows for subscriptions to occur before wsSocket starts
	 * @param sessionId
	 * @return
	 */
	public String getWsSession(String sessionId){
		if(!wsSessionMap.containsKey(sessionId))return sessionId;
		return wsSessionMap.get(sessionId);
	}
	
	public String getSessionId(String wsSession){
		return wsSessionMap.inverse().get(wsSession);
	}
	
	/**
	 * Inserts the sessionId, wsSession pair.
	 * Swaps the wsSessionId for any any inactive sessions that have been entered with sessionId, sessionId
	 * If this is a new connection with no subs then nothing will be tx'd
	 * @param sessionId
	 * @param wsSession
	 * @param ipAddress 
	 * @param string 
	 * @throws Exception 
	 */
	public void add(String sessionId, String wsSession, String outputType, String localIpAddress, String remoteIpAddress) throws Exception{
		if(StringUtils.isBlank(wsSession) ||  StringUtils.isBlank(sessionId))return; 
		wsSessionMap.put(sessionId, wsSession);
		outPutMap.put(wsSession, outputType);
		ipMap.put(wsSession, localIpAddress+"#"+remoteIpAddress);
		logger.debug("Adding "+sessionId+"/"+wsSession+", outputType="+outputType+", localAddress:"+localIpAddress+", remoteAddress:"+remoteIpAddress);
		//now update any subscriptions for sessionId
		List<Subscription> subs = getSubscriptions(sessionId);

		for (Subscription s: subs){
			if(s.getWsSession().equals(sessionId)){
				subscriptions.remove(s);
				s.setWsSession(wsSession);
				subscriptions.add(s);
			}
			s.setActive(true);
			if(!hasExistingRoute(s)){
				RouteManager routeManager = RouteManagerFactory.getInstance();
				SignalkRouteFactory.configureSubscribeTimer(routeManager, s);
			}
			
		}
		//if we have no subs, then we should put a sub for empty updates as heartbeat
		if(getSubscriptions(wsSession).size()==0){
			heartbeats.add(wsSession);
		}
	}

	public void removeAllSessions() throws Exception{
		wsSessionMap.clear();
		
		outPutMap.clear();
		ipMap.clear();
		//remove all subscriptions
		RouteManager routeManager = RouteManagerFactory.getInstance();
		List<Subscription> subs = subscriptions;
		SignalkRouteFactory.removeSubscribeTimers(routeManager,subscriptions );
		subscriptions.clear();
		heartbeats.clear();
		
	}

	public void removeSessionId(String sessionId) throws Exception{
		String wsSession = wsSessionMap.get(sessionId);
		wsSessionMap.remove(sessionId);
		outPutMap.remove(wsSession);
		ipMap.remove(wsSession);
		//remove all subscriptions
		RouteManager routeManager = RouteManagerFactory.getInstance();
		List<Subscription> subs = getSubscriptions(wsSession);
		SignalkRouteFactory.removeSubscribeTimers(routeManager,subs );
		subscriptions.removeAll(subs);
		subscriptions.removeAll(getSubscriptions(sessionId));
		heartbeats.remove(wsSession);
		
	}
	public void removeWsSession(String wsSession) throws Exception{
		wsSessionMap.inverse().remove(wsSession);
		outPutMap.remove(wsSession);
		ipMap.remove(wsSession);
		//remove all subscriptions
		RouteManager routeManager = RouteManagerFactory.getInstance();
		List<Subscription> subs = getSubscriptions(wsSession);
		SignalkRouteFactory.removeSubscribeTimers(routeManager, subs);
		subscriptions.removeAll(subs);
		heartbeats.remove(wsSession);
	}

	/**
	 * Returns a Set of all the current sessionIds.
	 * 
	 * @return
	 */
	public Set<String> getSessionKeys() {
		return wsSessionMap.keySet();
	}
	
	public String getOutputType(String wsSession){
		return outPutMap.get(wsSession);
	}
	
	/**
	 * Return the ipAddress of the cleint on this websocket session
	 * @param wsSession
	 * @return
	 */
	public String getRemoteIpAddress(String wsSession){
		String ips = ipMap.get(wsSession);
		if(StringUtils.isBlank(ips)) return null;
		return ips.split("#")[1];
	}
	
	public String getLocalIpAddress(String wsSession){
		String ips = ipMap.get(wsSession);
		if(StringUtils.isBlank(ips)) return null;
		return ips.split("#")[0];
	}
	/**
	 * Gets a Set of all the current wsSessions
	 * @return
	 */
	public Set<String> getWsSessionKeys() {
		return wsSessionMap.inverse().keySet();
	}

	public boolean isValid(String sessionId) {
		if(wsSessionMap.containsKey(sessionId))return true;
		return false;
	}

	public List<String> getHeartbeats() {
		return heartbeats;
	}
	
}
