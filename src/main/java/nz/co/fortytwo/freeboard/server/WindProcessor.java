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

import static nz.co.fortytwo.freeboard.server.util.JsonConstants.SELF;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.env_wind_directionApparent;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.env_wind_directionTrue;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.env_wind_speedApparent;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.env_wind_speedTrue;
import static nz.co.fortytwo.freeboard.server.util.JsonConstants.nav_speedOverGround;
import mjson.Json;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;



/**
 * Calculates the true wind from apparent wind and vessel speed/heading
 * 
 * @author robert
 * 
 */
public class WindProcessor extends FreeboardProcessor implements Processor{

	private static Logger logger = Logger.getLogger(WindProcessor.class);
	

	public void process(Exchange exchange) throws Exception {
		
		try {
		
			handle();
			
		} catch (Exception e) {
			logger.error(e);
		}
	}

	
	public  void handle() {
		try {

			Json vesselSpeed =  signalkModel.findValue(signalkModel.self(), nav_speedOverGround);
			Json apparentDirection = signalkModel.findValue(signalkModel.self(), env_wind_directionApparent);
			Json apparentWindSpeed =signalkModel.findValue(signalkModel.self(), env_wind_speedApparent);
			if (apparentWindSpeed !=null && apparentDirection!=null && vesselSpeed!=null) {
				// now calc and add to body
				// 0-360 from bow clockwise
				
				double[] windCalc = calcTrueWindDirection(apparentWindSpeed.asDouble(), apparentDirection.asDouble(), vesselSpeed.asDouble());
				if(windCalc!=null){
					
					if (!Double.isNaN(windCalc[1])) {
						//map.put(Constants.WIND_DIR_TRUE, round(trueDirection, 2));
						signalkModel.putWith(signalkModel.self(), env_wind_directionTrue, round(windCalc[1], 2), SELF);
					}
					if (!Double.isNaN(windCalc[0])) {
						//map.put(Constants.WIND_SPEED_TRUE, round(trueWindSpeed, 2));
						signalkModel.putWith(signalkModel.self(), env_wind_speedTrue, round(windCalc[0], 2), SELF);
					}
				}

			}

		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	
	}

	/**
	 * Calculates the true wind direction from apparent wind on vessel
	 * Result is relative to bow
	 * 
	 * @param apparentWnd
	 * @param apparentDir
	 *            0 to 360 deg to the bow
	 * @param vesselSpd
	 * @param trueDirection 
	 * @param trueWindSpeed 
	 * @return trueDirection 0 to 360 deg to the bow
	 */
	double[] calcTrueWindDirection(double apparentWnd, double apparentDir, double vesselSpd) {
		double trueDirection = 0.0;
		double trueWindSpeed = 0.0;
		double windCalc[] = {trueWindSpeed, trueDirection};
		/*
		 * Y = 90 - D
		 * a = AW * ( cos Y )
		 * bb = AW * ( sin Y )
		 * b = bb - BS
		 * True-Wind Speed = (( a * a ) + ( b * b )) 1/2
		 * True-Wind Angle = 90-arctangent ( b / a )
		 */
		apparentDir = apparentDir % 360;
		boolean port = apparentDir > 180;
		if (port) {
			apparentDir = 360 - apparentDir;
		}

		/*
		 * // Calculate true heading diff and true wind speed - JAVASCRIPT
		 * tan_alpha = (Math.sin(angle) / (aspeed - Math.cos(angle)));
		 * alpha = Math.atan(tan_alpha);
		 * 
		 * tdiff = rad2deg(angle + alpha);
		 * tspeed = Math.sin(angle)/Math.sin(alpha);
		 */
		double aspeed = Math.max(apparentDir, vesselSpd);
		if (apparentWnd > 0 && vesselSpd > 0.0) {
			aspeed = apparentWnd / vesselSpd;
		}
		double angle = Math.toRadians(apparentDir);
		double tan_alpha = (Math.sin(angle) / (aspeed - Math.cos(angle)));
		double alpha = Math.atan(tan_alpha);
		double tAngle = Math.toDegrees(alpha + angle);
		if (Double.valueOf(tAngle).isNaN() || Double.isInfinite(tAngle))
			return windCalc;
		if (port) {
			trueDirection = (360 - tAngle);
		} else {
			trueDirection = tAngle;
		}
		windCalc[1]=trueDirection;
		
		if (apparentWnd < 0.1 || vesselSpd < 0.1) {
			trueWindSpeed = Math.max(apparentWnd, vesselSpd);
			windCalc[0]=trueWindSpeed;
			return windCalc;
		}
		double tspeed = Math.sin(angle) / Math.sin(alpha);
		if (Double.valueOf(tspeed).isNaN() || Double.isInfinite(tspeed))
			return windCalc;
		trueWindSpeed = tspeed * vesselSpd;
		windCalc[0]=trueWindSpeed;
		return windCalc;
	}

	

}
