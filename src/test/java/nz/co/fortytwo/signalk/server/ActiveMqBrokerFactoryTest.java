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

import static org.junit.Assert.assertTrue;

import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.transport.stomp.Stomp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ActiveMqBrokerFactoryTest {

	private String xmlObject = "<pojo>\n" + "  <name>Dejan</name>\n" + "  <city>Belgrade</city>\n" + "</pojo>";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTransformationReceiveXMLObject() throws Exception {

		/*
		MessageProducer producer = session.createProducer(new ActiveMQQueue("USERS." + getQueueName()));
		ObjectMessage message = session.createObjectMessage(new SamplePojo("Dejan", "Belgrade"));
		producer.send(message);

		String frame = "CONNECT\n" + "login: system\n" + "passcode: manager\n\n" + Stomp.NULL;
		stompConnection.sendFrame(frame);

		frame = stompConnection.receiveFrame();
		assertTrue(frame.startsWith("CONNECTED"));

		frame = "SUBSCRIBE\n" + "destination:/queue/USERS." + getQueueName() + "\n" + "ack:auto" + "\n" + "transformation:jms-object-xml\n\n" + Stomp.NULL;
		stompConnection.sendFrame(frame);

		frame = stompConnection.receiveFrame();

		assertTrue(frame.trim().endsWith(xmlObject));

		frame = "DISCONNECT\n" + "\n\n" + Stomp.NULL;
		stompConnection.sendFrame(frame);*/
	}
}
