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
package nz.co.fortytwo.signalk.processor;

import static nz.co.fortytwo.signalk.util.SignalKConstants.dot;
import static nz.co.fortytwo.signalk.util.SignalKConstants.meta;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_anchor_currentRadius;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_anchor_position_latitude;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_anchor_position_longitude;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_position_latitude;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_position_longitude;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels_dot_self;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels_dot_self_dot;
import static nz.co.fortytwo.signalk.util.SignalKConstants.zones;
import mjson.Json;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

/**
 * Processes the signalk model and updates the currentRadius of the anchor
 * 
 * @author robert
 * 
 */
public class AnchorWatchProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = LogManager.getLogger(AnchorWatchProcessor.class);
	private static String zonesKey = vessels_dot_self_dot+nav_anchor_currentRadius+dot+meta+dot+zones;
	private static String radiusKey = vessels_dot_self_dot+nav_anchor_currentRadius;
	private static String latKey = vessels_dot_self_dot+nav_position_latitude;
	private static String lonKey = vessels_dot_self_dot+nav_position_longitude;
	private static String anchorLatKey = vessels_dot_self_dot+nav_anchor_position_latitude;
	private static String anchorLonKey = vessels_dot_self_dot+nav_anchor_position_longitude;
	public void process(Exchange exchange) throws Exception {
		
		try {
			Object zonesJson = signalkModel.get(zonesKey);
			if(zonesJson !=null && zonesJson instanceof Json && ((Json)zonesJson).isArray() && ((Json)zonesJson).asJsonList().size()>0){
				//anchor watch is on
				double lat = (double) signalkModel.get(latKey);
				double lon = (double) signalkModel.get(lonKey);
				double anchorLat = (double) signalkModel.get(anchorLatKey);
				double anchorLon = (double) signalkModel.get(anchorLonKey);
				//workout distance
				double distance = Util.haversineMeters(lat,lon,anchorLat, anchorLon);
				logger.debug("Updating anchor distance:"+distance);
				signalkModel.put(radiusKey,distance,null, Util.getIsoTimeString(System.currentTimeMillis()));
			}
			
		} catch (Exception e) {
			logger.error(e);
		}
	}

}
