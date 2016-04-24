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
import static nz.co.fortytwo.signalk.util.SignalKConstants.key;
import static nz.co.fortytwo.signalk.util.SignalKConstants.name;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_position_latitude;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_position_longitude;
import static nz.co.fortytwo.signalk.util.SignalKConstants.resources_routes;
import static nz.co.fortytwo.signalk.util.SignalKConstants.routes;
import static nz.co.fortytwo.signalk.util.SignalKConstants.sourceRef;
import static nz.co.fortytwo.signalk.util.SignalKConstants.timestamp;
import static nz.co.fortytwo.signalk.util.SignalKConstants.type;
import static nz.co.fortytwo.signalk.util.SignalKConstants.value;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels_dot_self_dot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mjson.Json;
import nz.co.fortytwo.signalk.handler.JsonStorageHandler;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.Position;
import nz.co.fortytwo.signalk.util.SignalKConstants;
import nz.co.fortytwo.signalk.util.TrackSimplifier;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

/**
 * Periodic saves the current track to the signalkModel
 * 
 * @author robert
 * 
 */
public class TrackProcessor extends SignalkProcessor implements Processor {

	private static final String COORDINATES = "coordinates";
	private static final String GEOMETRY = "geometry";
	private static final String FEATURES = "features";
	private static final String GEOJSON = "{\"features\":[{\"geometry\":{\"coordinates\":[],\"type\":\"LineString\"},\"properties\":null,\"id\":\"laqz\",\"type\":\"Feature\"}],\"type\":\"FeatureCollection\"}";

	// simplify to about 2m out of true (at equator)
	private static final double TRACK_TOLERANCE = 0.00002;

	private static final int MAX_COUNT = 5000;
	private static final int SAVE_COUNT = 60;

	private static Logger logger = Logger.getLogger(TrackProcessor.class);
	
	private Json msg = Json.object();
	private Json currentTrack;
	private List<Position> track = new ArrayList<Position>();
	private Json coords;
	private Json geometry;
	private static int count = 0;

	public TrackProcessor() throws Exception {
		Json val = Json.object();
		val.set(SignalKConstants.PATH, resources_routes + dot + SignalKConstants.currentTrack);
		currentTrack = Json.object();
		val.set(value, currentTrack);
		currentTrack.set(name, "Current Track");
		currentTrack.set(type, routes);
		currentTrack.set(key, SignalKConstants.currentTrack);
		currentTrack.set("description", "Auto saved current track");
		currentTrack.set(ConfigConstants.MIME_TYPE, ConfigConstants.MIME_TYPE_JSON);

		Json values = Json.array();
		values.add(val);

		Json update = Json.object();
		update.set(timestamp, DateTime.now().toDateTimeISO().toString());
		update.set(sourceRef, VESSELS_DOT_SELF);
		update.set(SignalKConstants.values, values);

		Json updates = Json.array();
		updates.add(update);

		msg.set(SignalKConstants.CONTEXT, VESSELS_DOT_SELF);
		msg.set(SignalKConstants.PUT, updates);
		// do we have an existing one? we dont want to stomp on it
		currentTrack.set("uri", "vessels/self/resources/routes/currentTrack.geojson");
		try {
			JsonStorageHandler storageHandler = new JsonStorageHandler();
			storageHandler.handle(msg);
			// should now have any existing track, so archive it, and start
			// fresh
			archiveTrack(msg);
		} catch (IOException io) {
			// no file, or unreadable
			currentTrack.delAt("uri");
		}
		Json geoJson = Json.read(GEOJSON);
		geometry = geoJson.at(FEATURES).at(0).at(GEOMETRY);
		coords = geometry.at(COORDINATES);
		currentTrack.set(ConfigConstants.PAYLOAD, geoJson);

	}

	public void process(Exchange exchange) throws Exception {
		try {
			if (exchange.getIn().getBody() instanceof SignalKModel) {

				handle(exchange.getIn().getBody(SignalKModel.class));
			}else{
				if(logger.isDebugEnabled())logger.debug("Ignored, not a track:"+exchange.getIn().getBody(Json.class));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	// @Override
	public void handle(SignalKModel node) {
		if (node.getData().size() == 0)
			return;

		if (node.getData().containsKey(vessels_dot_self_dot + nav_position_latitude) && node.getData().containsKey(vessels_dot_self_dot + nav_position_longitude)) {
			if (logger.isTraceEnabled())
				logger.trace("TrackProcessor  updating " + node);
			// we have a track change.
			track.add(new Position((double)node.get(vessels_dot_self_dot + nav_position_latitude), (double)node.get(vessels_dot_self_dot + nav_position_longitude)));
			count++;
			// append to file
			if (count % SAVE_COUNT == 0) {
				// save it
				// if(logger.isDebugEnabled())logger.debug("Track:"+msg);
				inProducer.sendBody(msg.toString());
			}
			if (count % (SAVE_COUNT * 4) == 0) {
				if (logger.isDebugEnabled())
					logger.debug("Simplify Track, size:" + coords.asList().size());// +":"+coords);
				track = TrackSimplifier.simplify(track, TRACK_TOLERANCE);

				if (logger.isDebugEnabled())
					logger.debug("  done, size:" + coords.asList().size());
				count = track.size();
			}
			// reset?
			if (count > MAX_COUNT) {
				for(Position p: track){
					coords.add(Json.array(p.longitude(), p.latitude()));
				}
				geometry.set(COORDINATES, coords);
				archiveTrack(msg);
				count = 0;
				coords.asJsonList().clear();
				track.clear();
			}
		}
	}



	private void archiveTrack(Json message) {
		Json lastTrack = message.dup();
		String time = Util.getIsoTimeString();
		if (logger.isDebugEnabled())
			logger.debug("Archive Track to File:" + SignalKConstants.currentTrack + time);
		Json val = lastTrack.at(SignalKConstants.PUT).at(0).at(SignalKConstants.values).at(0);

		time = time.substring(0, time.indexOf("."));
		val.set(SignalKConstants.PATH, resources_routes + dot + SignalKConstants.currentTrack + time);
		val.at(value).set(name, "Track at " + time);
		currentTrack.set(key, SignalKConstants.currentTrack + time);
		inProducer.sendBody(lastTrack.toString());

	}
}
