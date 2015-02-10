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
import static nz.co.fortytwo.signalk.util.JsonConstants.SIGNALK_SUBSCRIBE;
import static nz.co.fortytwo.signalk.util.JsonConstants.SIGNALK_WS;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.util.JsonConstants;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.websocket.SignalkWebsocketComponent;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.client.websocket.WebSocket;
import com.ning.http.client.websocket.WebSocketTextListener;
import com.ning.http.client.websocket.WebSocketUpgradeHandler;

public class WebsocketTest2 extends CamelTestSupport {
 
    private static Logger logger = Logger.getLogger(WebsocketTest2.class);
	private SignalKModel signalkModel=SignalKModelFactory.getInstance();
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
    public void shouldUpdateAfterSendingWsMsg() throws Exception {
		final List<String> received = new ArrayList<String>();
	    final CountDownLatch latch = new CountDownLatch(1);
	    final CountDownLatch latch2 = new CountDownLatch(1);
	    final CountDownLatch latch3 = new CountDownLatch(1);
        final AsyncHttpClient c = new AsyncHttpClient();
        //get a sessionid
        List<Cookie> cookies = c.prepareGet("http://localhost:9290/signalk/auth/demoPass").execute().get().getCookies();
        latch2.await(5, TimeUnit.SECONDS);
        
        //subscribe
        Response reponse = c.prepareGet("http://localhost:9290"+SIGNALK_SUBSCRIBE+"/vessels/"+SELF).setCookies(cookies).execute().get();
        latch3.await(2, TimeUnit.SECONDS);
        logger.debug(reponse.getResponseBody());
        assertEquals(202, reponse.getStatusCode());
        
        WebSocket websocket = c.prepareGet("ws://127.0.0.1:9292"+SIGNALK_WS+"?format=delta").setCookies(cookies).execute(
                new WebSocketUpgradeHandler.Builder()
                    .addWebSocketListener(new WebSocketTextListener() {
                        @Override
                        public void onMessage(String message) {
                            received.add(message);
                            log.info("received --> " + message);
                            latch.countDown();
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


        websocket.sendTextMessage(jsonDiff);
      
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        assertEquals(1, received.size());
        //{"context":"vessels","updates":[{"values":[{"path":"motu.navigation.courseOverGroundTrue","value":172.9}],"source":{"timestamp":"2014-08-15T16:00:00.081Z","source":"/dev/actisense-N2K-115-128267"}},{"values":[{"path":"motu.navigation.speedOverGround","value":3.85}],"source":{"timestamp":"2014-08-15T16:00:00.081Z","source":"/dev/actisense-N2K-115-128267"}}]}
        //{\"context\":\"vessels\",\"updates\":[{\"values\":[{\"path\":\"motu.navigation.courseOverGroundTrue\",\"value\":172.9}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}},{\"values\":[{\"path\":\"motu.navigation.speedOverGround\",\"value\":3.85}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}}]}
       // Json sk = Json.read("{\"context\":\"vessels."+SELF+"\",\"updates\":[{\"values\":[{\"path\":\"navigation.courseOverGroundTrue\",\"value\":172.9}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}},{\"values\":[{\"path\":\"navigation.speedOverGround\",\"value\":3.85}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}}]}");
        Json sk = Json.read("{\"context\":\"vessels\",\"updates\":[{\"values\":[{\"path\":\"motu.navigation.courseOverGroundTrue\",\"value\":172.9}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}},{\"values\":[{\"path\":\"motu.navigation.speedOverGround\",\"value\":3.85}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}}]}");
        logger.debug(received.get(0));
        assertEquals(sk , Json.read(received.get(0)));
        
        websocket.close();
        c.close();
        
    }
	
	
	
	 @Override
	 protected RouteBuilder createRouteBuilder() {
	        try {
				RouteManager routeBuilder = RouteManagerFactory.getInstance(Util.getConfig(null));
				return routeBuilder;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return null;
	    }

}
