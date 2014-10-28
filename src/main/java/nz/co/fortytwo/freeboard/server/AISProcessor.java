/*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
 * 
 * This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
 * 
 * FreeBoard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FreeBoard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FreeBoard. If not, see <http://www.gnu.org/licenses/>.
 */
package nz.co.fortytwo.freeboard.server;

import static nz.co.fortytwo.freeboard.server.util.JsonConstants.VESSELS;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.communication_callsignVhf;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.mmsi;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.name;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.nav_courseOverGroundTrue;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.nav_headingTrue;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.nav_position_latitude;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.nav_position_longitude;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.nav_speedOverGround;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.nav_state;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mjson.Json;
import nz.co.fortytwo.freeboard.server.ais.AisVesselInfo;
import nz.co.fortytwo.freeboard.server.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessage18;
import dk.dma.ais.message.AisPositionMessage;
import dk.dma.ais.message.AisStaticCommon;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.packet.AisPacketParser;
import dk.dma.ais.sentence.Abk;
import dk.dma.ais.sentence.SentenceException;

/**
 * Churns through incoming nav data and looking for AIVDM messages
 * Translates the VDMs into AisMessages and sends the AisPositionMessages on to the browser.
 * Mostly we need 1,2,3,5, 18,19
 * 
 * @author robert
 * 
 */
public class AISProcessor extends FreeboardProcessor implements Processor {


	private static Logger logger = Logger.getLogger(AISProcessor.class);

    /** Reader to parse lines and deliver complete AIS packets.
     * Updates them into model, and removes the key from the map. */
    private AisPacketParser packetParser = new AisPacketParser();
	
    private Map<Integer, String> navStatusMap = new HashMap<>();
	
	public AISProcessor() {
		navStatusMap.put(0 ,"Under way using engine");
		navStatusMap.put(1 ,"At anchor");
		navStatusMap.put(2 ,"Not under command");
		navStatusMap.put(3 ,"Restricted manoeuverability");
		navStatusMap.put(4 ,"Constrained by her draught");
		navStatusMap.put(5 ,"Moored");
		navStatusMap.put(6 ,"Aground");
		navStatusMap.put(7 ,"Engaged in Fishing");
		navStatusMap.put(8 ,"Under way sailing");
		navStatusMap.put(9 ,"Reserved for future amendment of Navigational Status for HSC");
		navStatusMap.put(10,"Reserved for future amendment of Navigational Status for WIG");
		navStatusMap.put(11,"Reserved for future use");
		navStatusMap.put(12,"Reserved for future use");
		navStatusMap.put(13,"Reserved for future use");
		navStatusMap.put(14,"Reserved for future use");
		navStatusMap.put(15,"Not defined (default)");
	}

	public void process(Exchange exchange) {
		if (exchange.getIn().getBody() == null)
			return;
		if (exchange.getIn().getBody() instanceof String){
				
			String bodyStr = exchange.getIn().getBody(String.class);
			exchange.getIn().setBody(handle(bodyStr));
		}

	}

	

	//@Override
	public Object handle(String bodyStr) {
		
			logger.debug("Processing AIS:"+bodyStr);
			if (StringUtils.isNotBlank(bodyStr)&&bodyStr.startsWith("!AIVDM")) {
				try {
					
					AisPacket packet = handleLine(bodyStr);
					AisVesselInfo vInfo = null;
					if(packet!=null && packet.isValidMessage()){
						//process message here
						AisMessage message = packet.getAisMessage();
						logger.debug("AisMessage:"+message.getClass()+":"+message.toString());
						//1,2,3
						if(message instanceof AisPositionMessage){
							vInfo=new AisVesselInfo((AisPositionMessage) message);
						}
						//5,19,24
						if(message instanceof AisStaticCommon){
							vInfo=new AisVesselInfo((AisStaticCommon) message);
						}
						if(message instanceof AisMessage18){
							vInfo=new AisVesselInfo((AisMessage18) message);
						}
						if(vInfo!=null){
							Json json = signalkModel.getEmptyRootNode();
							Json aisVessel  = signalkModel.addNode(json, VESSELS+"."+String.valueOf(vInfo.getUserId()));
						
							aisVessel.set(name, vInfo.getName());
							aisVessel.set(mmsi, String.valueOf(vInfo.getUserId()));
							signalkModel.putWith(aisVessel, nav_state, navStatusMap.get(vInfo.getNavStatus()), "AIS");
							if(vInfo.getPosition()!=null){
								signalkModel.putWith(aisVessel, nav_position_latitude, vInfo.getPosition().getLatitude(), "AIS");
								signalkModel.putWith(aisVessel, nav_position_longitude, vInfo.getPosition().getLongitude(), "AIS");
							}
							signalkModel.putWith(aisVessel, nav_courseOverGroundTrue, ((double)vInfo.getCog())/10, "AIS");
							signalkModel.putWith(aisVessel, nav_speedOverGround, Util.kntToMs(((double)vInfo.getSog())/10), "AIS");
							signalkModel.putWith(aisVessel, nav_headingTrue, ((double)vInfo.getTrueHeading())/10, "AIS");
							signalkModel.putWith(aisVessel, communication_callsignVhf, vInfo.getCallsign(), "AIS");
							return json;
						}
					}

				} catch (Exception e) {
					logger.debug(e.getMessage(),e);
					logger.error(e.getMessage()+" : "+bodyStr);
				}
			}
			return bodyStr;
			//https://github.com/dma-ais/AisLib
			
			/*
			 *  HD-SF. Free raw AIS data feed for non-commercial use.
			 *   hd-sf.com:9009 
			 */
		
	}

	   /**
     * Handle a received line
     * 
     * @param line
     * @return
     */
    private AisPacket handleLine(String messageString) throws IOException {
    	logger.debug("AIS Received : " + messageString);
        // Check for ABK
        if (Abk.isAbk(messageString)) {
            	logger.debug("AIS Received ABK: " + messageString);
            return null;
        }

        try {
        	AisPacket packet = null;
            String[] lines = messageString.split("\\r?\\n");
           
            for (String line : lines) {
                packet = packetParser.readLine(line);
            }
            return packet;
        } catch (SentenceException se) {
        	logger.info("AIS Sentence error: " + se.getMessage() + " line: " + messageString);
            throw new IOException(se);
            
        }
    }
   
}
