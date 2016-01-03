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

import static nz.co.fortytwo.signalk.util.SignalKConstants.env_depth_belowTransducer;
import static nz.co.fortytwo.signalk.util.SignalKConstants.env_wind_speedApparent;
import static nz.co.fortytwo.signalk.util.SignalKConstants.env_wind_speedTrue;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_magneticVariation;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_position_latitude;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_position_longitude;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels_dot_self_dot;

import java.util.concurrent.TimeUnit;

import nz.co.fortytwo.signalk.handler.DeclinationHandler;
import nz.co.fortytwo.signalk.handler.TrueWindHandler;
import nz.co.fortytwo.signalk.util.SignalKConstants;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class SignalKNmeaReceiverTest extends SignalKCamelTestSupport {
 
    static final String DIRECT_INPUT = "seda:input";
	static Logger logger = Logger.getLogger(SignalKNmeaReceiverTest.class);
	DeclinationHandler declinationProcessor=null;

	TrueWindHandler windProcessor = null;
	//private GPXProcessor gpxProcessor;

	MockEndpoint nmea = null;
	//@Produce(uri = RouteManager.SEDA_INPUT)
    protected ProducerTemplate template;
	
    
	@Before
	public void before() throws Exception {

		
	}
	
	public void init() throws Exception{
		
		declinationProcessor=new DeclinationHandler();
		windProcessor = new TrueWindHandler();
		template= new DefaultProducerTemplate(routeManager.getContext());
		template.setDefaultEndpointUri(DIRECT_INPUT);
		template.start();
	}

	@Test
    public void shouldProcessMessage() throws Exception {
		init();
        assertNotNull(template);
         nmea.reset();
         nmea.expectedMessageCount(1);
         template.sendBody(DIRECT_INPUT,"$GPRMC,144629.20,A,5156.91111,N,00434.80385,E,0.295,,011113,,,A*78");
         latch.await(2,TimeUnit.SECONDS);
		 logger.debug("SignalKModel:"+signalkModel);
		 assertEquals(51.9485185d,(double)signalkModel.get(SignalKConstants.vessels_dot_self_dot + nav_position_latitude),0.0001);
		 assertEquals(4.58006d,(double)signalkModel.get(SignalKConstants.vessels_dot_self_dot + nav_position_longitude),0.0001);
		 logger.debug("Lat :"+signalkModel.get(vessels_dot_self_dot + nav_position_latitude));
		 nmea.assertIsSatisfied();
      
    }
	
	@Test
    public void shouldProcessAisMessage() throws Exception {
		init();
        assertNotNull(template);
        nmea.reset();
        nmea.expectedMessageCount(1);
        template.sendBody(DIRECT_INPUT,"!AIVDM,1,1,,B,15MwkRUOidG?GElEa<iQk1JV06Jd,0*6D");
        latch.await(2,TimeUnit.SECONDS);
		 logger.debug("SignalKModel:"+signalkModel);
		 //assertNotNull(signalkModel.atPath(vessels,"366998410"));
		 assertEquals(37.8251d,(double)signalkModel.get(vessels+".366998410."+nav_position_latitude),0.001);
		 logger.debug("Lat :"+signalkModel.get(vessels+".366998410." + nav_position_latitude));
		 nmea.assertIsSatisfied();
    }
	@Test
    public void shouldProcessTwoMessages() throws Exception {
		init();
        assertNotNull(template);
        nmea.reset();
        nmea.expectedMessageCount(1);
        String jStr = "{\"vessels\":{\""+SignalKConstants.self+"\":{\"environment\":{\"wind\":{\"angleApparent\":{\"value\":90.0000000000},\"directionTrue\":{\"value\":0.0000000000},\"speedApparent\":{\"value\":20.0000000000},\"speedTrue\":{\"value\":0.0000000000}}}}}}";
        template.sendBody(DIRECT_INPUT,"$GPRMC,144629.20,A,5156.91111,N,00434.80385,E,0.295,,011113,,,A*78");
        template.sendBody(DIRECT_INPUT,jStr);
        latch.await(2,TimeUnit.SECONDS);
		 logger.debug("SignalKModel:"+signalkModel);
		
		 assertEquals(51.9485185d,(double)signalkModel.get(vessels_dot_self_dot + nav_position_latitude),0.00001);
		 assertEquals(20.0d,(double)signalkModel.getValue(vessels_dot_self_dot +env_wind_speedApparent ),0.00001);
		 nmea.assertIsSatisfied();
    }
	
	@Test
    public void shouldProcessWindTrue() throws Exception {
		init();
        assertNotNull(template);
        nmea.reset();
        nmea.expectedMessageCount(1);
        String jStr = "{\"vessels\":{\""+SignalKConstants.self+"\":{\"environment\":{\"wind\":{\"angleApparent\":{\"value\":90.0000000000},\"directionTrue\":{\"value\":0.0000000000},\"speedApparent\":{\"value\":20.0000000000},\"speedTrue\":{\"value\":0.0000000000}}}}}}";
        template.sendBody(DIRECT_INPUT,"$GPRMC,144629.20,A,5156.91111,N,00434.80385,E,0.295,,011113,,,A*78");
        template.sendBody(DIRECT_INPUT,jStr);
        latch.await(2,TimeUnit.SECONDS);
		 logger.debug("SignalKModel:"+signalkModel);
		
		 assertEquals(51.9485185d,(double)signalkModel.get(vessels_dot_self_dot + nav_position_latitude),0.00001);
		 assertEquals(20.0d,(double)signalkModel.getValue(vessels_dot_self_dot +env_wind_speedApparent ),0.00001);
		 windProcessor.handle(signalkModel);
		 assertEquals(20.0d,(double)signalkModel.getValue(vessels_dot_self_dot +env_wind_speedTrue ),0.00001);
		 nmea.assertIsSatisfied();
    }
	
	@Test
    public void shouldProcessDeclination() throws Exception {
		init();
        assertNotNull(template);
        nmea.reset();
        nmea.expectedMessageCount(1);
        String jStr = "{\"vessels\":{\""+SignalKConstants.self+"\":{\"environment\":{\"wind\":{\"angleApparent\":{\"value\":90.0000000000},\"directionTrue\":{\"value\":0.0000000000},\"speedApparent\":{\"value\":20.0000000000},\"speedTrue\":{\"value\":0.0000000000}}}}}}";
        template.sendBody(DIRECT_INPUT,"$GPRMC,144629.20,A,5156.91111,N,00434.80385,E,0.295,,011113,,,A*78");
        template.sendBody(DIRECT_INPUT,jStr);
        latch.await(2,TimeUnit.SECONDS);
		 logger.debug("SignalKModel:"+signalkModel);
		
		 //assertEquals(51.9485185d,signalkModel.getValue(vessels_dot_self_dot + nav_magneticVariation),0.00001);
		// assertEquals(20.0d,signalkModel.getValue(vessels_dot_self_dot +env_wind_speedApparent ),0.00001);
		 declinationProcessor.handle(signalkModel);
		 logger.debug("SignalKModel:"+signalkModel);
		 assertEquals(0.8d,(double)signalkModel.getValue(vessels_dot_self_dot +nav_magneticVariation ),0.00001);
		 nmea.assertIsSatisfied();
    }

	@Test
	public void shouldHandleMultipleMessages() throws Exception{
		
		init();
        assertNotNull(template);
        nmea.reset();
        nmea.expectedMessageCount(3);
       
        template.sendBody(DIRECT_INPUT,"$IIDBT,034.25,f,010.44,M,005.64,F*27");
        template.sendBody(DIRECT_INPUT,"$IIDBT,034.31,f,010.46,M,005.65,F*21");
        template.sendBody(DIRECT_INPUT,"$IIDBT,039.17,f,011.94,M,006.45,F*27");
        
        latch.await(5,TimeUnit.SECONDS);
		 logger.debug("SignalKModel:"+signalkModel);
		
		 assertEquals(011.94d,(double)signalkModel.getValue(vessels_dot_self_dot + env_depth_belowTransducer),0.00001);
		
		 nmea.assertIsSatisfied();
		 
		/*$IIDBT,034.25,f,010.44,M,005.64,F*27
		$IIDBT,034.31,f,010.46,M,005.65,F*21
		$IIDBT,034.28,f,010.45,M,005.64,F*2B
		$IIDBT,034.31,f,010.46,M,005.65,F*21
		$IIDBT,034.31,f,010.46,M,005.65,F*21*/

	}
	@Override
	public void configureRouteBuilder(RouteBuilder routeBuilder) {
		nmea = (MockEndpoint) routeBuilder.getContext().getEndpoint("mock:output");
		try{
			SignalkRouteFactory.configureInputRoute(routeBuilder, DIRECT_INPUT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		routeBuilder.from(RouteManager.SEDA_NMEA).to(nmea);
		
	}

}
