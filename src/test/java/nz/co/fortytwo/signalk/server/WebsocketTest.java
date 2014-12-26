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

import static nz.co.fortytwo.signalk.server.util.JsonConstants.SELF;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.SIGNALK_WS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.util.JsonConstants;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.client.websocket.WebSocket;
import com.ning.http.client.websocket.WebSocketTextListener;
import com.ning.http.client.websocket.WebSocketUpgradeHandler;

public class WebsocketTest extends CamelTestSupport {
 
    private static Logger logger = Logger.getLogger(WebsocketTest.class);
	private SignalKModel signalkModel=SignalKModelFactory.getInstance();
	String jsonDiff = "{\"updates\":[{\"values\":[{\"value\":172.9,\"path\":\"courseOverGroundTrue\"},{\"value\":3.85,\"path\":\"speedOverGround\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081+00:00\",\"device\":\"/dev/actisense\",\"pgn\":\"128267\",\"src\":\"115\"}}],\"context\":\"vessels."+SELF+".navigation\"}";
	
	@Produce(uri = "direct:input")
    protected ProducerTemplate template;
	
	

	@Test
    public void shouldUpdateAfterSendingMsg() throws Exception {
		final List<String> received = new ArrayList<String>();
	    final CountDownLatch latch = new CountDownLatch(1);
	    final CountDownLatch latch2 = new CountDownLatch(1);
        final AsyncHttpClient c = new AsyncHttpClient();
        //get a sessionid
        c.prepareGet("http://localhost:9290/signalk/auth/demoPass").execute().get();
        latch2.await(5, TimeUnit.SECONDS);
        
        WebSocket websocket = c.prepareGet("ws://127.0.0.1:9292"+SIGNALK_WS+"?test=1234").execute(
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
      
        assertTrue(latch.await(20, TimeUnit.SECONDS));

        assertEquals(1, received.size());
        			//{\"updates\":[{\"values\":[{\"value\":172.9,\"path\":\"navigation.courseOverGroundTrue\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}}],\"context\":\"vessels.self\"}
        Json sk = Json.read("{\"updates\":[{\"values\":[{\"value\":172.9,\"path\":\"navigation.courseOverGroundTrue\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}},{\"values\":[{\"value\":3.85,\"path\":\"navigation.speedOverGround\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}}],\"context\":\"vessels."+SELF+"\"}");
        assertEquals(sk , Json.read(received.get(0)));
       
        websocket.close();
        c.close();
    }
	
	@Test
    public void shouldReceiveMsgs() throws Exception {
		final List<String> received = new ArrayList<String>();
	    final CountDownLatch latch = new CountDownLatch(1);
	    final CountDownLatch latch2 = new CountDownLatch(1);
        final AsyncHttpClient c = new AsyncHttpClient();
        //get a sessionid
        List<Cookie> cookies = c.prepareGet("http://localhost:9290/signalk/auth/demoPass").execute().get().getCookies();
        latch2.await(5, TimeUnit.SECONDS);
        
        WebSocket websocket = c.prepareGet("ws://127.0.0.1:9292"+SIGNALK_WS+"?test=1234").setCookies(cookies).execute(
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
        //template.sendBody(RouteManager.SEDA_INPUT,jsonDiff);
        websocket.sendTextMessage(jsonDiff);
        assertTrue(latch.await(10, TimeUnit.SECONDS));

        assertEquals(1, received.size());
       Json json = Json.read("{\"context\":\"vessels\",\"updates\":[{\"values\":[{\"value\":172.9,\"path\":\""+SELF+".navigation.courseOverGroundTrue\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}},{\"values\":[{\"value\":3.85,\"path\":\""+SELF+".navigation.speedOverGround\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}}]}");
        assertEquals(json, Json.read(received.get(0)));
        
        websocket.close();
        c.close();
    }
	
	
	 @Override
	    protected RouteBuilder createRouteBuilder() {
	        return new RouteBuilder(){
	            public void configure() {
	    			SignalkRouteFactory.configureInputRoute(this, RouteManager.SEDA_INPUT);
	    			from(RouteManager.DIRECT_TCP).to("log:nz.co.fortytwo.signalk.model.output.tcp").end();
	    			SignalkRouteFactory.configureWebsocketRxRoute(this, RouteManager.SEDA_INPUT, 9292);
	    			SignalkRouteFactory.configureWebsocketTxRoute(this,  RouteManager.SEDA_WEBSOCKETS, 9292);
	    			SignalkRouteFactory.configureRestRoute(this, "jetty:http://0.0.0.0:9290" + JsonConstants.SIGNALK_API+"?sessionSupport=true&matchOnUriPrefix=true");
	    			SignalkRouteFactory.configureAuthRoute(this, "jetty:http://0.0.0.0:9290" + JsonConstants.SIGNALK_AUTH+"?sessionSupport=true&matchOnUriPrefix=true");
	    			SignalkRouteFactory.configureOutputTimer(this, "timer://signalkAll?fixedRate=true&period=1000");
	    			
	            }
	        };
	    }

}
