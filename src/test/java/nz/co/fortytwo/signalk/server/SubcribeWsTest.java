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

import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_AUTH;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_DISCOVERY;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_WS;
import static nz.co.fortytwo.signalk.util.SignalKConstants.websocketUrl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import mjson.Json;
import nz.co.fortytwo.signalk.util.SignalKConstants;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.client.websocket.WebSocket;
import com.ning.http.client.websocket.WebSocketTextListener;
import com.ning.http.client.websocket.WebSocketUpgradeHandler;

public class SubcribeWsTest extends SignalKCamelTestSupport{
 
    private static Logger logger = LogManager.getLogger(SubcribeWsTest.class);
	String jsonDiff = null;
	
	
	@Produce(uri = "direct:input")
    protected ProducerTemplate template;
	


	public SubcribeWsTest(){
		try {
			
			jsonDiff = FileUtils.readFileToString(new File("src/test/resources/samples/testUpdate.json"));
			jsonDiff=jsonDiff.replaceAll("self", SignalKConstants.self);
		} catch (IOException e) {
			logger.error(e);
			fail();
		}
	}
	
	@Test
    public void shouldGetWsUrl() throws Exception {
		
        final AsyncHttpClient c = new AsyncHttpClient();
        
        //get a sessionid
        Response r1 = c.prepareGet("http://localhost:"+restPort+SIGNALK_AUTH+"/demo/pass").execute().get();
        assertEquals(200, r1.getStatusCode());
        Response r2 = c.prepareGet("http://localhost:"+restPort+SIGNALK_DISCOVERY).setCookies(r1.getCookies()).execute().get();
        Json json = Json.read(r2.getResponseBody());
        assertEquals("ws://localhost:"+wsPort+SIGNALK_WS, json.at("endpoints").at("v1").at(websocketUrl).asString());
        c.close();
	}
	
	@Test
    public void shouldGetSubscribeWsResponse() throws Exception {
		final List<String> received = new ArrayList<String>();
	    final CountDownLatch latch2 = new CountDownLatch(1);
	    final CountDownLatch latch3 = new CountDownLatch(2);
	    final CountDownLatch latch4 = new CountDownLatch(1);
        final AsyncHttpClient c = new AsyncHttpClient();
        
        template.sendBody(RouteManager.SEDA_INPUT,jsonDiff);
        //get a sessionid
        Response r1 = c.prepareGet("http://localhost:"+restPort+SIGNALK_AUTH+"/demo/pass").execute().get();
        Response r2 = c.prepareGet("http://localhost:"+restPort+SIGNALK_DISCOVERY).setCookies(r1.getCookies()).execute().get();
        Json json = Json.read(r2.getResponseBody());
        latch2.await(3, TimeUnit.SECONDS);
      //await messages
        WebSocket websocket = c.prepareGet(json.at("endpoints").at("v1").at(websocketUrl).asString()).setCookies(r1.getCookies()).execute(
                new WebSocketUpgradeHandler.Builder()
                    .addWebSocketListener(new WebSocketTextListener() {
                        @Override
                        public void onMessage(String message) {
                            received.add(message);
                            log.info("received --> " + message);
                            //{"context":"vessels.self","updates":[]}
                            if(message.startsWith("{\"context\":\"vessels.self\",\"updates\":[]}"))return; //heartbeats
                            latch3.countDown();
                        }

                        @Override
                        public void onFragment(String fragment, boolean last) {
                        }

                        @Override
                        public void onOpen(WebSocket websocket) {
                        }

                        @Override
                        public void onClose(WebSocket websocket) {
                        }

                        @Override
                        public void onError(Throwable t) {
                            t.printStackTrace();
                        }
                    }).build()).get();

      //subscribe
        String subscribeMsg="{\"context\":\"vessels.motu\",\"subscribe\":[{\"path\":\"navigation\"}]}";
		websocket.sendTextMessage(subscribeMsg);
        latch4.await(2, TimeUnit.SECONDS);
      
        websocket.sendTextMessage(jsonDiff);
        logger.debug("Sent update = "+jsonDiff);
        latch3.await(10, TimeUnit.SECONDS);
        
        //assertTrue(latch3.await(15, TimeUnit.SECONDS));
        String fullMsg = null;
        for(String msg : received){
        	logger.debug("Received msg = "+msg);
        	if(msg.contains("\"updates\":[{\"")){
        		fullMsg=msg;
        	}
        }
        assertTrue(received.size()>1);
       
        //Json sk = Json.read("{\"context\":\"vessels."+SignalKConstants.self+".navigation\",\"updates\":[{\"values\":[{\"path\":\"courseOverGroundTrue\",\"value\":3.0176},{\"path\":\"speedOverGround\",\"value\":3.85}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081+00:00\",\"device\":\"/dev/actisense\",\"src\":\"115\",\"pgn\":\"128267\"}}]}");
        //Json sk = Json.read("{\"context\":\"vessels.motu\",\"updates\":[{\"values\":[{\"path\":\"navigation.courseOverGroundTrue\",\"value\":3.0176},{\"path\":\"navigation.speedOverGround\",\"value\":3.85}],\"timestamp\":\"2014-08-15T16:00:00.081+00:00\",\"source\":{\"device\":\"/dev/actisense\",\"src\":\"115\",\"pgn\":\"128267\"}}]}");
        assertNotNull(fullMsg);
        assertTrue(fullMsg.contains("\"context\":\"vessels.motu\""));
        assertTrue(fullMsg.contains("\"path\":\"navigation.courseOverGroundTrue\""));
        assertTrue(fullMsg.contains("\"value\":3.0176"));
        assertTrue(fullMsg.contains("\"updates\":[{"));
        
        c.close();
    }
	
	

	@Override
	public void configureRouteBuilder(RouteBuilder routeBuilder) {
		// TODO Auto-generated method stub
		try {
			((RouteManager)routeBuilder).configure0();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
			fail();
		}
	}

}
