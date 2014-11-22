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
package nz.co.fortytwo.signalk.server;

import static org.junit.Assert.*;
import mjson.Json;
import nz.co.fortytwo.signalk.server.ValidationProcessor;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ValidationProcessorTest {

	private static Logger logger = Logger.getLogger(ValidationProcessorTest.class);
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldAddTimestamp() {
		ValidationProcessor validationProcessor = new ValidationProcessor();
		Json wind = Json.read("{\"speedAlarm\": {\"value\":0.0000000000},\"directionChangeAlarm\": {\"value\":0.0000000000},\"directionApparent\": {\"value\":0.0000000000},\"directionTrue\": {\"value\":0.0000000000},\"speedApparent\": {\"value\":0.0000000000},\"speedTrue\": {\"value\":7.68}}");
		assertNull(wind.at("speedAlarm").at(TIMESTAMP));
		validationProcessor.validate(wind);
		assertNotNull(wind.at("speedAlarm").at(TIMESTAMP));
		logger.debug(wind);
	}
	@Test
	public void shouldNotAddTimestamp() {
		ValidationProcessor validationProcessor = new ValidationProcessor();
		Json wind = Json.read("{\"speedAlarm\": {\"value\":0.0000000000,\"timestamp\":\"2014-10-22T21:32:43.313+13:00\",\"source\":\"unknown\"},\"directionChangeAlarm\": {\"value\":0.0000000000},\"directionApparent\": {\"value\":0.0000000000},\"directionTrue\": {\"value\":0.0000000000},\"speedApparent\": {\"value\":0.0000000000},\"speedTrue\": {\"value\":7.68}}");
		assertNotNull(wind.at("speedAlarm").at(TIMESTAMP));
		assertNull(wind.at("directionChangeAlarm").at(TIMESTAMP));
		validationProcessor.validate(wind);
		assertNotNull(wind.at("speedAlarm").at(TIMESTAMP));
		assertNotNull(wind.at("directionChangeAlarm").at(TIMESTAMP));
		logger.debug(wind);
	}
	
	@Test
	public void shouldAddSource() {
		ValidationProcessor validationProcessor = new ValidationProcessor();
		Json wind = Json.read("{\"speedAlarm\": {\"value\":0.0000000000},\"directionChangeAlarm\": {\"value\":0.0000000000},\"directionApparent\": {\"value\":0.0000000000},\"directionTrue\": {\"value\":0.0000000000},\"speedApparent\": {\"value\":0.0000000000},\"speedTrue\": {\"value\":7.68}}");
		assertNull(wind.at("speedAlarm").at(SOURCE));
		validationProcessor.validate(wind);
		assertNotNull(wind.at("speedAlarm").at(SOURCE));
		logger.debug(wind);
	}
	@Test
	public void shouldNotAddSource() {
		ValidationProcessor validationProcessor = new ValidationProcessor();
		Json wind = Json.read("{\"speedAlarm\": {\"value\":0.0000000000,\"timestamp\":\"2014-10-22T21:32:43.313+13:00\",\"source\":\"unknown\"},\"directionChangeAlarm\": {\"value\":0.0000000000},\"directionApparent\": {\"value\":0.0000000000},\"directionTrue\": {\"value\":0.0000000000},\"speedApparent\": {\"value\":0.0000000000},\"speedTrue\": {\"value\":7.68}}");
		assertNotNull(wind.at("speedAlarm").at(SOURCE));
		assertNull(wind.at("directionChangeAlarm").at(SOURCE));
		validationProcessor.validate(wind);
		assertNotNull(wind.at("speedAlarm").at(SOURCE));
		assertNotNull(wind.at("directionChangeAlarm").at(SOURCE));
		logger.debug(wind);
	}
	
	
}
