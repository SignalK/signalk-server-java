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
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.fortytwo.signalk.server;

import static nz.co.fortytwo.signalk.util.SignalKConstants.FORMAT_DELTA;
import static nz.co.fortytwo.signalk.util.SignalKConstants.POLICY_FIXED;
import static nz.co.fortytwo.signalk.util.SignalKConstants.dot;
import static nz.co.fortytwo.signalk.util.SignalKConstants.env;
import static nz.co.fortytwo.signalk.util.SignalKConstants.env_wind;
import static nz.co.fortytwo.signalk.util.SignalKConstants.env_wind_angleApparent;
import static nz.co.fortytwo.signalk.util.SignalKConstants.env_wind_speedApparent;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels_dot_self_dot;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import mjson.Json;
import nz.co.fortytwo.signalk.client.StompConnection;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.util.SignalKConstants;
import nz.co.fortytwo.signalk.util.TestHelper;

import org.apache.activemq.transport.stomp.Stomp;
import org.apache.activemq.transport.stomp.Stomp.Headers.Subscribe;
import org.apache.activemq.transport.stomp.StompFrame;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.fusesource.stomp.client.Constants;
import org.junit.Test;

public class MqttTest extends SignalKCamelTestSupport {

	static Logger logger = Logger.getLogger(MqttTest.class);

	@Test
	public void testSubscribe() throws Exception {
		// fill the model with data
		SignalKModel model = SignalKModelFactory.getMotuTestInstance();
		model.putAll(TestHelper.getBasicModel().getFullData());

		// create MQTT connection
		MQTT mqtt = new MQTT();
		mqtt.setHost("localhost", 1883);

		BlockingConnection connection = mqtt.blockingConnection();
		logger.debug("Opened MQTT socket, connecting.. ");
		connection.connect();
		// StompFrame connect = connection.receive();
		// if (!connect.getAction().equals(Stomp.Responses.CONNECTED)) {
		// throw new Exception ("Not connected");
		// }
		logger.debug("connected" + connection.toString());

		// create a private receive queue
		String uuid = UUID.randomUUID().toString();
		Topic[] topics = { new Topic("signalk/" + uuid
				+ "/vessels/motu/navigation", QoS.AT_LEAST_ONCE) };
		connection.subscribe(topics);

		// subscribe
		Json subMsg = getSubscribe("vessels." + SignalKConstants.self, "navigation", 1000, 0,
				FORMAT_DELTA, POLICY_FIXED);
		subMsg.set(Constants.REPLY_TO.toString(), "signalk." + uuid
				+ ".vessels.motu.navigation");
		subMsg.set(WebsocketConstants.CONNECTION_KEY, uuid);
		subMsg.set(nz.co.fortytwo.signalk.util.ConfigConstants.OUTPUT_TYPE,
				nz.co.fortytwo.signalk.util.ConfigConstants.OUTPUT_MQTT);
		// HashMap<String,String> headers = new HashMap<String,String>();

		// queue>signalk.3202a939-1681-4a74-ad4b-3a90212e4f33.vessels.motu.navigation
		// set private queue to receive data
		// headers.put("reply-to","/queue/signalk."+uuid+".vessels.motu.navigation");
		// headers.put(WebsocketConstants.CONNECTION_KEY, uuid);
		connection.publish("signalk.put", subMsg.toString().getBytes(),
				QoS.AT_LEAST_ONCE, false);
		logger.debug("Sent subscribe msg: " + subMsg);

		// listen for messages
		Message message = connection.receive(5, TimeUnit.SECONDS);
		String body = new String(message.getPayload());
		logger.debug("Body: " + body);
		message.ack();
		assertNotNull(body);
		Json reply = Json.read(body);

		assertNotNull(reply.at(SignalKConstants.CONTEXT));
		assertNotNull(reply.at(SignalKConstants.UPDATES));
		// unsubscribe
		subMsg = getSubscribe("vessels." + SignalKConstants.self, "navigation", 1000, 0,
				FORMAT_DELTA, POLICY_FIXED);
		// connection.send("/queue/signalk.put", subMsg.toString(),null,
		// headers);
		// connection.unsubscribe("/queue/signalk."+uuid+".vessels.motu.navigation");
		// disconnect
		connection.disconnect();
	}

