package nz.co.fortytwo.freeboard.server.util;

import static org.junit.Assert.*;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.*;
import mjson.Json;

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
		Json root = Util.getEmptyRootNode();
		//Json position = Util.addNode(root, VESSELS+"."+SELF+"."+nav_anchor_position_latitude);
		//System.out.println(position);
		System.out.println(Util.putWith(root,VESSELS+"."+SELF+"."+nav_anchor_position_latitude, 23.07d, "nmea"));
		System.out.println(root);
		assertEquals(Util.findValue(root,VESSELS+"."+SELF+"."+nav_anchor_position_latitude).asDouble(), 023.07d, 0.0002);
	}

}
