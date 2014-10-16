package nz.co.fortytwo.freeboard.server;

import static nz.co.fortytwo.freeboard.server.util.JsonConstants.*;
import static org.junit.Assert.*;
import nz.co.fortytwo.freeboard.server.util.Util;
import nz.co.fortytwo.freeboard.signalk.SignalKModel;
import nz.co.fortytwo.freeboard.signalk.SignalkRouteFactory;
import nz.co.fortytwo.freeboard.signalk.impl.SignalKModelFactory;
import nz.co.fortytwo.freeboard.signalk.impl.SignalKModelImpl;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.log4j.Logger;
import org.junit.Test;

public class SignalKReceiverTest extends CamelTestSupport {
 
    private static Logger logger = Logger.getLogger(NMEAProcessorTest.class);
	private SignalKModel signalkModel=SignalKModelFactory.getInstance();


	private DeclinationProcessor declinationProcessor;

	private WindProcessor windProcessor = new WindProcessor();
	//private GPXProcessor gpxProcessor;

	
	@Produce(uri = "direct:input")
    protected ProducerTemplate template;
	


	@Test
    public void shouldProcessMessage() throws Exception {
        assertNotNull(template);
        template.sendBody("direct:input","$GPRMC,144629.20,A,5156.91111,N,00434.80385,E,0.295,,011113,,,A*78");
        
		 logger.debug(signalkModel);
		 assertEquals(51.9485185d,Util.findValue(signalkModel.atPath(VESSELS,SELF), nav_position_latitude).asDouble(),0.00001);
		 logger.debug("Lat :"+Util.findValue(signalkModel.atPath(VESSELS,SELF), nav_position_latitude));
 
      
    }
	
	@Test
    public void shouldProcessAisMessage() throws Exception {
        assertNotNull(template);
        template.sendBody("direct:input","!AIVDM,1,1,,B,15MwkRUOidG?GElEa<iQk1JV06Jd,0*6D");
        
		 logger.debug(signalkModel);
		 assertNotNull(signalkModel.atPath(VESSELS,"366998410"));
		 assertEquals(37.8251d,Util.findValue(signalkModel.atPath(VESSELS,"366998410"), nav_position_latitude).asDouble(),0.001);
		 logger.debug("Lat :"+Util.findValue(signalkModel.atPath(VESSELS,SELF), nav_position_latitude));
    }
	@Test
    public void shouldProcessTwoMessages() throws Exception {
        assertNotNull(template);
        String jStr = "{\"vessels\":{\"self\":{\"environment\":{\"wind\":{\"directionApparent\":{\"value\":90.0000000000},\"directionTrue\":{\"value\":0.0000000000},\"speedApparent\":{\"value\":20.0000000000},\"speedTrue\":{\"value\":0.0000000000}}}}}}";
        template.sendBody("direct:input","$GPRMC,144629.20,A,5156.91111,N,00434.80385,E,0.295,,011113,,,A*78");
        template.sendBody("direct:input",jStr);
        
		 logger.debug(signalkModel);
		
		 assertEquals(51.9485185d,Util.findValue(signalkModel.atPath(VESSELS,SELF), nav_position_latitude).asDouble(),0.00001);
		 assertEquals(20.0d,Util.findValue(signalkModel.atPath(VESSELS,SELF),env_wind_speedApparent ).asDouble(),0.00001);
      
    }
	
	@Test
    public void shouldProcessWindTrue() throws Exception {
        assertNotNull(template);
        String jStr = "{\"vessels\":{\"self\":{\"environment\":{\"wind\":{\"directionApparent\":{\"value\":90.0000000000},\"directionTrue\":{\"value\":0.0000000000},\"speedApparent\":{\"value\":20.0000000000},\"speedTrue\":{\"value\":0.0000000000}}}}}}";
        template.sendBody("direct:input","$GPRMC,144629.20,A,5156.91111,N,00434.80385,E,0.295,,011113,,,A*78");
        template.sendBody("direct:input",jStr);
        
		 logger.debug(signalkModel);
		
		 assertEquals(51.9485185d,Util.findValue(signalkModel.atPath(VESSELS,SELF), nav_position_latitude).asDouble(),0.00001);
		 assertEquals(20.0d,Util.findValue(signalkModel.atPath(VESSELS,SELF),env_wind_speedApparent ).asDouble(),0.00001);
		 windProcessor.handle();
		 assertEquals(20.0d,Util.findValue(signalkModel.atPath(VESSELS,SELF),env_wind_speedTrue ).asDouble(),0.00001);
      
    }
	
	@Test
    public void shouldProcessDeclination() throws Exception {
        assertNotNull(template);
        String jStr = "{\"vessels\":{\"self\":{\"environment\":{\"wind\":{\"directionApparent\":{\"value\":90.0000000000},\"directionTrue\":{\"value\":0.0000000000},\"speedApparent\":{\"value\":20.0000000000},\"speedTrue\":{\"value\":0.0000000000}}}}}}";
        template.sendBody("direct:input","$GPRMC,144629.20,A,5156.91111,N,00434.80385,E,0.295,,011113,,,A*78");
        template.sendBody("direct:input",jStr);
        
		 logger.debug(signalkModel);
		
		 //assertEquals(51.9485185d,Util.findValue(signalkModel.atPath(VESSELS,SELF), nav_magneticVariation).asDouble(),0.00001);
		// assertEquals(20.0d,Util.findValue(signalkModel.atPath(VESSELS,SELF),env_wind_speedApparent ).asDouble(),0.00001);
		 declinationProcessor.handle();
		 assertEquals(-3.1d,Util.findValue(signalkModel.atPath(VESSELS,SELF),nav_magneticVariation ).asDouble(),0.00001);
      
    }
	
	 @Override
	    protected RouteBuilder createRouteBuilder() {
	        return new RouteBuilder(){
	            public void configure() {
	            
	    			SignalkRouteFactory.configureInputRoute(this, "direct:input");
	            	
	            }
	        };
	    }

}