	@Test
	public void testSendingUpdate() throws Exception {
		// fill the model with data
		SignalKModel model = SignalKModelFactory.getMotuTestInstance();

		// create MQTT connection
		MQTT mqtt = new MQTT();
		mqtt.setHost("localhost", 1883);

		BlockingConnection connection = mqtt.blockingConnection();
		logger.debug("Opened MQTT socket, connecting.. ");
		connection.connect();

		logger.debug("connected" + connection.toString());

		connection
				.publish(
						"signalk.put",
						FileUtils
								.readFileToByteArray(new File(
										"src/test/resources/samples/windAngleUpdate.json.txt")),
						QoS.AT_LEAST_ONCE, false);

		latch.await(2, TimeUnit.SECONDS);
		log.debug("model:" + model);
		assertEquals(338.0,
				model.getValue(vessels_dot_self_dot + env_wind_angleApparent));
		assertEquals(6.8986404,
				model.getValue(vessels_dot_self_dot + env_wind_speedApparent));
		// disconnect
		connection.disconnect();
	}

	@Test
	public void testSendingList() throws Exception {
		// fill the model with data

		SignalKModel model = SignalKModelFactory.getMotuTestInstance();
		model.putAll(TestHelper.getBasicModel().getFullData());

		// create MQTT connection
		MQTT mqtt = new MQTT();
		mqtt.setHost("localhost", 1883);

		BlockingConnection connection = mqtt.blockingConnection();
		logger.debug("Opened MQTT socket, connecting.. ");
		connection.connect();
		// StompFrame connect = connection.receive();
		// if (!connect.getAction().equals(Stomp.Responses.CONNECTED)) {
		// throw new Exception ("Not connected");
		// }
		logger.debug("connected" + connection.toString());

		// create a private receive queue
		String uuid = UUID.randomUUID().toString();
		Topic[] topics = { new Topic("signalk/" + uuid
				+ "/vessels/motu/navigation", QoS.AT_LEAST_ONCE) };
		connection.subscribe(topics);

		latch.await(2, TimeUnit.SECONDS);
		// send get
		Json subMsg = getList("vessels." + SignalKConstants.self, "navigation.position.*");

		subMsg.set(Constants.REPLY_TO.toString(), "signalk." + uuid
				+ ".vessels.motu.navigation");
		subMsg.set(WebsocketConstants.CONNECTION_KEY, uuid);
		// HashMap<String,String> headers = new HashMap<String,String>();

		// queue>signalk.3202a939-1681-4a74-ad4b-3a90212e4f33.vessels.motu.navigation
		// set private queue to receive data
		// headers.put("reply-to","/queue/signalk."+uuid+".vessels.motu.navigation");
		// headers.put(WebsocketConstants.CONNECTION_KEY, uuid);
		connection.publish("signalk.put", subMsg.toString().getBytes(),
				QoS.AT_LEAST_ONCE, false);
		logger.debug("Sent get msg: " + subMsg);

		// listen for messages
		Message message = connection.receive(5, TimeUnit.SECONDS);
		String body = new String(message.getPayload());
		logger.debug("Body: " + body);
		message.ack();
		assertNotNull(body);
		Json reply = Json.read(body);

		assertNotNull(reply.at(SignalKConstants.CONTEXT));
		assertNotNull(reply.at(SignalKConstants.PATHLIST));
		// unsubscribe

		// disconnect
		connection.disconnect();
	}

