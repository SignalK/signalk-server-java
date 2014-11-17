package nz.co.fortytwo.signalk.server;

import static nz.co.fortytwo.signalk.server.util.JsonConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.SignalkRouteFactory;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.websocket.WebSocket;
import com.ning.http.client.websocket.WebSocketTextListener;
import com.ning.http.client.websocket.WebSocketUpgradeHandler;

public class WebsocketTest extends CamelTestSupport {
 
    private static Logger logger = Logger.getLogger(WebsocketTest.class);
	private SignalKModel signalkModel=SignalKModelFactory.getInstance();
	String jsonDiff = "{\"updates\":[{\"values\":[{\"value\":172.9,\"path\":\"courseOverGroundTrue\"},{\"value\":3.85,\"path\":\"speedOverGround\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081+00:00\",\"device\":\"/dev/actisense\",\"pgn\":\"128267\",\"src\":\"115\"}}],\"context\":\"vessels.self.navigation\"}";
	
	@Produce(uri = "direct:input")
    protected ProducerTemplate template;
	
	

	@Test
    public void shouldUpdateAfterSendingMsg() throws Exception {
		final List<String> received = new ArrayList<String>();
	    final CountDownLatch latch = new CountDownLatch(1);
        final AsyncHttpClient c = new AsyncHttpClient();

        WebSocket websocket = c.prepareGet("ws://127.0.0.1:9292"+SIGNALK_WS).execute(
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
      
        assertTrue(latch.await(10, TimeUnit.SECONDS));

        assertEquals(1, received.size());
        			//{\"updates\":[{\"values\":[{\"value\":172.9,\"path\":\"navigation.courseOverGroundTrue\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}}],\"context\":\"vessels.self\"}
        String sk = "{\"updates\":[{\"values\":[{\"value\":172.9,\"path\":\"navigation.courseOverGroundTrue\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}},{\"values\":[{\"value\":3.85,\"path\":\"navigation.speedOverGround\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}}],\"context\":\"vessels.self\"}";
        assertEquals(sk , received.get(0));
       
        websocket.close();
        c.close();
    }
	
	@Test
    public void shouldReceiveMsgs() throws Exception {
		final List<String> received = new ArrayList<String>();
	    final CountDownLatch latch = new CountDownLatch(1);
        
        AsyncHttpClient c = new AsyncHttpClient();
        WebSocket websocket = c.prepareGet("ws://127.0.0.1:9292"+SIGNALK_WS).execute(
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
        template.sendBody(SignalKReceiver.SEDA_INPUT,jsonDiff);
        assertTrue(latch.await(10, TimeUnit.SECONDS));

        assertEquals(1, received.size());
        String sk = "{\"updates\":[{\"values\":[{\"value\":172.9,\"path\":\"navigation.courseOverGroundTrue\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}},{\"values\":[{\"value\":3.85,\"path\":\"navigation.speedOverGround\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}}],\"context\":\"vessels.self\"}";
        assertEquals(sk, received.get(0));
        
        websocket.close();
        c.close();
    }
	
	
	 @Override
	    protected RouteBuilder createRouteBuilder() {
	        return new RouteBuilder(){
	            public void configure() {
	    			SignalkRouteFactory.configureInputRoute(this, SignalKReceiver.SEDA_INPUT);
	    			SignalkRouteFactory.configureWebsocketRxRoute(this, SignalKReceiver.SEDA_INPUT, 9292);
	    			SignalkRouteFactory.configureWebsocketTxRoute(this,  SignalKReceiver.DIRECT_WEBSOCKETS, 9292,null);
	    			from("timer://signalkAll?fixedRate=true&period=1000").process(new DeltaExportProcessor()).split(body())
	    				.to("log:nz.co.fortytwo.signalk.model.signalkAll?level=INFO")
	    				.to(SignalKReceiver.DIRECT_WEBSOCKETS)
	    				//.to(DIRECT_TCP)
	    				.end();
	            }
	        };
	    }

}
