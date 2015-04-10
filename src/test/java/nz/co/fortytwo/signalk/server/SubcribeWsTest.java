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

import static nz.co.fortytwo.signalk.util.JsonConstants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import mjson.Json;
import nz.co.fortytwo.signalk.util.Constants;
import nz.co.fortytwo.signalk.util.JsonConstants;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.client.websocket.WebSocket;
import com.ning.http.client.websocket.WebSocketTextListener;
import com.ning.http.client.websocket.WebSocketUpgradeHandler;

public class SubcribeWsTest extends SignalKCamelTestSupport{
 
    private static Logger logger = Logger.getLogger(SubcribeWsTest.class);
	String jsonDiff = null;
	
	@Produce(uri = "direct:input")
    protected ProducerTemplate template;

	public SubcribeWsTest(){
		try {
			jsonDiff = FileUtils.readFileToString(new File("src/test/resources/samples/testUpdate.json"));
			jsonDiff=jsonDiff.replaceAll("SELF", SELF);
		} catch (IOException e) {
			logger.error(e);
			fail();
		}
	}
	@Test
    public void shouldGetSubscribeWsResponse() throws Exception {
		final List<String> received = new ArrayList<String>();
	    final CountDownLatch latch = new CountDownLatch(1);
	    final CountDownLatch latch2 = new CountDownLatch(1);
	    final CountDownLatch latch3 = new CountDownLatch(2);
	    final CountDownLatch latch4 = new CountDownLatch(1);
        final AsyncHttpClient c = new AsyncHttpClient();
        
        template.sendBody(RouteManager.SEDA_INPUT,jsonDiff);
        //get a sessionid
        Response r1 = c.prepareGet("http://localhost:9290/signalk/auth/demoPass").execute().get();
        latch2.await(3, TimeUnit.SECONDS);
        
        
      //await messages
        WebSocket websocket = c.prepareGet("ws://127.0.0.1:9292"+SIGNALK_WS).setCookies(r1.getCookies()).execute(
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

        //websocket.sendTextMessage(jsonDiff);
        latch4.await(2, TimeUnit.SECONDS);
      //subscribe
        Response reponse = c.prepareGet("http://localhost:9290"+SIGNALK_SUBSCRIBE+"/vessels/motu/navigation?format=delta").setCookies(r1.getCookies()).execute().get();
        latch.await(2, TimeUnit.SECONDS);
        logger.debug("Get response = "+reponse.getStatusCode());
        assertEquals(202, reponse.getStatusCode());
        
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
       
        //Json sk = Json.read("{\"context\":\"vessels."+SELF+".navigation\",\"updates\":[{\"values\":[{\"path\":\"courseOverGroundTrue\",\"value\":172.9},{\"path\":\"speedOverGround\",\"value\":3.85}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081+00:00\",\"device\":\"/dev/actisense\",\"src\":\"115\",\"pgn\":\"128267\"}}]}");
        Json sk = Json.read("{\"context\":\"vessels.motu\",\"updates\":[{\"values\":[{\"path\":\"navigation.courseOverGroundTrue\",\"value\":172.9},{\"path\":\"navigation.speedOverGround\",\"value\":3.85}],\"timestamp\":\"2014-08-15T16:00:00.081+00:00\",\"source\":{\"device\":\"/dev/actisense\",\"src\":\"115\",\"pgn\":\"128267\"}}]}");
        assertNotNull(fullMsg);
        assertEquals(sk , Json.read(fullMsg));
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
