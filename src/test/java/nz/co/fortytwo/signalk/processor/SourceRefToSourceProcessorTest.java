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

import static org.junit.Assert.assertEquals;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.junit.Test;

import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.RouteManagerFactory;

public class SourceRefToSourceProcessorTest {
	
	private static Logger logger = LogManager.getLogger(SourceRefToSourceProcessorTest.class);

	@Test
	public void shouldRemoveSourceRef() throws Exception {
		CamelContext ctx = RouteManagerFactory.getMotuTestInstance().getContext();
		SignalKModel model = SignalKModelFactory.getCleanInstance();
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.courseOverGroundMagnetic.$source","nmea.0183.VHW");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.courseOverGroundMagnetic.timestamp","2016-03-30T08:05:46.983Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.courseOverGroundMagnetic.value",5.28834763);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.courseOverGroundMagnetic.values.nmea.0183.HDM.$source","nmea.0183.HDM");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.courseOverGroundMagnetic.values.nmea.0183.HDM.timestamp","2016-03-30T08:05:38.546Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.courseOverGroundMagnetic.values.nmea.0183.HDM.value",5.28834763);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.courseOverGroundMagnetic.values.nmea.0183.VHW.$source","nmea.0183.VHW");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.courseOverGroundMagnetic.values.nmea.0183.VHW.timestamp","2016-03-30T08:05:46.983Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.courseOverGroundMagnetic.values.nmea.0183.VHW.value",5.28834763);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.position.$source","nmea.0183.RMC");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.position.altitude",0.0);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.position.latitude",37.81306667);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.position.longitude",-122.44718333);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.position.timestamp","2016-03-30T08:06:18.556Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.speedOverGround.$source","nmea.0183.RMC");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.speedOverGround.timestamp","2016-03-30T08:06:18.546Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.speedOverGround.value",1.61298375);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.speedOverGround.values.nmea.0183.RMC.$source","nmea.0183.RMC");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.speedOverGround.values.nmea.0183.RMC.timestamp","2016-03-30T08:06:18.546Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.speedOverGround.values.nmea.0183.RMC.value",1.61298375);
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.speedOverGround.values.nmea.0183.VHW.$source","nmea.0183.VHW");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.speedOverGround.values.nmea.0183.VHW.timestamp","2016-03-30T08:05:46.983Z");
		model.getFullData().put("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.speedOverGround.values.nmea.0183.VHW.value",1.1124765);
		SignalKModel signalk = SignalKModelFactory.getMotuTestInstance();
		signalk.getFullData().put("sources.nmea.0183.VHW","$IIVHW123456789*78");
		signalk.getFullData().put("sources.nmea.0183.RMC","$GPRMC123456789*78");
		
		logger.debug(model);
		
		Exchange ex = new DefaultExchange(ctx);
		ex.getIn().setBody(model);

		SourceRefToSourceProcessor processor = new SourceRefToSourceProcessor();
		processor.process(ex);
		logger.debug(ex.getIn().getBody());
		assertEquals("$IIVHW123456789*78", model.get("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.courseOverGroundMagnetic.source"));
		assertEquals("$GPRMC123456789*78", model.get("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.position.source"));
		assertEquals("$GPRMC123456789*78", model.get("vessels.urn:mrn:signalk:uuid:6b0e776f-811a-4b35-980e-b93405371bc5.navigation.speedOverGround.source"));
	}

}
