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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.broker.region.policy.IndividualDeadLetterStrategy;
import org.apache.activemq.broker.region.policy.OldestMessageEvictionStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.policy.PrefetchRatePendingMessageLimitStrategy;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.SystemUsage;
import org.apache.camel.component.jms.JmsConfiguration;

import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.Util;

public class ActiveMqBrokerFactory {

	public static BrokerService newInstance() throws Exception {
		final BrokerService broker = new BrokerService();
		broker.setUseShutdownHook(false);
		broker.setDeleteAllMessagesOnStartup(true);
		// vm connector
		TransportConnector connector = new TransportConnector();
		connector.setUri(new URI("tcp://localhost:61616"));
		broker.addConnector(connector);
		broker.addConnector("ws://localhost:61614");
		
		TransportConnector stomp = new TransportConnector();
		stomp.setUri(new URI("stomp+nio://0.0.0.0:"+Util.getConfigPropertyInt(ConfigConstants.STOMP_PORT)+"?transport.hbGracePeriodMultiplier=1.5"));
		broker.addConnector(stomp);
		
		TransportConnector mqtt = new TransportConnector();
		mqtt.setUri(new URI("mqtt+nio://0.0.0.0:"+Util.getConfigPropertyInt(ConfigConstants.MQTT_PORT)));
		broker.addConnector(mqtt);
		
		List<BrokerPlugin> plugins = new ArrayList<BrokerPlugin>();
		BrokerPlugin[] pluginArray = broker.getPlugins();
		if(pluginArray!=null){
			plugins.addAll(Arrays.asList(pluginArray));
		}
		plugins.add(new ActiveMqSubscriptionPlugin());
		broker.setPlugins(plugins.toArray(new BrokerPlugin[0]));
		
		broker.setPersistent(false);
		
		configureBroker(broker);
//"activemq:topic:ActiveMQ.Advisory.Connection?mapJmsMessage=false"
		final ActiveMQTopic topic = new ActiveMQTopic("ActiveMQ.Advisory.Connection");
		broker.setDestinations(new ActiveMQDestination[] { topic });

		final ManagementContext managementContext = new ManagementContext();
		managementContext.setCreateConnector(true);
		broker.setManagementContext(managementContext);
		
		return broker;
	}
	
	private static void configureBroker(BrokerService broker) {
		SystemUsage usage = new SystemUsage();
		MemoryUsage mem = new MemoryUsage();
		mem.setLimit(16*1024*1024);
		usage.setMemoryUsage(mem);
		broker.setSystemUsage(usage);
		//DLQ strategy
		
        PolicyMap map = new PolicyMap();

        map.put(new ActiveMQQueue(">"), queuePolicy(">"));
        map.put(new ActiveMQTopic(">"), topicPolicy(">"));

        PolicyEntry qEntry = new PolicyEntry();
        qEntry.setQueue("signalk.>");

        IndividualDeadLetterStrategy strategy = new IndividualDeadLetterStrategy();
        strategy.setQueuePrefix("DLQ.");
        strategy.setUseQueueForQueueMessages(true);
        strategy.setProcessExpired(false);
        strategy.setProcessNonPersistent(true);
        qEntry.setDeadLetterStrategy(strategy);
        
        PrefetchRatePendingMessageLimitStrategy preFetchRate = new PrefetchRatePendingMessageLimitStrategy();
        preFetchRate.setMultiplier(2.0);
        qEntry.setPendingMessageLimitStrategy(preFetchRate);
        qEntry.setMessageEvictionStrategy(new OldestMessageEvictionStrategy());
        map.put(new ActiveMQQueue("signalk.>"), qEntry);

        broker.setDestinationPolicy(map);
    }

    private static PolicyEntry topicPolicy(String name) {
        PolicyEntry rc = new PolicyEntry();
        rc.setTopic(name);
        rc.setProducerFlowControl(false);
        return rc;
    }

    private static PolicyEntry queuePolicy(String name) {
        PolicyEntry rc = new PolicyEntry();
        rc.setQueue(name);
        rc.setProducerFlowControl(false);
        return rc;
    }
	
	public static ActiveMQComponent newAMQInstance(){
		 ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		    connectionFactory.setBrokerURL("vm:localhost");
		    // use a pooled connection factory between the module and the queue
		    PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(connectionFactory);

		    // how many connections should there be in the session pool?
		    pooledConnectionFactory.setMaxConnections(100);
		    pooledConnectionFactory.setMaximumActiveSessionPerConnection(100);
		    pooledConnectionFactory.setCreateConnectionOnStartup(true);
		    pooledConnectionFactory.setBlockIfSessionPoolIsFull(false);

		    JmsConfiguration jmsConfiguration = new JmsConfiguration(pooledConnectionFactory);
		    jmsConfiguration.setDeliveryPersistent(false);
		    jmsConfiguration.setTimeToLive(1000*10);
		    ActiveMQComponent activeMQComponent = ActiveMQComponent.activeMQComponent("vm:localhost");
		    return activeMQComponent;
	}
}