	@Test
	public void testSendingGetFull() throws Exception {
		// fill the model with data
		SignalKModel model = SignalKModelFactory.getMotuTestInstance();
		model.putAll(TestHelper.getBasicModel().getFullData());

		// create MQTT connection
		MQTT mqtt = new MQTT();
		mqtt.setHost("localhost", 1883);

		BlockingConnection connection = mqtt.blockingConnection();
		logger.debug("Opened MQTT socket, connecting.. ");
		connection.connect();
		// StompFrame connect = connection.receive();
		// if (!connect.getAction().equals(Stomp.Responses.CONNECTED)) {
		// throw new Exception ("Not connected");
		// }
		logger.debug("connected" + connection.toString());

		// create a private receive queue
		String uuid = UUID.randomUUID().toString();
		Topic[] topics = { new Topic("signalk/" + uuid
				+ "/vessels/motu/navigation", QoS.AT_LEAST_ONCE) };
		connection.subscribe(topics);

		latch.await(2, TimeUnit.SECONDS);
		// send get
		Json subMsg = getGet("vessels." + SignalKConstants.self, env_wind + ".*",
				SignalKConstants.FORMAT_FULL);

		subMsg.set(Constants.REPLY_TO.toString(), "signalk." + uuid
				+ ".vessels.motu.navigation");
		subMsg.set(WebsocketConstants.CONNECTION_KEY, uuid);
		// HashMap<String,String> headers = new HashMap<String,String>();

		// queue>signalk.3202a939-1681-4a74-ad4b-3a90212e4f33.vessels.motu.navigation
		// set private queue to receive data
		// headers.put("reply-to","/queue/signalk."+uuid+".vessels.motu.navigation");
		// headers.put(WebsocketConstants.CONNECTION_KEY, uuid);
		connection.publish("signalk.put", subMsg.toString().getBytes(),
				QoS.AT_LEAST_ONCE, false);
		logger.debug("Sent get msg: " + subMsg);

		// listen for messages
		Message message = connection.receive(5, TimeUnit.SECONDS);
		String body = new String(message.getPayload());
		logger.debug("Body: " + body);
		message.ack();
		assertNotNull(body);
		Json reply = Json.read(body);

		assertNotNull(reply.at(SignalKConstants.vessels));
		assertNotNull(reply.at(SignalKConstants.vessels).at(SignalKConstants.self).at(env)
				.at("wind"));
		// unsubscribe
		// connection.unsubscribe("/queue/signalk."+uuid+"."+vessels_dot_self_dot+env_wind);
		// disconnect
		connection.disconnect();
	}

	@Test
	public void testSendingGetDelta() throws Exception {
		// fill the model with data
		SignalKModel model = SignalKModelFactory.getMotuTestInstance();
		model.putAll(TestHelper.getBasicModel().getFullData());

		// create STOMP connection
		StompConnection connection = new StompConnection();
		connection.open("localhost", 61613);
		logger.debug("Opened STOMP socket, connecting.. ");
		StompFrame connect = connection.connect("system", "manager");
		// StompFrame connect = connection.receive();
		if (!connect.getAction().equals(Stomp.Responses.CONNECTED)) {
			throw new Exception("Not connected");
		}
		logger.debug("connected" + connect.getHeaders());

		// create a private receive queue
		String uuid = UUID.randomUUID().toString();
		connection
				.subscribe("/queue/signalk." + uuid + "."
						+ vessels_dot_self_dot + env_wind,
						Subscribe.AckModeValues.AUTO);
		latch.await(2, TimeUnit.SECONDS);
		// send list
		Json subMsg = getGet("vessels." + SignalKConstants.self, env_wind + ".*",
				SignalKConstants.FORMAT_DELTA);
		HashMap<String, String> headers = new HashMap<String, String>();
		logger.debug("sending" + subMsg);
		// queue>signalk.3202a939-1681-4a74-ad4b-3a90212e4f33.vessels.motu.navigation
		// set private queue to receive data
		headers.put("reply-to", "/queue/signalk." + uuid + dot
				+ vessels_dot_self_dot + env_wind);
		headers.put(WebsocketConstants.CONNECTION_KEY, uuid);
		connection.send("/queue/signalk.put", subMsg.toString(), null, headers);

		// listen for messages
		StompFrame message = connection.receive();
		logger.debug("Body: " + message.getBody());
		assertNotNull(message);
		Json reply = Json.read(message.getBody());

		assertNotNull(reply.at(SignalKConstants.CONTEXT));
		assertNotNull(reply.at(SignalKConstants.UPDATES));
		// unsubscribe
		connection.unsubscribe("/queue/signalk." + uuid + "."
				+ vessels_dot_self_dot + env_wind);
		// disconnect
		connection.disconnect();
	}

	@Override
	public void configureRouteBuilder(RouteBuilder routeBuilder) {
		try {
			((RouteManager) routeBuilder).configure0();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
			fail();
		}

	}
}
