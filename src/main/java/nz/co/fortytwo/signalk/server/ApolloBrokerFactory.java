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

import java.io.File;

import org.apache.activemq.apollo.broker.Broker;
import org.apache.activemq.apollo.dto.AcceptingConnectorDTO;
import org.apache.activemq.apollo.dto.BrokerDTO;
import org.apache.activemq.apollo.dto.QueueDTO;
import org.apache.activemq.apollo.dto.VirtualHostDTO;
import org.apache.activemq.apollo.dto.WebAdminDTO;
import org.apache.activemq.apollo.stomp.dto.StompDTO;



public class ApolloBrokerFactory {
	
	public static Broker newInstance() throws Exception {
		final Broker broker = new Broker();
		
		// Configure STOMP over TCP connector
		final AcceptingConnectorDTO tcp = new AcceptingConnectorDTO();
		tcp.id = "tcp";
		tcp.bind = "tcp://localhost:61613";		
		tcp.protocols.add( new StompDTO() );
		tcp.connection_limit=100;
		
		
		// Configure STOMP over WebSockects connector
		final AcceptingConnectorDTO ws = new AcceptingConnectorDTO();
		ws.id = "ws";
		ws.bind = "ws://localhost:61614";		
		ws.protocols.add( new StompDTO() );
		ws.connection_limit=100;
		// Create a std queues with name 'subscribe, unsubscribe, get, list'
		final QueueDTO subscribe = new QueueDTO();
		subscribe.id = "subscribe";
		
		
		// Create virtual host (based on localhost)
		final VirtualHostDTO host = new VirtualHostDTO();
		host.id = "localhost";		
		host.queues.add( subscribe );
		host.host_names.add( "localhost" );
		host.host_names.add( "127.0.0.1" );
		host.auto_create_destinations = true;
		
		// Create a web admin UI (REST) accessible at: http://localhost:61680/api/index.html#!/ 
        final WebAdminDTO webadmin = new WebAdminDTO();
        webadmin.bind = "http://localhost:61680";

		// Finally, glue all together inside broker configuration
		final BrokerDTO config = new BrokerDTO();
		config.connectors.add( tcp );
		config.connectors.add( ws );
		config.virtual_hosts.add( host );
		config.web_admins.add( webadmin );
		
		
		broker.setConfig( config );
		broker.setTmp( new File( System.getProperty( "java.io.tmpdir" ) ) );
		
		broker.start( new Runnable() {			
			@Override
			public void run() {		
				System.out.println("The broker has been started.");
			}
		} );
		
		return broker;
	}
}

