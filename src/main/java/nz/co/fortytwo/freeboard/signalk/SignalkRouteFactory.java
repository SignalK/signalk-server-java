package nz.co.fortytwo.freeboard.signalk;

import nz.co.fortytwo.freeboard.server.AISProcessor;
import nz.co.fortytwo.freeboard.server.InputFilterProcessor;
import nz.co.fortytwo.freeboard.server.NMEAProcessor;
import nz.co.fortytwo.freeboard.server.OutputFilterProcessor;
import nz.co.fortytwo.freeboard.server.SignalkModelProcessor;

import org.apache.camel.builder.RouteBuilder;

public class SignalkRouteFactory {

	/**
	 * Configures a route for all input traffic, which will parse the traffic and update the signalk model
	 * @param routeBuilder
	 * @param input
	 * @param inputFilterProcessor
	 * @param nmeaProcessor
	 * @param aisProcessor
	 * @param signalkModelProcessor
	 */
	public static void configureInputRoute(RouteBuilder routeBuilder,String input) {
		routeBuilder.from(input).onException(Exception.class).handled(true).maximumRedeliveries(0)
		.to("log:nz.co.fortytwo.freeboard.signalk.receive?level=ERROR&showException=true&showStackTrace=true").end()
		// dump misc rubbish
		.process(new InputFilterProcessor())
		//convert NMEA to signalk
		.process(new NMEAProcessor())
		//convert AIS to signalk
		.process(new AISProcessor())
		//and update signalk model
		.process(new SignalkModelProcessor());
		
	}
	
	/**
	 * Configures the route for output to websockets
	 * @param routeBuilder
	 * @param input
	 */
	public static void configureWebsocketRoute(RouteBuilder routeBuilder ,String input){
		routeBuilder.from(input).onException(Exception.class).handled(true).maximumRedeliveries(0)
		.process(new OutputFilterProcessor())
		.to("log:nz.co.fortytwo.freeboard.signalk.websocket?level=ERROR&showException=true&showStackTrace=true").end()
		.to("websocket:signalk?sendToAll=true");
	}

}
