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
package nz.co.fortytwo.signalk.server.util;

import static nz.co.fortytwo.signalk.server.util.JsonConstants.SELF;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.VESSELS;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.nav_anchor_position_latitude;
import static org.junit.Assert.assertEquals;
import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldMakeNode() {
		SignalKModel signalkModel = SignalKModelFactory.getInstance();
		Json root = (Json) SignalKModelFactory.getCleanInstance();
		//Json position = signalkModel.addNode(root, VESSELS+"."+SELF+"."+nav_anchor_position_latitude);
		//System.out.println(position);
		System.out.println(signalkModel.putWith(root,VESSELS+"."+SELF+"."+nav_anchor_position_latitude, 23.07d, "nmea"));
		System.out.println(root);
		assertEquals(signalkModel.findValue(root,VESSELS+"."+SELF+"."+nav_anchor_position_latitude).asDouble(), 023.07d, 0.0002);
	}

}
