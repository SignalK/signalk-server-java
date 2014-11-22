package nz.co.fortytwo.signalk.server;

import static org.junit.Assert.*;
import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.util.JsonConstants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeclinationProcessorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldGetDeclination() {
		DeclinationProcessor p = new DeclinationProcessor();
		SignalKModel model = SignalKModelFactory.getInstance();
		model.putWith(model.self(), JsonConstants.nav_position_latitude, -41.5);
		model.putWith(model.self(), JsonConstants.nav_position_longitude, 172.5);
		p.handle();
		double decl = model.findValue(model.self(),JsonConstants.nav_magneticVariation).asDouble();
		assertEquals(22.1, decl, 001);
	}
	
	@Test
	public void shouldNotGetDeclination() {
		DeclinationProcessor p = new DeclinationProcessor();
		SignalKModel model = SignalKModelFactory.getInstance();
		model.putWith(model.self(), JsonConstants.nav_position_latitude, -41.5);
		//model.putWith(model.self(), JsonConstants.nav_position_longitude, 172.5);
		p.handle();
		Json decl = model.findValue(model.self(),JsonConstants.nav_magneticVariation);
		assertNull( decl);
	}

}
