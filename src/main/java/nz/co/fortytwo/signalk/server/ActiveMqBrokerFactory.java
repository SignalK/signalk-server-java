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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTopic;

public class ActiveMqBrokerFactory {

	public static BrokerService newInstance() throws Exception {
		final BrokerService broker = new BrokerService();
		// vm connector
		TransportConnector connector = new TransportConnector();
		connector.setUri(new URI("tcp://localhost:61616"));
		broker.addConnector(connector);
		broker.addConnector("ws://localhost:61614");
		TransportConnector stomp = new TransportConnector();
		stomp.setUri(new URI("stomp+nio://localhost:61613?transport.hbGracePeriodMultiplier=1.5"));
		broker.addConnector(stomp);
		
		List<BrokerPlugin> plugins = new ArrayList<BrokerPlugin>();
		BrokerPlugin[] pluginArray = broker.getPlugins();
		if(pluginArray!=null){
			plugins.addAll(Arrays.asList(pluginArray));
		}
		plugins.add(new ActiveMqSubscriptionPlugin());
		broker.setPlugins(plugins.toArray(new BrokerPlugin[0]));
		
		broker.setPersistent(false);
//"activemq:topic:ActiveMQ.Advisory.Connection?mapJmsMessage=false"
		final ActiveMQTopic topic = new ActiveMQTopic("ActiveMQ.Advisory.Connection");
		broker.setDestinations(new ActiveMQDestination[] { topic });

		final ManagementContext managementContext = new ManagementContext();
		managementContext.setCreateConnector(true);
		broker.setManagementContext(managementContext);

		return broker;
	}
}
