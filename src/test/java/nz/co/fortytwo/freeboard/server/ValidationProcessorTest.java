package nz.co.fortytwo.freeboard.server;

import static org.junit.Assert.*;
import mjson.Json;

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
		assertNull(wind.at("speedAlarm").at("timestamp"));
		validationProcessor.validate(wind);
		assertNotNull(wind.at("speedAlarm").at("timestamp"));
		logger.debug(wind);
	}
	@Test
	public void shouldNotAddTimestamp() {
		ValidationProcessor validationProcessor = new ValidationProcessor();
		Json wind = Json.read("{\"speedAlarm\": {\"value\":0.0000000000,\"timestamp\":\"2014-10-22T21:32:43.313+13:00\",\"source\":\"unknown\"},\"directionChangeAlarm\": {\"value\":0.0000000000},\"directionApparent\": {\"value\":0.0000000000},\"directionTrue\": {\"value\":0.0000000000},\"speedApparent\": {\"value\":0.0000000000},\"speedTrue\": {\"value\":7.68}}");
		assertNotNull(wind.at("speedAlarm").at("timestamp"));
		assertNull(wind.at("directionChangeAlarm").at("timestamp"));
		validationProcessor.validate(wind);
		assertNotNull(wind.at("speedAlarm").at("timestamp"));
		assertNotNull(wind.at("directionChangeAlarm").at("timestamp"));
		logger.debug(wind);
	}
	
	
}
