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

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.processor.DeclinationProcessor;
import nz.co.fortytwo.signalk.processor.WindProcessor;
import nz.co.fortytwo.signalk.server.util.Util;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.log4j.Logger;

public abstract class SignalKCamelTestSupport extends CamelTestSupport {
	static Logger logger = Logger.getLogger(SignalKNmeaReceiverTest.class);
	protected SignalKModel signalkModel = null;
	protected RouteManager routeManager = null;
	protected final CountDownLatch latch = new CountDownLatch(1);

	public SignalKCamelTestSupport() {
		super();
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
	    try {
	    	try {
				Properties config=Util.getConfig(null);
			
				routeManager=new RouteManager(config){
					@Override
					public void configure() throws Exception {
						configureRouteBuilder(this);
					
					}
				};
				//must do this early!
				CamelContextFactory.setContext(routeManager);
				RouteManagerFactory.manager=routeManager;
	    	} catch (Exception e) {
				logger.error(e);
			} 
			signalkModel=SignalKModelFactory.getInstance();

			return routeManager;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	    return null;
	}
	public abstract void configureRouteBuilder(RouteBuilder routeBuilder);

}