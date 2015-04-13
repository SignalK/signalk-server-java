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

import static nz.co.fortytwo.signalk.util.JsonConstants.SELF;
import static nz.co.fortytwo.signalk.util.JsonConstants.SIGNALK_AUTH;
import static nz.co.fortytwo.signalk.util.JsonConstants.SIGNALK_WS_URL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import mjson.Json;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.client.websocket.WebSocket;
import com.ning.http.client.websocket.WebSocketTextListener;
import com.ning.http.client.websocket.WebSocketUpgradeHandler;

public class WebsocketTest1 extends SignalKCamelTestSupport {
 
    private static Logger logger = Logger.getLogger(WebsocketTest1.class);
	
	String jsonDiff = "{\"updates\":[{\"values\":[{\"value\":172.9,\"path\":\"courseOverGroundTrue\"},{\"value\":3.85,\"path\":\"speedOverGround\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081+00:00\",\"device\":\"/dev/actisense\",\"pgn\":\"128267\",\"src\":\"115\"}}],\"context\":\"vessels."+SELF+".navigation\"}";
	String jsonPosDiff = "{\"updates\":[{\"values\":[{\"path\": \"log\",\"value\": 17404540},{\"value\":172.9,\"path\":\"courseOverGroundTrue\"},{\"value\":3.85,\"path\":\"speedOverGround\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081+00:00\",\"device\":\"/dev/actisense\",\"pgn\":\"128267\",\"src\":\"115\"}}],\"context\":\"vessels."+SELF+".navigation\"}";
	
	@Produce(uri = "direct:input")
    protected ProducerTemplate template;
	
	@After
	public void clearContext() throws Exception{
		//SubscriptionManagerFactory.clear();
		//CamelContextFactory.getInstance().stop();
		//RouteManagerFactory.clear();
		
	}


	
	@Test
    public void shouldReceiveMsgs() throws Exception {
		final List<String> received = new ArrayList<String>();
	    final CountDownLatch latch = new CountDownLatch(2);
	    final CountDownLatch latch1 = new CountDownLatch(1);
	    final CountDownLatch latch2 = new CountDownLatch(1);
        final AsyncHttpClient c = new AsyncHttpClient();
        //get a sessionid
        List<Cookie> cookies = c.prepareGet("http://localhost:"+restPort+SIGNALK_AUTH+"/demo/pass").execute().get().getCookies();
        Response r2 = c.prepareGet("http://localhost:"+restPort+SIGNALK_WS_URL).setCookies(cookies).execute().get();
        latch2.await(5, TimeUnit.SECONDS);
        
      
        
        WebSocket websocket = c.prepareGet(r2.getResponseBody()).setCookies(cookies).execute(
                new WebSocketUpgradeHandler.Builder()
                    .addWebSocketListener(new WebSocketTextListener() {
                        @Override
                        public void onMessage(String message) {
                            
                            log.info("received --> " + message);
                            if(message.startsWith("{\"context\":\"vessels.self\",\"updates\":[]}"))return; //heartbeats
                            latch.countDown();
                            received.add(message);
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
        String subscribeMsg="{\"context\":\"vessels.self\",\"subscribe\":[{\"path\":\"navigation\"}]}";
		websocket.sendTextMessage(subscribeMsg);
        latch1.await(2, TimeUnit.SECONDS);
        
        template.sendBody(RouteManager.SEDA_INPUT,jsonDiff);
        //websocket.sendTextMessage(jsonDiff);
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        assertTrue(2<=received.size());
       Json json = Json.read("{\"context\":\"vessels.motu\",\"updates\":[{\"values\":[{\"path\":\"navigation.courseOverGroundTrue\",\"value\":172.9},{\"path\":\"navigation.speedOverGround\",\"value\":3.85}]},{\"values\":[{\"path\":\"navigation.courseOverGroundTrue\",\"value\":172.9},{\"path\":\"navigation.speedOverGround\",\"value\":3.85}]}]}");
       
       logger.debug("Msg1:" +received.get(0));
        assertEquals(json, Json.read(received.get(0)));
        logger.debug("Msg2:" +received.get(1));
        assertEquals(json, Json.read(received.get(1)));
        
        websocket.close();
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
