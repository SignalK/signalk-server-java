package nz.co.fortytwo.signalk.model;

import nz.co.fortytwo.signalk.server.AISProcessor;
import nz.co.fortytwo.signalk.server.InputFilterProcessor;
import nz.co.fortytwo.signalk.server.NMEAProcessor;
import nz.co.fortytwo.signalk.server.OutputFilterProcessor;
import nz.co.fortytwo.signalk.server.RestAuthProcessor;
import nz.co.fortytwo.signalk.server.RestProcessor;
import nz.co.fortytwo.signalk.server.SignalkDiffProcessor;
import nz.co.fortytwo.signalk.server.SignalkModelProcessor;
import nz.co.fortytwo.signalk.server.TcpServer;
import nz.co.fortytwo.signalk.server.ValidationProcessor;

import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
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
		.to("log:nz.co.fortytwo.signalk.model.receive?level=ERROR&showException=true&showStackTrace=true").end()
		// dump misc rubbish
		.process(new InputFilterProcessor())
		//convert NMEA to signalk
		.process(new NMEAProcessor())
		//convert AIS to signalk
		.process(new AISProcessor())
		//deal with diff format
		.process(new SignalkDiffProcessor())
		//make sure we have timestamp/source
		.process(new ValidationProcessor())
		//and update signalk model
		.process(new SignalkModelProcessor());
		
	}
	
	/**
	 * Configures the route for output to websockets
	 * @param routeBuilder
	 * @param input
	 */
	public static void configureWebsocketTxRoute(RouteBuilder routeBuilder ,String input, int port, String staticResources){
		routeBuilder.from(input).onException(Exception.class).handled(true).maximumRedeliveries(0)
		.process(new OutputFilterProcessor())
		.to("log:nz.co.fortytwo.signalk.model.websocket.tx?level=ERROR&showException=true&showStackTrace=true").end()
		.to("websocket://0.0.0.0:"+port+"/signalk/stream?sendToAll=true&staticResources="+staticResources);
	}
	/**
	 * Configures the route for input to websockets
	 * @param routeBuilder
	 * @param input
	 */
	public static void configureWebsocketRxRoute(RouteBuilder routeBuilder ,String input, int port){
		routeBuilder.from("websocket://0.0.0.0:"+port+"/signalk/stream").onException(Exception.class).handled(true).maximumRedeliveries(0)
		.to("log:nz.co.fortytwo.signalk.model.websocket.rx?level=ERROR&showException=true&showStackTrace=true").end()
		.to(input);
	}
	public static void configureTcpServerRoute(RouteBuilder routeBuilder ,String input, TcpServer tcpServer){
	// push NMEA out via TCPServer.
		routeBuilder.from(input).process((Processor) tcpServer).end();
	}
	
	public static void configureRestRoute(RouteBuilder routeBuilder ,String input){
		routeBuilder.from(input)
			.setExchangePattern(ExchangePattern.InOut)
			.process(new RestProcessor());
		}
	
	public static void configureAuthRoute(RouteBuilder routeBuilder ,String input){
		routeBuilder.from(input)
			.setExchangePattern(ExchangePattern.InOut)
			.process(new RestAuthProcessor());
		}
}
