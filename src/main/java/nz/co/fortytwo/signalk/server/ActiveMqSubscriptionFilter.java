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

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.RemoveSubscriptionInfo;
import org.apache.activemq.util.IntrospectionSupport;
import org.apache.log4j.Logger;

public class ActiveMqSubscriptionFilter extends BrokerFilter{

	private static Logger logger = Logger.getLogger(ActiveMqSubscriptionFilter.class);
	
	public ActiveMqSubscriptionFilter(Broker next) {
		super(next);
	}

	@Override
	public Subscription addConsumer(ConnectionContext context, ConsumerInfo info) throws Exception {
		if(logger.isDebugEnabled())logger.debug("Adding subscription:"+IntrospectionSupport.toString(info));
		return super.addConsumer(context, info);
	}

	@Override
	public void removeSubscription(ConnectionContext context, RemoveSubscriptionInfo info) throws Exception {
		if(logger.isDebugEnabled())logger.debug("Remove subscription:"+IntrospectionSupport.toString(info));
		super.removeSubscription(context, info);
	}

	@Override
	public void removeConsumer(ConnectionContext context, ConsumerInfo info) throws Exception {
		if(logger.isDebugEnabled())logger.debug("Remove consumer:"+IntrospectionSupport.toString(info));
		super.removeConsumer(context, info);
	}
	
	

}
