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
import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.RouteManagerFactory;

import org.apache.camel.CamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JsonListProcessorTest {

	@Before
	public void setUp() throws Exception {
		CamelContext ctx = RouteManagerFactory.getInstance(null).getContext();
		SignalKModel model = SignalKModelFactory.getInstance();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldProducePathList() throws Exception {
		String request = "{\"context\":\"vessels.*\",\"list\":[{\"path\":\"navigation.*\"}]}";
		Json json = Json.read(request);
		JsonListProcessor processor = new JsonListProcessor();
		Json reply = processor.handle(json, "mmmmmmm");
		assertNotNull(reply);
		assertTrue(reply.at("pathlist").asList().size()>10);
	}
	@Test
	public void shouldProduceMultiplePathList() throws Exception {
		String request = "{\"context\":\"vessels.*\",\"list\":[{\"path\":\"navigation.position.*\"},{\"path\":\"navigation.course*\"}]}";
		Json json = Json.read(request);
		JsonListProcessor processor = new JsonListProcessor();
		Json reply = processor.handle(json, "mmmmmmm");
		assertNotNull(reply);
		assertTrue(reply.at("pathlist").asList().size()>4);
	}
	
	@Test
	public void shouldProduceVesselPathList() throws Exception {
		String request = "{\"context\":\"vessels.motu\",\"list\":[{\"path\":\"navigation.position.*\"},{\"path\":\"navigation.course*\"}]}";
		Json json = Json.read(request);
		JsonListProcessor processor = new JsonListProcessor();
		Json reply = processor.handle(json, "mmmmmmm");
		assertNotNull(reply);
		assertTrue(reply.at("pathlist").asList().size()>4);
	}

}
