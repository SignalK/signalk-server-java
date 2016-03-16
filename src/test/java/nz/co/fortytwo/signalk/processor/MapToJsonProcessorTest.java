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
package nz.co.fortytwo.signalk.processor;

import static org.junit.Assert.*;

import java.util.NavigableMap;

import mjson.Json;
import nz.co.fortytwo.signalk.handler.FullToMapConverter;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.SignalKCamelTestSupport;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.log4j.Logger;
import org.junit.Test;

public class MapToJsonProcessorTest extends SignalKCamelTestSupport{

	private static Logger logger = Logger.getLogger(MapToJsonProcessorTest.class);
	
	@Test
	public void test() throws Exception {
		SignalKModel model = SignalKModelFactory.getCleanInstance();

		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundMagnetic.pgn",21.406661494994307);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundMagnetic.sentence","ipsum");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundMagnetic.source.label","testLabel");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundMagnetic.source.type","testType");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundMagnetic.timestamp","2016-03-14T08:51:56.744Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundMagnetic.value",24.99888661232309);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundMagnetic.values.b5d1104.pgn",10.98299199970798);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundMagnetic.values.b5d1104.sentence","ipsum");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundMagnetic.values.b5d1104.timestamp","2016-03-14T08:51:56.744Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundMagnetic.values.b5d1104.value",20.523181765927777);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundTrue.pgn",47.961135869778914);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundTrue.sentence","ipsum");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundTrue.source.label","testLabel");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundTrue.source.type","testType");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundTrue.timestamp","2016-03-14T08:51:56.744Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundTrue.value",21.577254828573665);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundTrue.values.c116f44.pgn",49.98039875805297);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundTrue.values.c116f44.sentence","ipsum");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundTrue.values.c116f44.timestamp","2016-03-14T08:51:56.744Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.courseOverGroundTrue.values.c116f44.value",29.234137464073527);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingMagnetic.pgn",65.95645592408168);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingMagnetic.sentence","ipsum");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingMagnetic.source.label","testLabel");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingMagnetic.source.type","testType");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingMagnetic.timestamp","2016-03-14T08:51:56.744Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingMagnetic.value",13.918538822712634);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingMagnetic.values.ec87399.pgn",0.22110212431679654);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingMagnetic.values.ec87399.sentence","ipsum");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingMagnetic.values.ec87399.timestamp","2016-03-14T08:51:56.744Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingMagnetic.values.ec87399.value",78.7534528174313);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingTrue.pgn",50.846242235996556);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingTrue.sentence","ipsum");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingTrue.source.label","testLabel");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingTrue.source.type","testType");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingTrue.timestamp","2016-03-14T08:51:56.744Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingTrue.value",99.96161913730467);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingTrue.values.b8defda.pgn",38.61428260398605);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingTrue.values.b8defda.sentence","ipsum");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingTrue.values.b8defda.timestamp","2016-03-14T08:51:56.744Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.headingTrue.values.b8defda.value",64.08349014224515);
		
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.position.altitude",0.0);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.position.latitude",77.65327885982339);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.position.longitude",96.98441049156695);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.position.pgn",81.58427978544627);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.position.sentence","ipsum");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.position.source.label","testLabel");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.position.source.type","testType");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.position.timestamp","2016-03-14T08:51:56.744Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedOverGround.pgn",86.45359814901583);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedOverGround.sentence","ipsum");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedOverGround.source.label","testLabel");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedOverGround.source.type","testType");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedOverGround.timestamp","2016-03-14T08:51:56.744Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedOverGround.value",57.448476349323705);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedOverGround.values.c327297.pgn",90.60234199140433);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedOverGround.values.c327297.sentence","ipsum");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedOverGround.values.c327297.timestamp","2016-03-14T08:51:56.744Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedOverGround.values.c327297.value",46.77694243864312);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedThroughWater.pgn",56.62465022638501);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedThroughWater.sentence","ipsum");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedThroughWater.source.label","testLabel");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedThroughWater.source.type","testType");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedThroughWater.timestamp","2016-03-14T08:51:57.727Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedThroughWater.value",74.59723282465691);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedThroughWater.values.d0267df.pgn",49.711687770743886);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedThroughWater.values.d0267df.sentence","ipsum");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedThroughWater.values.d0267df.timestamp","2016-03-14T08:51:57.727Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedThroughWater.values.d0267df.value",41.95402030596676);
		
		MapToJsonProcessor processor = new MapToJsonProcessor();
		Exchange exchange = createExchangeWithBody(model);
		processor.process(exchange);
		logger.debug(exchange.getIn().getBody());
		Json json = exchange.getIn().getBody(Json.class);
		assertEquals(74.59723282465691d, Util.findNode(json, "vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedThroughWater.value").asDouble(),0.000001d);
		assertEquals(77.65327885982339d, Util.findNode(json, "vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.position.latitude").asDouble(),0.000001d);
		assertEquals(96.98441049156695d, Util.findNode(json, "vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.position.longitude").asDouble(),0.000001d);
		
		//lets convert back
		FullToMapConverter fullToMap = new FullToMapConverter();
		SignalKModel modelOut = fullToMap.handle(json);
		logger.debug(modelOut);
		//now do we have the same keys
		assertEquals(77.65327885982339d,(double) modelOut.get("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.position.latitude"),0.00001d);
		//do we have values?
		NavigableMap<String, Object> values = modelOut.getValues("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedThroughWater");
		logger.debug(values);
		assertEquals(41.95402030596676d,(double) values.get("vessels.urn:mrn:signalk:uuid:9119b97a-19ee-4f45-a27f-a9a99ce0d0c2.navigation.speedThroughWater.values.d0267df.value"),0.00001d);
	}

	@Override
	public void configureRouteBuilder(RouteBuilder routeBuilder) {
		// TODO Auto-generated method stub
		
	}

}
