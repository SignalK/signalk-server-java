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

import static nz.co.fortytwo.signalk.server.util.JsonConstants.SELF;
import static org.junit.Assert.*;
import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.RouteManagerFactory;

import org.apache.camel.CamelContext;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FullToDeltaProcessorTest {
	private static Logger logger = Logger.getLogger(FullToDeltaProcessorTest.class);
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldCreateDelta() {
		CamelContext ctx = RouteManagerFactory.getInstance(null).getContext();
		SignalKModel model = SignalKModelFactory.getInstance();
		Json data = Json
				.read("{\"vessels\":{\"SELF\":{\"environment\":{\"temperature\":{\"air\":{\"value\":26.7,\"source\":\"n2k1-12-0\",\"n2k1-12-0\":{\"value\":26.7,\"source\":{\"label\":\"OutsideAmbientMasthead\",\"bus\":\"/dev/ttyUSB1\",\"timestamp\":\"2014-08-15-16:00:00.081\"}}},\"water\":{\"value\":18.2,\"source\":\"n2k1-12-1\",\"n2k1-12-1\":{\"value\":18.2,\"source\":{\"label\":\"WaterTemperature\",\"bus\":\"/dev/ttyUSB1\",\"timestamp\":\"2014-08-15-16:00:00.081\"}}},\"n2k2-201-0\":{\"value\":66.7,\"source\":{\"label\":\"Another freezer\",\"bus\":\"/dev/ttyUSB2\",\"timestamp\":\"2014-08-15-16:00:00.081\"}},\"aftMainFreezer\":{\"value\":18.2,\"source\":\"n2k2-201-0\",\"n2k2-201-0\":{\"value\":66.7,\"source\":{\"label\":\"Aftmainfreezer\",\"bus\":\"/dev/ttyUSB2\",\"timestamp\":\"2014-08-15-16:00:00.081\"}}}}}}}}");
		
		FullToDeltaProcessor processor = new FullToDeltaProcessor();
		Json out = processor.handle(data);
		logger.debug(out);
	}

}
