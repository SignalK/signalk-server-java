package nz.co.fortytwo.freeboard.server;

import mjson.Json;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.restlet.RestletConstants;
import org.apache.log4j.Logger;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

/**
 * Processes auth requests for Signal K data
 * 
 * 
 * @author robert
 *
 */
public class RestAuthProcessor extends FreeboardProcessor implements Processor{

	private static Logger logger = Logger.getLogger(RestAuthProcessor.class);
	@Override
	public void process(Exchange exchange) throws Exception {
		// the Restlet request should be available if neeeded
        Request request = exchange.getIn().getHeader(RestletConstants.RESTLET_REQUEST, Request.class);
        if(Method.GET==request.getMethod()) processGet(request, exchange);
     
	}

	private void processGet(Request request, Exchange exchange) {
		// use Restlet API to create the response
        Response response = exchange.getIn().getHeader(RestletConstants.RESTLET_RESPONSE, Response.class);
        String path =  request.getOriginalRef().getPath();
        logger.debug("We are processing the restlet:"+path);
        int len = request.getRootRef().getPath().length();
        //check valid request.
        if(path.length()<len){
        	response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Bad Request");
        	return;
        }
        path=path.substring(request.getRootRef().getPath().length());
      
        logger.debug("We are processing the extension:"+path);
        response.setEntity("OK",MediaType.TEXT_PLAIN);
        CookieSetting cookieSetting = new CookieSetting(0, "signalk.auth", path);
        cookieSetting.setPath("/signalk/");
        response.getCookieSettings().add(cookieSetting);

        // SEND RESPONSE
        exchange.getOut().setBody(response.getEntityAsText());
        response.setStatus(Status.SUCCESS_OK);
		
	}

}
