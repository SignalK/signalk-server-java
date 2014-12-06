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

import static nz.co.fortytwo.signalk.server.util.JsonConstants.SELF;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.VESSELS;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.env_wind_speedApparent;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.env_wind_speedTrue;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.nav_magneticVariation;
import static nz.co.fortytwo.signalk.server.util.JsonConstants.nav_position_latitude;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.SignalkRouteFactory;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.processor.DeclinationProcessor;
import nz.co.fortytwo.signalk.processor.WindProcessor;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.log4j.Logger;
import org.junit.Test;

public class SignalKReceiverTest extends CamelTestSupport {
 
    private static Logger logger = Logger.getLogger(SignalKReceiverTest.class);
	private SignalKModel signalkModel=SignalKModelFactory.getInstance();


	private DeclinationProcessor declinationProcessor=new DeclinationProcessor();

	private WindProcessor windProcessor = new WindProcessor();
	//private GPXProcessor gpxProcessor;

	
	@Produce(uri = "direct:input")
    protected ProducerTemplate template;
	


	@Test
    public void shouldProcessMessage() throws Exception {
        assertNotNull(template);
        template.sendBody("direct:input","$GPRMC,144629.20,A,5156.91111,N,00434.80385,E,0.295,,011113,,,A*78");
        
		 logger.debug(signalkModel);
		 assertEquals(51.9485185d,signalkModel.findValue(signalkModel.atPath(VESSELS,SELF), nav_position_latitude).asDouble(),0.00001);
		 logger.debug("Lat :"+signalkModel.findValue(signalkModel.atPath(VESSELS,SELF), nav_position_latitude));
 
      
    }
	
	@Test
    public void shouldProcessAisMessage() throws Exception {
        assertNotNull(template);
        template.sendBody("direct:input","!AIVDM,1,1,,B,15MwkRUOidG?GElEa<iQk1JV06Jd,0*6D");
        
		 logger.debug(signalkModel);
		 assertNotNull(signalkModel.atPath(VESSELS,"366998410"));
		 assertEquals(37.8251d,signalkModel.findValue(signalkModel.atPath(VESSELS,"366998410"), nav_position_latitude).asDouble(),0.001);
		 logger.debug("Lat :"+signalkModel.findValue(signalkModel.atPath(VESSELS,SELF), nav_position_latitude));
    }
	@Test
    public void shouldProcessTwoMessages() throws Exception {
        assertNotNull(template);
        String jStr = "{\"vessels\":{\"self\":{\"environment\":{\"wind\":{\"directionApparent\":{\"value\":90.0000000000},\"directionTrue\":{\"value\":0.0000000000},\"speedApparent\":{\"value\":20.0000000000},\"speedTrue\":{\"value\":0.0000000000}}}}}}";
        template.sendBody("direct:input","$GPRMC,144629.20,A,5156.91111,N,00434.80385,E,0.295,,011113,,,A*78");
        template.sendBody("direct:input",jStr);
        
		 logger.debug(signalkModel);
		
		 assertEquals(51.9485185d,signalkModel.findValue(signalkModel.atPath(VESSELS,SELF), nav_position_latitude).asDouble(),0.00001);
		 assertEquals(20.0d,signalkModel.findValue(signalkModel.atPath(VESSELS,SELF),env_wind_speedApparent ).asDouble(),0.00001);
      
    }
	
	@Test
    public void shouldProcessWindTrue() throws Exception {
        assertNotNull(template);
        String jStr = "{\"vessels\":{\"self\":{\"environment\":{\"wind\":{\"directionApparent\":{\"value\":90.0000000000},\"directionTrue\":{\"value\":0.0000000000},\"speedApparent\":{\"value\":20.0000000000},\"speedTrue\":{\"value\":0.0000000000}}}}}}";
        template.sendBody("direct:input","$GPRMC,144629.20,A,5156.91111,N,00434.80385,E,0.295,,011113,,,A*78");
        template.sendBody("direct:input",jStr);
        
		 logger.debug(signalkModel);
		
		 assertEquals(51.9485185d,signalkModel.findValue(signalkModel.atPath(VESSELS,SELF), nav_position_latitude).asDouble(),0.00001);
		 assertEquals(20.0d,signalkModel.findValue(signalkModel.atPath(VESSELS,SELF),env_wind_speedApparent ).asDouble(),0.00001);
		 windProcessor.handle();
		 assertEquals(20.0d,signalkModel.findValue(signalkModel.atPath(VESSELS,SELF),env_wind_speedTrue ).asDouble(),0.00001);
      
    }
	
	@Test
    public void shouldProcessDeclination() throws Exception {
        assertNotNull(template);
        String jStr = "{\"vessels\":{\"self\":{\"environment\":{\"wind\":{\"directionApparent\":{\"value\":90.0000000000},\"directionTrue\":{\"value\":0.0000000000},\"speedApparent\":{\"value\":20.0000000000},\"speedTrue\":{\"value\":0.0000000000}}}}}}";
        template.sendBody("direct:input","$GPRMC,144629.20,A,5156.91111,N,00434.80385,E,0.295,,011113,,,A*78");
        template.sendBody("direct:input",jStr);
        
		 logger.debug(signalkModel);
		
		 //assertEquals(51.9485185d,signalkModel.findValue(signalkModel.atPath(VESSELS,SELF), nav_magneticVariation).asDouble(),0.00001);
		// assertEquals(20.0d,signalkModel.findValue(signalkModel.atPath(VESSELS,SELF),env_wind_speedApparent ).asDouble(),0.00001);
		 declinationProcessor.handle();
		 assertEquals(0.4d,signalkModel.findValue(signalkModel.atPath(VESSELS,SELF),nav_magneticVariation ).asDouble(),0.00001);
      
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
