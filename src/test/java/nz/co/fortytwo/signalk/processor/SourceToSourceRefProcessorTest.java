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

import static nz.co.fortytwo.signalk.util.SignalKConstants.MSG_TYPE;
import static nz.co.fortytwo.signalk.util.SignalKConstants.dot;
import static nz.co.fortytwo.signalk.util.SignalKConstants.env_outside_pressure;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_courseOverGroundMagnetic;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_courseOverGroundTrue;
import static nz.co.fortytwo.signalk.util.SignalKConstants.source;
import static nz.co.fortytwo.signalk.util.SignalKConstants.sourceRef;
import static nz.co.fortytwo.signalk.util.SignalKConstants.sources;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels_dot_self_dot;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.model.impl.SignalKModelImpl;
import nz.co.fortytwo.signalk.server.RouteManagerFactory;
import nz.co.fortytwo.signalk.util.TestHelper;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.util.ExchangeHelper;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class SourceToSourceRefProcessorTest{
	private static Logger logger = LogManager.getLogger(SourceToSourceRefProcessorTest.class);
	@Test
	public void shouldMoveSource() throws Exception {
		CamelContext ctx = RouteManagerFactory.getMotuTestInstance().getContext();
		SignalKModel model = SignalKModelFactory.getMotuTestInstance();
		
		model.putAll(TestHelper.getSourcesModel().getFullData());
		logger.debug("Input:"+model);
		logger.debug("Input vessels:"+model.getSubMap("vessels"));
		SourceToSourceRefProcessor processor = new SourceToSourceRefProcessor();
		Exchange ex = new DefaultExchange(ctx);
		ex.getIn().setBody(model);
		ex.getIn().setHeader(MSG_TYPE,"unknown");
		processor.process(ex);
		logger.debug("Processed to:"+ex.getIn().getBody());
		String srcRef = "unknown";
		String label ="testAirP";
		assertEquals(srcRef+dot+label,  model.getFullData().get(vessels_dot_self_dot+env_outside_pressure+dot+sourceRef));
		assertEquals("testAirP",  model.getFullData().get(sources+dot+srcRef+dot+label+".label"));
		assertNull(model.getFullData().get(vessels_dot_self_dot+env_outside_pressure+dot+source+".label"));
		
		label ="testCogM";
		assertEquals(srcRef+dot+label,  model.getFullData().get(vessels_dot_self_dot+nav_courseOverGroundMagnetic+dot+sourceRef));
		assertEquals("testCogM",  model.getFullData().get(sources+dot+srcRef+dot+label+".label"));
		assertNull(model.getFullData().get(vessels_dot_self_dot+nav_courseOverGroundMagnetic+dot+source+".label"));
		
		label ="testCogT";
		assertEquals(srcRef+dot+label,  model.getFullData().get(vessels_dot_self_dot+nav_courseOverGroundTrue+dot+sourceRef));
		assertEquals("testCogT",  model.getFullData().get(sources+dot+srcRef+dot+label+".label"));
		assertNull(model.getFullData().get(vessels_dot_self_dot+nav_courseOverGroundTrue+dot+source+".label"));
	}

	@Test
	public void shouldHandleSourceRef() throws Exception{
		CamelContext ctx = RouteManagerFactory.getMotuTestInstance().getContext();
		SignalKModel model = SignalKModelFactory.getMotuTestInstance();
		 model.getFullData().put("vessels.motu.navigation.courseOverGroundMagnetic.$source","unknown");
		 model.getFullData().put("vessels.motu.navigation.courseOverGroundMagnetic.timestamp","2016-02-27T22:57:47.113Z");
		 model.getFullData().put("vessels.motu.navigation.courseOverGroundMagnetic.value",93.0);
		 model.getFullData().put("vessels.motu.navigation.courseOverGroundTrue.$source","unknown");
		 model.getFullData().put("vessels.motu.navigation.courseOverGroundTrue.timestamp","2016-02-27T22:57:47.117Z");
		 model.getFullData().put("vessels.motu.navigation.courseOverGroundTrue.value",11.96);
		 model.getFullData().put("vessels.motu.navigation.position.$source","test");
		 model.getFullData().put("vessels.motu.navigation.position.altitude",0.0);
		 model.getFullData().put("vessels.motu.navigation.position.latitude",-41.29369354);
		 model.getFullData().put("vessels.motu.navigation.position.longitude",11.96);
		 model.getFullData().put("vessels.motu.navigation.position.timestamp","2016-02-27T22:57:46.094Z");
		 
		 logger.debug("Input:"+model);
		SourceToSourceRefProcessor processor = new SourceToSourceRefProcessor();
		Exchange ex = new DefaultExchange(ctx);
		ex.getIn().setBody(model);
		processor.process(ex);
		logger.debug("Processed to:"+ex.getIn().getBody());
		assertNotNull(model.get("vessels.motu.navigation.courseOverGroundMagnetic.$source"));
		assertNotNull(model.get("vessels.motu.navigation.courseOverGroundTrue.$source"));
		assertNotNull(model.get("vessels.motu.navigation.position.$source"));
	}
}
