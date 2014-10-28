package nz.co.fortytwo.signalk.server.util;

import static nz.co.fortytwo.signalk.server.util.JsonConstants.SELF;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.VESSELS;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.nav_anchor_position_latitude;
import static org.junit.Assert.assertEquals;
import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldMakeNode() {
		SignalKModel signalkModel = SignalKModelFactory.getInstance();
		Json root = signalkModel.getEmptyRootNode();
		//Json position = signalkModel.addNode(root, VESSELS+"."+SELF+"."+nav_anchor_position_latitude);
		//System.out.println(position);
		System.out.println(signalkModel.putWith(root,VESSELS+"."+SELF+"."+nav_anchor_position_latitude, 23.07d, "nmea"));
		System.out.println(root);
		assertEquals(signalkModel.findValue(root,VESSELS+"."+SELF+"."+nav_anchor_position_latitude).asDouble(), 023.07d, 0.0002);
	}

}
