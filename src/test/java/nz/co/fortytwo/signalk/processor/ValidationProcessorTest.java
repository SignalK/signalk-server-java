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
package nz.co.fortytwo.signalk.processor;

import static nz.co.fortytwo.signalk.util.SignalKConstants.dot;
import static nz.co.fortytwo.signalk.util.SignalKConstants.env_wind_directionChangeAlarm;
import static nz.co.fortytwo.signalk.util.SignalKConstants.env_wind_speedAlarm;
import static nz.co.fortytwo.signalk.util.SignalKConstants.source;
import static nz.co.fortytwo.signalk.util.SignalKConstants.timestamp;
import static nz.co.fortytwo.signalk.util.SignalKConstants.value;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels_dot_self_dot;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.RouteManagerFactory;
import nz.co.fortytwo.signalk.util.JsonSerializer;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ValidationProcessorTest {

	private static Logger logger = Logger.getLogger(ValidationProcessorTest.class);
	private JsonSerializer ser = new JsonSerializer();
	@Before
	public void setUp() throws Exception {
		RouteManagerFactory.getMotuTestInstance();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldAddTimestamp() throws IOException {
		ValidationProcessor validationProcessor = new ValidationProcessor();
		//{\"vessels\":{\""+SignalKConstants.self+"\":{\"environment\":{\"wind\":
		SignalKModel wind = SignalKModelFactory.getCleanInstance();
		wind.put(vessels_dot_self_dot+env_wind_speedAlarm+dot+source,"unknown");
		//wind.put(vessels_dot_self_dot+env_wind_speedAlarm+dot+timestamp,"2015-03-16T03:31:22.327Z");
		wind.put(vessels_dot_self_dot+env_wind_speedAlarm+dot+value,0d);
		
		assertNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+timestamp));
		validationProcessor.validate(wind);
		
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+timestamp));
		logger.debug(wind);
	}
	@Test
	public void shouldNotAddTimestamp() throws IOException{
		ValidationProcessor validationProcessor = new ValidationProcessor();
		SignalKModel wind = SignalKModelFactory.getCleanInstance();
		wind.put(vessels_dot_self_dot+env_wind_speedAlarm+dot+source,"unknown");
		wind.put(vessels_dot_self_dot+env_wind_speedAlarm+dot+timestamp,"2015-03-16T03:31:22.327Z");
		wind.put(vessels_dot_self_dot+env_wind_speedAlarm+dot+value,0d);
		wind.put(vessels_dot_self_dot+env_wind_directionChangeAlarm+dot+source,"unknown");
		//wind.put(vessels_dot_self_dot+env_wind_directionChangeAlarm+dot+timestamp,"2015-03-16T03:31:22.326Z");
		wind.put(vessels_dot_self_dot+env_wind_directionChangeAlarm+dot+value,0d);
		
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+timestamp));
		assertNull(wind.get(vessels_dot_self_dot+env_wind_directionChangeAlarm+dot+timestamp));
		validationProcessor.validate(wind);
		logger.debug(wind);
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+timestamp));
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_directionChangeAlarm+dot+timestamp));
		logger.debug(wind);
	}
	
	@Test
	public void shouldAddSource()throws IOException {
		ValidationProcessor validationProcessor = new ValidationProcessor();
		
		SignalKModel wind = SignalKModelFactory.getCleanInstance();
		//wind.put(vessels_dot_self_dot+env_wind_speedAlarm+dot+source,"unknown");
		wind.put(vessels_dot_self_dot+env_wind_speedAlarm+dot+timestamp,"2015-03-16T03:31:22.327Z");
		wind.put(vessels_dot_self_dot+env_wind_speedAlarm+dot+value,0d);
		
		assertNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+source));
		validationProcessor.validate(wind);
		logger.debug(wind);
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+source));
		logger.debug(wind);
	}
	@Test
	public void shouldNotAddSource() throws IOException{
		ValidationProcessor validationProcessor = new ValidationProcessor();
		SignalKModel wind = SignalKModelFactory.getCleanInstance();
		wind.put(vessels_dot_self_dot+env_wind_speedAlarm+dot+source,"unknown");
		wind.put(vessels_dot_self_dot+env_wind_speedAlarm+dot+timestamp,"2015-03-16T03:31:22.327Z");
		wind.put(vessels_dot_self_dot+env_wind_speedAlarm+dot+value,0d);
		//wind.put(vessels_dot_self_dot+env_wind_directionChangeAlarm+dot+source,"unknown");
		wind.put(vessels_dot_self_dot+env_wind_directionChangeAlarm+dot+timestamp,"2015-03-16T03:31:22.326Z");
		wind.put(vessels_dot_self_dot+env_wind_directionChangeAlarm+dot+value,0d);
		
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+source));
		assertNull(wind.get(vessels_dot_self_dot+env_wind_directionChangeAlarm+dot+source));
		validationProcessor.validate(wind);
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+source));
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_directionChangeAlarm+dot+source));
		logger.debug(wind);
	}
	
	
}
