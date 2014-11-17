package nz.co.fortytwo.signalk.server;

import static org.junit.Assert.*;

import java.util.List;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.event.JsonEvent;
import nz.co.fortytwo.signalk.model.event.JsonEvent.EventType;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.model.impl.SignalKModelImpl;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.*;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

public class DeltaExportProcessorTest {
	private static Logger logger = Logger.getLogger(DeltaExportProcessorTest.class);
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void shouldHandleEventsFromBus() throws Exception {
		
		DeltaExportProcessor processor = new DeltaExportProcessor();
		assertEquals(0, processor.map.size());
		EventBus bus = new EventBus();
		bus.register(processor);
		
		JsonEvent event = new JsonEvent(getJsonForEvent(), EventType.EDIT);
		bus.post(event);
		assertEquals(1, processor.map.size());
	
	}

	@Test
	public void shouldCreateDelta() throws Exception {
		DeltaExportProcessor processor = new DeltaExportProcessor();
		assertEquals(0, processor.map.size());
		EventBus bus = new EventBus();
		bus.register(processor);
		
		JsonEvent event = new JsonEvent(getJsonForEvent(), EventType.EDIT);
		bus.post(event);
		assertEquals(1, processor.map.size());
		CamelContext ctx = new DefaultCamelContext(); 
		Exchange ex = new DefaultExchange(ctx);
		processor.process(ex);
		assertEquals(0, processor.map.size());
		assertNotNull(ex.getOut());
		List<?> delta = ex.getIn().getBody(List.class);
		logger.debug(delta.toString());
		String expected = "[{\"updates\":[{\"values\":[{\"value\":172.9,\"path\":\"navigation.courseOverGroundTrue\"}],\"source\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\"}}],\"context\":\"vessels.self\"}]";
		assertEquals(expected, delta.toString());
	}
	
	@Test
	public void shouldCreateTwoDeltas() {
		
	}
	
	private Json getJsonForEvent(){
		SignalKModel model = SignalKModelFactory.getCleanInstance();
		Json json = Json.read("{\"vessels\":{\"self\":{\"navigation\":{\"courseOverGroundTrue\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\",\"value\":172.9},\"speedOverGround\":{\"timestamp\":\"2014-08-15T16:00:00.081Z\",\"source\":\"/dev/actisense-N2K-115-128267\",\"value\":3.85}}}}}");
		model.merge(json);
		return model.atPath(VESSELS,SELF,nav_courseOverGroundTrue);
	}
	
}
