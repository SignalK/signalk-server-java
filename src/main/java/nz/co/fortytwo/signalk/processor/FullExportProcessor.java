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

import static nz.co.fortytwo.signalk.server.util.JsonConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.event.JsonEvent;
import nz.co.fortytwo.signalk.model.event.PathEvent;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.CamelContextFactory;
import nz.co.fortytwo.signalk.server.RouteManager;
import nz.co.fortytwo.signalk.server.Subscription;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Queues;
import com.google.common.collect.TreeMultimap;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;

/**
 * Exports the signalkModel as a json object
 * 
 * @author robert
 * 
 */
public class FullExportProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = Logger.getLogger(FullExportProcessor.class);
	protected String wsSession = null;
	protected Queue<String> queue = Queues.newConcurrentLinkedQueue();
	private ProducerTemplate exportProducer = null;
	private MsgSender sender = new MsgSender();

	public FullExportProcessor(String wsSession) {
		super();
		this.wsSession = wsSession;
		exportProducer = new DefaultProducerTemplate(CamelContextFactory.getInstance());
		exportProducer.setDefaultEndpointUri(RouteManager.SEDA_COMMON_OUT);
		try {
			exportProducer.start();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		signalkModel.getEventBus().register(this);

	}

	public void process(Exchange exchange) throws Exception {

		try {
			if(logger.isDebugEnabled())logger.info("process  subs for " + exchange.getFromRouteId());
			// get the accumulated delta nodes.
			exchange.getIn().setBody(createTree(exchange.getFromRouteId()));
			if(isDelta(exchange.getFromRouteId())){
				exchange.getIn().setHeader(SIGNALK_FORMAT, FORMAT_DELTA);
			}else{
				exchange.getIn().setHeader(SIGNALK_FORMAT, FORMAT_FULL);
			}
			if(logger.isDebugEnabled())logger.debug("Body set to :" + exchange.getIn().getBody());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private boolean isDelta(String routeId) {
		for (Subscription sub : manager.getSubscriptions(wsSession)) {
			if (sub == null || !sub.isActive() || !routeId.equals(sub.getRouteId()))
				continue;
			if(FORMAT_DELTA.equals(sub.getFormat())) return true;
		}
		return false;
	}

	private SignalKModel createTree(String routeId) {
		// we need the routeId.
		// Multimap<String,String> vesselList = TreeMultimap.create();
		SignalKModel temp = SignalKModelFactory.getCleanInstance();
		for (Subscription sub : manager.getSubscriptions(wsSession)) {
			if (sub == null || !sub.isActive() || !routeId.equals(sub.getRouteId()))
				continue;
			for (String p : sub.getSubscribed(null)) {
				populateTree(temp, p);
			}
		}
		return temp;

	}

	/*
	 * 
	 * <pre>
	 * {
	 * "context": "vessels.motu.navigation",
	 * "updates":[
	 * {
	 * "source": {
	 * "device" : "/dev/actisense",
	 * "timestamp":"2014-08-15-16:00:00.081",
	 * "src":"115",
	 * "pgn":"128267"
	 * },
	 * "values": [
	 * { "path": "courseOverGroundTrue","value": 172.9 },
	 * { "path": "speedOverGround","value": 3.85 }
	 * ]
	 * },
	 * {
	 * "source": {
	 * "device" : "/dev/actisense",
	 * "timestamp":"2014-08-15-16:00:00.081",
	 * "src":"115",
	 * "pgn":"128267"
	 * },
	 * "values": [
	 * { "path": "courseOverGroundTrue","value": 172.9 },
	 * { "path": "speedOverGround","value": 3.85 }
	 * ]
	 * }
	 * ]
	 * 
	 * }
	 * </pre>
	 */

	public void populateTree(SignalKModel temp, String p) {
		Json node = signalkModel.findNode(p);
		if(logger.isDebugEnabled())logger.debug("Found node:" + p + " = " + node);
		if (node != null) {
			Json n = temp.addNode((Json) temp, node.up().getPath());
			if (node.isPrimitive()) {
				n.set(node.getParentKey(), node.getValue());
			} else {
				n.set(node.getParentKey(),node.getValue());
			}
		}
		
	}

	/**
	 * @param pathEvent
	 */
	@Subscribe
	public void recordEvent(PathEvent pathEvent) {
		if (pathEvent == null)
			return;
		if (pathEvent.getPath() == null)
			return;
		if (logger.isDebugEnabled())
			logger.debug(this.wsSession + " received event " + pathEvent.getPath());

		// do we care?
		for (Subscription s : manager.getSubscriptions(wsSession)) {
			if (s.isActive() && !POLICY_FIXED.equals(s.getPolicy()) && s.isSubscribed(pathEvent.getPath())) {
				if(logger.isDebugEnabled())logger.debug("Adding to send queue : "+pathEvent.getPath());
				queue.add(pathEvent.getPath());
				sender.startSender();
				break;
			}
		}

	}

	@Subscribe
	public void recordEvent(DeadEvent e) {
		logger.debug("Received dead event" + e.getSource());
	}

	public ProducerTemplate getExportProducer() {
		return exportProducer;
	}

	public void setExportProducer(ProducerTemplate exportProducer) {
		this.exportProducer = exportProducer;
	}

	class MsgSender implements Runnable {
		long lastSend = 0;

		Thread t = null;

		public void startSender() {
			if(logger.isDebugEnabled())logger.debug("Checking sender..");
			if (t != null && t.isAlive())
				return;
			if(logger.isDebugEnabled())logger.debug("Starting sender..");
			t = new Thread(this);
			t.start();
		}

		@Override
		public void run() {
			// TODO make this react to actual subs minPeriod
			while (true) {
				if (System.currentTimeMillis() - lastSend > 100) {
					// send messages
					String p = null;
					SignalKModel temp = SignalKModelFactory.getCleanInstance();
					boolean send = false;
					while ((p = queue.poll()) != null) {
						Json node = signalkModel.findNode(p);
						if(logger.isDebugEnabled())logger.debug("Found node:" + p + " = " + node);
						if (node != null) {
							Json n = temp.addNode((Json) temp, node.getPath());
							send = true;
							if (node.isPrimitive()) {
								n.set(node.getParentKey(), node.getValue());
							} else {
								//logger.debug("Object at end of path! : " + node);
								n.up().set(node.getParentKey(),node.getValue());
							}
						}
					}
					if (send) {
						exportProducer.sendBodyAndHeader(temp, WebsocketConstants.CONNECTION_KEY, wsSession);
						lastSend = System.currentTimeMillis();
					}
					break;
				} else {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
					}
				}
			}

		}

	}
}
