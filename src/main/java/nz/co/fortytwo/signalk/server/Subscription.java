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

/**
 * Holds subscription data, wsSessionId, path, period
 * If a subscription is made before the websocket is started then the wsSocket will hold the sessionId.
 * This must be swapped for the wsSessionId when the websocket starts.
 * The subscription will be in an inactive state when it holds a sessionId
 * @author robert
 *
 */
public class Subscription {
	String wsSession = null;
	String path = null;
	long period = -1;
	boolean active = true;

	public Subscription(String wsSession, String path, long period){
		this.wsSession=wsSession;
		path=path.replace('/', '.');
		if(path.startsWith("."))path = path.substring(1);
		this.path=path;
		this.period=period;
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
		path=path.replace('/', '.');
		if(path.startsWith("."))path = path.substring(1);
		this.path = path;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}
	@Override
	public String toString() {
		return "Subscription [wsSession=" + wsSession + ", path=" + path + ", period=" + period + ", active=" + active + "]";
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	

}
