package nz.co.fortytwo.signalk.server;

import static org.junit.Assert.*;
import mjson.Json;
import nz.co.fortytwo.signalk.model.impl.SignalKModelImpl;
import nz.co.fortytwo.signalk.server.util.Util;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SignalkDiffProcessorTest {

	String jsonDiff = "{\"context\": \"vessels.motu.navigation\",\"updates\":[{\"source\": {\"device\" : \"/dev/actisense\",\"timestamp\":\"2014-08-15T16:00:00.081+00:00\",\"src\":\"115\",\"pgn\":\"128267\"},\"values\": [{ \"path\": \"courseOverGroundTrue\",\"value\": 172.9 },{ \"path\": \"speedOverGround\",\"value\": 3.85 }]}]}";
	String jsonDiff1 = "{\"context\": \"vessels.motu\",\"updates\":[{\"source\": {\"device\" : \"/dev/actisense\", \"timestamp\":\"2014-08-15T16:00:00.081+00:00\",\"src\":\"115\",\"pgn\":\"128267\"},\"values\": [{ \"path\": \"navigation.courseOverGroundTrue\",\"value\": 172.9 },{ \"path\": \"navigation.speedOverGround\",\"value\": 3.85 }]}]}";
	String jsonDiff2 = "{\"context\": \"vessels.motu.navigation\",\"updates\":[{\"source\": {\"device\" : \"/dev/actisense\",\"timestamp\":\"2014-08-15T16:00:00.081+00:00\",\"src\":\"115\",\"pgn\":\"128267\"},\"values\": [{ \"path\": \"courseOverGroundTrue\",\"value\": 172.9 },{ \"path\": \"speedOverGround\",\"value\": 3.85 }]},{\"source\": {\"device\" : \"/dev/ttyUSB0\",\"timestamp\":\"2014-08-15T16:00:00.081+00:00\",\"src\":\"115\",\"pgn\":\"128267\"},\"values\": [{ \"path\": \"courseOverGroundMagnetic\",\"value\": 152.9 },{ \"path\": \"speedOverWater\",\"value\": 2.85 }]}]}";
	private static Logger logger = Logger.getLogger(SignalKModelImpl.class);
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldProcessDiff() {
		Json diff = Json.read(jsonDiff);
		SignalkDiffProcessor processor = new SignalkDiffProcessor();
		Json output = processor.handle(diff);
		logger.debug(output);
		assertEquals(172.9, output.at(VESSELS).at("motu").at(navigation).at("courseOverGroundTrue").at(VALUE).asDouble(),001);
		assertEquals("2014-08-15T16:00:00.081Z", output.at(VESSELS).at("motu").at(navigation).at("courseOverGroundTrue").at(TIMESTAMP).asString());
		assertEquals("/dev/actisense-N2K-115-128267", output.at(VESSELS).at("motu").at(navigation).at("courseOverGroundTrue").at(SOURCE).asString());
		
		assertEquals(3.85, output.at(VESSELS).at("motu").at(navigation).at("speedOverGround").at(VALUE).asDouble(),001);
		assertEquals("2014-08-15T16:00:00.081Z", output.at(VESSELS).at("motu").at(navigation).at("speedOverGround").at(TIMESTAMP).asString());
		assertEquals("/dev/actisense-N2K-115-128267", output.at(VESSELS).at("motu").at(navigation).at("speedOverGround").at(SOURCE).asString());
	}

	@Test
	public void shouldIgnoreSignalKJson() {
		Json diff = Util.getEmptyRootNode();
		SignalkDiffProcessor processor = new SignalkDiffProcessor();
		Json output = processor.handle(diff);
		logger.debug(output);
		assertEquals(diff.toString(), output.toString());
	}
	@Test
	public void shouldIgnoreRandomJson() {
		Json diff = Json.read("{\"headingTrue\": {\"value\": 23,\"source\": \"self\",\"timestamp\": \"2014-03-24T00: 15: 41Z\" }}");
		SignalkDiffProcessor processor = new SignalkDiffProcessor();
		Json output = processor.handle(diff);
		logger.debug(output);
		assertEquals(diff.toString(), output.toString());
	}
	@Test
	public void shouldProcessComplexDiff() {
		Json diff = Json.read(jsonDiff1);
		SignalkDiffProcessor processor = new SignalkDiffProcessor();
		Json output = processor.handle(diff);
		logger.debug(output);
		assertEquals(172.9, output.at(VESSELS).at("motu").at(navigation).at("courseOverGroundTrue").at(VALUE).asDouble(),001);
		assertEquals("2014-08-15T16:00:00.081Z", output.at(VESSELS).at("motu").at(navigation).at("courseOverGroundTrue").at(TIMESTAMP).asString());
		assertEquals("/dev/actisense-N2K-115-128267", output.at(VESSELS).at("motu").at(navigation).at("courseOverGroundTrue").at(SOURCE).asString());
		
		assertEquals(3.85, output.at(VESSELS).at("motu").at(navigation).at("speedOverGround").at(VALUE).asDouble(),001);
		assertEquals("2014-08-15T16:00:00.081Z", output.at(VESSELS).at("motu").at(navigation).at("speedOverGround").at(TIMESTAMP).asString());
		assertEquals("/dev/actisense-N2K-115-128267", output.at(VESSELS).at("motu").at(navigation).at("speedOverGround").at(SOURCE).asString());
	}
	
	@Test
	public void shouldProcessDiffArray() {
		Json diff = Json.read(jsonDiff2);
		SignalkDiffProcessor processor = new SignalkDiffProcessor();
		Json output = processor.handle(diff);
		logger.debug(output);
		assertEquals(172.9, output.at(VESSELS).at("motu").at(navigation).at("courseOverGroundTrue").at(VALUE).asDouble(),001);
		assertEquals("2014-08-15T16:00:00.081Z", output.at(VESSELS).at("motu").at(navigation).at("courseOverGroundTrue").at(TIMESTAMP).asString());
		assertEquals("/dev/actisense-N2K-115-128267", output.at(VESSELS).at("motu").at(navigation).at("courseOverGroundTrue").at(SOURCE).asString());
		
		assertEquals(3.85, output.at(VESSELS).at("motu").at(navigation).at("speedOverGround").at(VALUE).asDouble(),001);
		assertEquals("2014-08-15T16:00:00.081Z", output.at(VESSELS).at("motu").at(navigation).at("speedOverGround").at(TIMESTAMP).asString());
		assertEquals("/dev/actisense-N2K-115-128267", output.at(VESSELS).at("motu").at(navigation).at("speedOverGround").at(SOURCE).asString());
		
		assertEquals(152.9, output.at(VESSELS).at("motu").at(navigation).at("courseOverGroundMagnetic").at(VALUE).asDouble(),001);
		assertEquals("2014-08-15T16:00:00.081Z", output.at(VESSELS).at("motu").at(navigation).at("courseOverGroundMagnetic").at(TIMESTAMP).asString());
		assertEquals("/dev/ttyUSB0-N2K-115-128267", output.at(VESSELS).at("motu").at(navigation).at("courseOverGroundMagnetic").at(SOURCE).asString());
		
		assertEquals(2.85, output.at(VESSELS).at("motu").at(navigation).at("speedOverWater").at(VALUE).asDouble(),001);
		assertEquals("2014-08-15T16:00:00.081Z", output.at(VESSELS).at("motu").at(navigation).at("speedOverWater").at(TIMESTAMP).asString());
		assertEquals("/dev/ttyUSB0-N2K-115-128267", output.at(VESSELS).at("motu").at(navigation).at("speedOverWater").at(SOURCE).asString());
	}
}
