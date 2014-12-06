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

import static org.junit.Assert.*;
import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.processor.DeclinationProcessor;
import nz.co.fortytwo.signalk.server.util.JsonConstants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeclinationProcessorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldGetDeclination() {
		DeclinationProcessor p = new DeclinationProcessor();
		SignalKModel model = SignalKModelFactory.getInstance();
		model.putWith(model.self(), JsonConstants.nav_position_latitude, -41.5);
		model.putWith(model.self(), JsonConstants.nav_position_longitude, 172.5);
		p.handle();
		double decl = model.findValue(model.self(),JsonConstants.nav_magneticVariation).asDouble();
		assertEquals(22.1, decl, 001);
	}
	
	@Test
	public void shouldNotGetDeclination() {
		DeclinationProcessor p = new DeclinationProcessor();
		SignalKModel model = SignalKModelFactory.getInstance();
		model.putWith(model.self(), JsonConstants.nav_position_latitude, -41.5);
		//model.putWith(model.self(), JsonConstants.nav_position_longitude, 172.5);
		p.handle();
		Json decl = model.findValue(model.self(),JsonConstants.nav_magneticVariation);
		assertNull( decl);
	}

}
