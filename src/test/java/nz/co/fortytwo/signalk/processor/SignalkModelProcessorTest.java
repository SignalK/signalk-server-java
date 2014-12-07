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
import nz.co.fortytwo.signalk.model.SignalkRouteFactory;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.processor.SignalkModelProcessor;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.*;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SignalkModelProcessorTest extends CamelTestSupport {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldMerge() {
		SignalKModel signalkModel=SignalKModelFactory.getInstance();
		signalkModel.delete(signalkModel.self(), navigation);
		signalkModel.delete(VESSELS, "366951720");
		SignalkModelProcessor p = new SignalkModelProcessor();
		assertEquals(signalkModel.duplicate().toString(),"{\"vessels\":{\""+SELF+"\":{}}}");
		
		Json json = Json.read("{\"vessels\":{\""+SELF+"\":{\"navigation\":{\"courseOverGroundTrue\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\",\"value\":172.9},\"speedOverGround\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\",\"value\":3.85}}}}}");
		p.handle(json);
		Json cog = signalkModel.findValue(signalkModel.self(),nav_courseOverGroundTrue);
		assertEquals(172.9,cog.asDouble(), 0.001);
	}
	@Test
	public void shouldNotMergeOtherJson() {
		SignalKModel signalkModel=SignalKModelFactory.getInstance();
		signalkModel.delete(signalkModel.self(), navigation);
		signalkModel.delete(VESSELS, "366951720");
		SignalkModelProcessor p = new SignalkModelProcessor();
		assertEquals(signalkModel.duplicate().toString(),"{\"vessels\":{\""+SELF+"\":{}}}");
		
		Json json = Json.read("{\"invalid\":{\""+SELF+"\":{\"navigation\":{\"courseOverGroundTrue\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\",\"value\":172.9},\"speedOverGround\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\",\"value\":3.85}}}}}");
		p.handle(json);
		Json cog = signalkModel.findValue(signalkModel.self(),nav_courseOverGroundTrue);
		assertNull(cog);
		assertEquals(signalkModel.duplicate().toString(),"{\"vessels\":{\""+SELF+"\":{}}}");
		
	}
	@Test
	public void shouldNotMergeNmea() throws Exception {
		SignalKModel signalkModel=SignalKModelFactory.getInstance();
		signalkModel.delete(signalkModel.self(), navigation);
		signalkModel.delete(VESSELS, "366951720");
		SignalkModelProcessor p = new SignalkModelProcessor();
		assertEquals(signalkModel.duplicate().toString(),"{\"vessels\":{\""+SELF+"\":{}}}");
		//make an exchange here
		CamelContext ctx = new DefaultCamelContext(); 
	    Exchange ex = new DefaultExchange(ctx);
	    ex.getIn().setBody("$GPRMC,144629.30,A,5156.91115,N,00434.80383,E,1.689,,011113,,,A*73");
	    p.process(ex);
		assertEquals(signalkModel.duplicate().toString(),"{\"vessels\":{\""+SELF+"\":{}}}");
		
	}
	@Test
	public void shouldNotMergeAis() throws Exception {
		SignalKModel signalkModel=SignalKModelFactory.getInstance();
		signalkModel.delete(signalkModel.self(), navigation);
		signalkModel.delete(VESSELS, "366951720");
		SignalkModelProcessor p = new SignalkModelProcessor();
		assertEquals(signalkModel.duplicate().toString(),"{\"vessels\":{\""+SELF+"\":{}}}");
		//make an exchange here
		CamelContext ctx = new DefaultCamelContext(); 
	    Exchange ex = new DefaultExchange(ctx);
	    ex.getIn().setBody("!AIVDM,1,1,,B,15MwkRUOidG?GElEa<iQk1JV06Jd,0*6D");
	    p.process(ex);
		assertEquals(signalkModel.duplicate().toString(),"{\"vessels\":{\""+SELF+"\":{}}}");
		
	}
	@Test
	public void shouldHandleNull() throws Exception{
		SignalKModel signalkModel=SignalKModelFactory.getInstance();
		signalkModel.delete(signalkModel.self(), navigation);
		signalkModel.delete(VESSELS, "366951720");
		SignalkModelProcessor p = new SignalkModelProcessor();
		assertEquals(signalkModel.duplicate().toString(),"{\"vessels\":{\""+SELF+"\":{}}}");
		//make an exchange here
		CamelContext ctx = new DefaultCamelContext(); 
	    Exchange ex = new DefaultExchange(ctx);
	    ex.getIn().setBody(null);
	    p.process(ex);
		assertEquals(signalkModel.duplicate().toString(),"{\"vessels\":{\""+SELF+"\":{}}}");
	}
	
	
}
