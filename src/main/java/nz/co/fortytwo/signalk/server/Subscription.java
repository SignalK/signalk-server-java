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
package nz.co.fortytwo.signalk.server;

import static nz.co.fortytwo.signalk.util.JsonConstants.VESSELS;
import io.netty.util.internal.ConcurrentSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import nz.co.fortytwo.signalk.model.event.PathEvent;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.processor.SignalkProcessor;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

/**
 * Holds subscription data, wsSessionId, path, period
 * If a subscription is made via REST before the websocket is started then the wsSocket will hold the sessionId.
 * This must be swapped for the wsSessionId when the websocket starts.
 * The subscription will be in an inactive state when submitted by REST if wsSession = sessionId
 * 
 * @author robert
 * 
 */
public class Subscription {
	private static Logger logger = Logger.getLogger(Subscription.class);
	String wsSession = null;
	String path = null;
	long period = -1;
	boolean active = true;
	private long minPeriod;
	private String format;
	private String policy;
	private Pattern pattern = null;
	private String vesselPath;
	Set<String> subscribedPaths = new ConcurrentSet<String>();
	private String routeId;
	private String destination;
	private String outputType;

	public Subscription(String wsSession, String path, long period, long minPeriod, String format, String policy) {
		this.wsSession = wsSession;

		this.path = SignalkProcessor.sanitizePath(path);
		pattern = SignalkProcessor.regexPath(this.path);
		this.period = period;
		this.minPeriod = minPeriod;
		this.format = format;
		this.policy = policy;
		SignalKModelFactory.getInstance().getEventBus().register(this);
		for(String p: ImmutableList.copyOf(SignalKModelFactory.getInstance().getKeys())){
			if(isSubscribed(p)){
				subscribedPaths.add(p);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + (int) (period ^ (period >>> 32));
		result = prime * result + ((wsSession == null) ? 0 : wsSession.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Subscription other = (Subscription) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (period != other.period)
			return false;
		if (wsSession == null) {
			if (other.wsSession != null)
				return false;
		} else if (!wsSession.equals(other.wsSession))
			return false;
		return true;
	}

	public String getWsSession() {
		return wsSession;
	}

	public void setWsSession(String wsSession) {
		this.wsSession = wsSession;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = SignalkProcessor.sanitizePath(path);
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	@Override
	public String toString() {
		return "Subscription [wsSession=" + wsSession + ", path=" + path + ", period=" + period + ", format=" + format + ", active=" + active + "]";
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isSameRoute(Subscription sub) {
		if (period != sub.period)
			return false;
		if (wsSession == null) {
			if (sub.wsSession != null)
				return false;
		} else if (!wsSession.equals(sub.wsSession))
			return false;
		if (format == null) {
			if (sub.format != null)
				return false;
		} else if (!format.equals(sub.format))
			return false;
		if (policy == null) {
			if (sub.policy != null)
				return false;
		} else if (!policy.equals(sub.policy))
			return false;
		return true;
	}

	public boolean isSameVessel(Subscription sub) {
		if (getVesselPath() != null && getVesselPath().equals(sub.getVesselPath()))
			return true;
		return false;
	}

	/**
	 * Gets the context path, eg vessels.motu, vessel.*, or vessels.2933??
	 *  
	 * @param path
	 * @return
	 */
	public String getVesselPath() {
		if (vesselPath == null) {
			if (!path.startsWith(VESSELS))
				return null;
			int pos = path.indexOf(".") + 1;
			// could be just 'vessels'
			if (pos < 1)
				return null;
			pos = path.indexOf(".", pos);
			// could be just one .\dot. vessels.123456789
			if (pos < 0)
				return path;
			vesselPath = path.substring(0, pos);
		}
		return vesselPath;
	}

	/**
	 * Returns true if this subscription is interested in this path
	 * 
	 * @param key
	 * @return
	 */
	public boolean isSubscribed(String key) {
		return pattern.matcher(key).find();
	}

	public long getMinPeriod() {
		return minPeriod;
	}

	public String getFormat() {
		return format;
	}

	public String getPolicy() {
		return policy;
	}

	/**
	 * Returns a list of paths that this subscription is currently providing.
	 * The list is filtered by the key if it is not null or empty in which case a full list is returned,
	 * @param key
	 * @return
	 */
	public List<String> getSubscribed(String key) {
		if(StringUtils.isBlank(key)){
			return ImmutableList.copyOf(subscribedPaths);
		}
		List<String> paths = new ArrayList<String>();
		for (String p : subscribedPaths) {
			if (p.startsWith(key)) {
				if(logger.isDebugEnabled())logger.debug("Adding path:" + p);
				paths.add(p);
			}
		}
		return paths;
	}

	/**
	 * Listens for node changes in the server and adds them if they match the subscription
	 * 
	 * @param pathEvent
	 */
	@Subscribe
	public void recordEvent(PathEvent pathEvent) {
		if (pathEvent == null)
			return;
		if (pathEvent.getPath() == null)
			return;
		if (logger.isDebugEnabled())
			logger.debug(this.hashCode() + " received event " + pathEvent.getPath());
		if(isSubscribed(pathEvent.getPath())){
			subscribedPaths.add(pathEvent.getPath());
		}
	}

	public void setRouteId(String routeId) {
		this.routeId=routeId;
	}

	public String getRouteId() {
		return routeId;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getOutputType() {
		return outputType;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	

}
