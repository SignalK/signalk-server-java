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

package nz.co.fortytwo.signalk.server;

import static org.junit.Assert.*;
import mjson.Json;
import nz.co.fortytwo.signalk.server.AISProcessor;

import org.apache.log4j.Logger;
import org.junit.Test;

//public class AISProcessorTest extends CamelTestSupport {
public class AISProcessorTest{

	private static Logger log = Logger.getLogger(AISProcessorTest.class);
	/*
	@EndpointInject(uri = "mock:result")
	protected MockEndpoint resultEndpoint;

	@Produce(uri = "direct:start")
	protected ProducerTemplate template;
*/
	
	@Test
	public void shouldPassJson(){
		 String jStr = "{\"vessels\":{\"self\":{\"environment\":{\"wind\":{\"directionApparent\":0.0000000000,\"directionTrue\":0.0000000000,\"speedApparent\":0.0000000000,\"speedTrue\":20.0000000000}}}}}";
		 AISProcessor processor = new AISProcessor();
		
		 String json = (String) processor.handle(jStr);
		 log.debug(json);
		 assertEquals(jStr, json);
	}
	@Test
	//@Ignore
	public void shouldIgnoreSingleMessage() {
		String msg = "$PGHP,1,2010,6,11,11,46,11,929,244,0,,1,72*21\r\n";
		msg += "\\1G2:0125,c:1354719387*0D";
		msg += "\\!AIVDM,2,1,4,A,539LiHP2;42`@pE<000<tq@V1<TpL4000000001?1SV@@73R0J0TQCAD,0*1E\r\n";
		msg += "\\2G2:0125*7B";
		msg += "\\!AIVDM,2,2,4,A,R0EQCP000000000,2*45";
		AISProcessor processor = new AISProcessor();
		Object obj = processor.handle(msg);
		assertEquals(msg,obj);

	}

	@Test
	//@Ignore
	public void shouldParseSingleMessage() {
		String msg = "!AIVDM,1,1,,B,15MwkRUOidG?GElEa<iQk1JV06Jd,0*6D";
		AISProcessor processor = new AISProcessor();
		Json json = (Json) processor.handle(msg);
		assertNotNull(json);
		
		log.debug(msg);
	}

	@Test
	//@Ignore
	public void shouldIgnoreSeparatedMessage() {

		String msg1 = "$PGHP,1,2013,3,13,10,39,18,375,219,,2190047,1,4A*57\r\n";
		String msg2 = "\\g:1-2-0136,c:1363174860*24\\!BSVDM,2,1,4,B,53B>2V000000uHH4000@T4p4000000000000000S30C6340006h00000,0*4C\r\n";
		String msg3 = "\\g:2-2-0136*59\\!BSVDM,2,2,4,B,000000000000000,2*3A";
		AISProcessor processor = new AISProcessor();
		Object json = processor.handle(msg1);
		assertEquals(msg1,json);

		json = processor.handle(msg2);
		assertEquals(msg2,json);

		json = processor.handle(msg3);
		assertEquals(msg3,json);
		// assertEquals(map.get("TEST"), 5);
	}

	@Test
	//@Ignore
	public void shouldParseTwoMessages() {

		String msg1 = "!AIVDM,1,1,,A,15MvJw5P0NG?Us6EaDVTTOvR06Jd,0*22";
		AISProcessor processor = new AISProcessor();
		Json json = (Json)processor.handle(msg1);
		assertNotNull(json);
		//assertTrue(map.get(Constants.AIS) instanceof AisVesselInfo);

		String msg = "!AIVDM,1,1,,B,15Mtu:0000o@05tE`?Ctn@6T06Jd,0*40";
		json=null;
		json = (Json)processor.handle(msg);
		assertNotNull(json);
		//assertTrue(map.get(Constants.AIS) instanceof AisVesselInfo);
	}

	@Test
	//@Ignore
	public void shouldIgnoreTwoMessages() {

		String msg1 = "$PGHP,1,2013,3,13,10,39,18,375,219,,2190047,1,4A*57\r\n";
		String msg2 = "\\g:1-2-0136,c:1363174860*24\\!BSVDM,2,1,4,B,53B>2V000000uHH4000@T4p4000000000000000S30C6340006h00000,0*4C\r\n";
		String msg3 = "\\g:2-2-0136*59\\!BSVDM,2,2,4,B,000000000000000,2*3A";
		AISProcessor processor = new AISProcessor();
		Object json = processor.handle(msg1);
		assertEquals(msg1,json);

		json = processor.handle(msg2);
		assertEquals(msg2,json);

		json = processor.handle(msg3);
		assertEquals(msg3,json);
		json=null;
		String msg = "$PGHP,1,2010,6,11,11,46,11,929,244,0,,1,72*21\r\n";
		msg += "\\1G2:0125,c:1354719387*0D";
		msg += "\\!AIVDM,2,1,4,A,539LiHP2;42`@pE<000<tq@V1<TpL4000000001?1SV@@73R0J0TQCAD,0*1E\r\n";
		msg += "\\2G2:0125*7B";
		msg += "\\!AIVDM,2,2,4,A,R0EQCP000000000,2*45";

		json = processor.handle(msg);
		assertEquals(msg,json);
		// assertEquals(map.get("TEST"), 5);
	}
/*
	@Test
	@Ignore
	public void shouldProduceJson() throws InterruptedException, UnsupportedEncodingException {
		resultEndpoint.expectedMessageCount(1);
		String msg = "!AIVDM,1,1,,A,15Mw0LPP3hG?PPTEag8UW?vj0@?A,0*47";
		AISProcessor processor = new AISProcessor();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(Constants.AIS, msg);
		processor.handle(map);
		// RouteBuilder builder = createRouteBuilder();
		template.sendBody(map);
		resultEndpoint.assertIsSatisfied();
		byte[] bytes = (byte[]) resultEndpoint.getReceivedExchanges().get(0).getIn().getBody();
		String json = new String(bytes, "UTF-8");

		log.debug(json);
		assertTrue(json.indexOf("{\"AIS\":{\"position\":{") == 0);

	}

	@Test
	@Ignore
	public void shouldProduceJson2() throws InterruptedException, UnsupportedEncodingException {
		resultEndpoint.expectedMessageCount(1);

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(Constants.AIS, 123);
		// processor.handle(map);
		// RouteBuilder builder = createRouteBuilder();
		template.sendBody(map);
		resultEndpoint.assertIsSatisfied();
		byte[] bytes = (byte[]) resultEndpoint.getReceivedExchanges().get(0).getIn().getBody();
		String json = new String(bytes, "UTF-8");

		log.debug(json);
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			public void configure() {
				from("direct:start").marshal().json(JsonLibrary.Jackson).to("mock:result");
			}
		};
	}*/
}
