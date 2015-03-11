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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.NavigableMap;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.processor.ValidationProcessor;
import nz.co.fortytwo.signalk.server.RouteManagerFactory;
import nz.co.fortytwo.signalk.util.JsonSerializer;
import static nz.co.fortytwo.signalk.util.JsonConstants.*;
import static nz.co.fortytwo.signalk.util.SignalKConstants.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ValidationProcessorTest {

	private static Logger logger = Logger.getLogger(ValidationProcessorTest.class);
	private JsonSerializer ser = new JsonSerializer();
	@Before
	public void setUp() throws Exception {
		RouteManagerFactory.getInstance(null);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldAddTimestamp() throws IOException {
		ValidationProcessor validationProcessor = new ValidationProcessor();
		//{\"vessels\":{\""+self+"\":{\"environment\":{\"wind\":
		SignalKModel wind = SignalKModelFactory.getWrappedInstance(ser.read("{\"vessels\":{\""+self+"\":{\"environment\":{\"wind\":{\"speedAlarm\": {\"value\":0.0000000000},\"directionChangeAlarm\": {\"value\":0.0000000000},\"angleApparent\": {\"value\":0.0000000000},\"directionTrue\": {\"value\":0.0000000000},\"speedApparent\": {\"value\":0.0000000000},\"speedTrue\": {\"value\":7.68}}}}}}"));
		assertNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+TIMESTAMP));
		validationProcessor.validate(wind);
		
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+TIMESTAMP));
		logger.debug(wind);
	}
	@Test
	public void shouldNotAddTimestamp() throws IOException{
		ValidationProcessor validationProcessor = new ValidationProcessor();
		SignalKModel wind = SignalKModelFactory.getWrappedInstance(ser.read("{\"vessels\":{\""+self+"\":{\"environment\":{\"wind\":{\"speedAlarm\": {\"value\":0.0000000000,\"timestamp\":\"2014-10-22T21:32:43.313+13:00\",\"source\":\"unknown\"},\"directionChangeAlarm\": {\"value\":0.0000000000},\"angleApparent\": {\"value\":0.0000000000},\"directionTrue\": {\"value\":0.0000000000},\"speedApparent\": {\"value\":0.0000000000},\"speedTrue\": {\"value\":7.68}}}}}}"));
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+TIMESTAMP));
		assertNull(wind.get(vessels_dot_self_dot+env_wind_directionChangeAlarm+dot+TIMESTAMP));
		validationProcessor.validate(wind);
		logger.debug(wind);
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+TIMESTAMP));
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_directionChangeAlarm+dot+TIMESTAMP));
		logger.debug(wind);
	}
	
	@Test
	public void shouldAddSource()throws IOException {
		ValidationProcessor validationProcessor = new ValidationProcessor();
		SignalKModel wind = SignalKModelFactory.getWrappedInstance(ser.read("{\"vessels\":{\""+self+"\":{\"environment\":{\"wind\":{\"speedAlarm\": {\"value\":0.0000000000},\"directionChangeAlarm\": {\"value\":0.0000000000},\"angleApparent\": {\"value\":0.0000000000},\"directionTrue\": {\"value\":0.0000000000},\"speedApparent\": {\"value\":0.0000000000},\"speedTrue\": {\"value\":7.68}}}}}}"));
		assertNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+SOURCE));
		validationProcessor.validate(wind);
		logger.debug(wind);
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+SOURCE));
		logger.debug(wind);
	}
	@Test
	public void shouldNotAddSource() throws IOException{
		ValidationProcessor validationProcessor = new ValidationProcessor();
		SignalKModel wind = SignalKModelFactory.getWrappedInstance(ser.read("{\"vessels\":{\""+self+"\":{\"environment\":{\"wind\":{\"speedAlarm\": {\"value\":0.0000000000,\"timestamp\":\"2014-10-22T21:32:43.313+13:00\",\"source\":\"unknown\"},\"directionChangeAlarm\": {\"value\":0.0000000000},\"angleApparent\": {\"value\":0.0000000000},\"directionTrue\": {\"value\":0.0000000000},\"speedApparent\": {\"value\":0.0000000000},\"speedTrue\": {\"value\":7.68}}}}}}"));
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+SOURCE));
		assertNull(wind.get(vessels_dot_self_dot+env_wind_directionChangeAlarm+dot+SOURCE));
		validationProcessor.validate(wind);
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_speedAlarm+dot+SOURCE));
		assertNotNull(wind.get(vessels_dot_self_dot+env_wind_directionChangeAlarm+dot+SOURCE));
		logger.debug(wind);
	}
	
	
}
