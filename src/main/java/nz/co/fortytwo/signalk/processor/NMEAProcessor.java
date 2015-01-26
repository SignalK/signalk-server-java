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
package nz.co.fortytwo.signalk.processor;

import static nz.co.fortytwo.signalk.server.util.JsonConstants.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import mjson.Json;
import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.BVESentence;
import net.sf.marineapi.nmea.sentence.DepthSentence;
import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.VHWSentence;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;



/**
 * Processes NMEA sentences in the body of a message, firing events to interested listeners
 * Converts the NMEA messages to signalk
 * 
 * @author robert
 * 
 */
public class NMEAProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = Logger.getLogger(NMEAProcessor.class);
	private static final String DISPATCH_ALL = "DISPATCH_ALL";

	// map of sentence listeners
	private ConcurrentMap<String, List<SentenceListener>> listeners = new ConcurrentHashMap<String, List<SentenceListener>>();

	public NMEAProcessor() {
		super();
		// register BVE
		SentenceFactory.getInstance().registerParser("BVE", net.sf.marineapi.nmea.parser.BVEParser.class);
		SentenceFactory.getInstance().registerParser("XDR", net.sf.marineapi.nmea.parser.CruzproXDRParser.class);
		setNmeaListeners();
	}

	public void process(Exchange exchange) throws Exception {
		if (exchange.getIn().getBody() == null || !(exchange.getIn().getBody() instanceof String)) {
			return;
		}
		
		String body = exchange.getIn().getBody(String.class);
		exchange.getIn().setBody(handle(body));
	}

	// @Override
	public Object handle(String bodyStr) {
		SignalKModel json = null;
		if (StringUtils.isNotBlank(bodyStr)&& bodyStr.startsWith("$")) {
			try {
				logger.debug("Processing NMEA:" + bodyStr);
				Sentence sentence = SentenceFactory.getInstance().createParser(bodyStr);
				json = SignalKModelFactory.getCleanInstance();
				fireSentenceEvent(json, sentence);
				return json;
			} catch (Exception e) {
				logger.debug(e.getMessage(), e);
				logger.error(e.getMessage() + " : " + bodyStr);
			}
		}
		return bodyStr;
	}

	/**
	 * Adds a {@link SentenceListener} that wants to receive all sentences read
	 * by the reader.
	 * 
	 * @param listener
	 *            {@link SentenceListener} to be registered.
	 * @see net.sf.marineapi.nmea.event.SentenceListener
	 */
	public void addSentenceListener(SentenceListener listener) {
		registerListener(DISPATCH_ALL, listener);
	}

	/**
	 * Adds a {@link SentenceListener} that is interested in receiving only
	 * sentences of certain type.
	 * 
	 * @param sl
	 *            SentenceListener to add
	 * @param type
	 *            Sentence type for which the listener is registered.
	 * @see net.sf.marineapi.nmea.event.SentenceListener
	 */
	public void addSentenceListener(SentenceListener sl, SentenceId type) {
		registerListener(type.toString(), sl);
	}

	/**
	 * Adds a {@link SentenceListener} that is interested in receiving only
	 * sentences of certain type.
	 * 
	 * @param sl
	 *            SentenceListener to add
	 * @param type
	 *            Sentence type for which the listener is registered.
	 * @see net.sf.marineapi.nmea.event.SentenceListener
	 */
	public void addSentenceListener(SentenceListener sl, String type) {
		registerListener(type, sl);
	}

	/**
	 * Remove a listener from reader. When removed, listener will not receive
	 * any events from the reader.
	 * 
	 * @param sl
	 *            {@link SentenceListener} to be removed.
	 */
	public void removeSentenceListener(SentenceListener sl) {
		for (List<SentenceListener> list : listeners.values()) {
			if (list.contains(sl)) {
				list.remove(sl);
			}
		}
	}

	/**
	 * Dispatch data to all listeners.
	 * 
	 * @param map
	 * 
	 * @param sentence
	 *            sentence string.
	 */
	private void fireSentenceEvent(SignalKModel json, Sentence sentence) {
		if (!sentence.isValid()) {
			logger.warn("NMEA Sentence is invalid:" + sentence.toSentence());
			return;
		}
		// TODO: Why am I creating all these lists?
		String type = sentence.getSentenceId();
		Set<SentenceListener> list = new HashSet<SentenceListener>();

		if (listeners.containsKey(type)) {
			list.addAll(listeners.get(type));
		}
		if (listeners.containsKey(DISPATCH_ALL)) {
			list.addAll(listeners.get(DISPATCH_ALL));
		}

		for (SentenceListener sl : list) {
			try {
				SentenceEvent se = new SentenceEvent(json, sentence);
				sl.sentenceRead(se);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

	}

	/**
	 * Registers a SentenceListener to hash map with given key.
	 * 
	 * @param type
	 *            Sentence type to register for
	 * @param sl
	 *            SentenceListener to register
	 */
	private void registerListener(String type, SentenceListener sl) {
		if (listeners.containsKey(type)) {
			listeners.get(type).add(sl);
		} else {
			List<SentenceListener> list = new Vector<SentenceListener>();
			list.add(sl);
			listeners.put(type, list);
		}
	}

	/**
	 * Adds NMEA sentence listeners to process NMEA to simple output
	 * 
	 * @param processor
	 */
	private void setNmeaListeners() {

		addSentenceListener(new SentenceListener() {

			private boolean startLat = true;
			private boolean startLon = true;
			double previousLat = 0;
			double previousLon = 0;
			double previousSpeed = 0;
			static final double ALPHA = 1 - 1.0 / 6;

			public void sentenceRead(SentenceEvent evt) {
				SignalKModel sk = (SignalKModel) evt.getSource();
				Json  json=sk.self();
				try{
					if (evt.getSentence() instanceof PositionSentence) {
						PositionSentence sen = (PositionSentence) evt.getSentence();
	
						if (startLat) {
							previousLat = sen.getPosition().getLatitude();
							startLat = false;
						}
						previousLat = Util.movingAverage(ALPHA, previousLat, sen.getPosition().getLatitude());
						logger.debug("lat position:" + sen.getPosition().getLatitude() + ", hemi=" + sen.getPosition().getLatitudeHemisphere());
						sk.putWith(json, nav_position_latitude , previousLat, "output");
	
						if (startLon) {
							previousLon = sen.getPosition().getLongitude();
							startLon = false;
						}
						previousLon = Util.movingAverage(ALPHA, previousLon, sen.getPosition().getLongitude());
						sk.putWith(json, nav_position_longitude , previousLon, "output");
					}
	
					if (evt.getSentence() instanceof HeadingSentence) {
						
						if (!(evt.getSentence() instanceof VHWSentence)) {
							
							HeadingSentence sen = (HeadingSentence) evt.getSentence();
							
							if (sen.isTrue()) {
								try {
									
									sk.putWith(json, nav_courseOverGroundTrue , sen.getHeading(), "output");
									
								} catch (Exception e) {
									logger.error(e.getMessage());
								}
							} else {
								
								sk.putWith(json, nav_courseOverGroundMagnetic , sen.getHeading(), "output");
							}
						}
					}
					
					if (evt.getSentence() instanceof RMCSentence) {
						RMCSentence sen = (RMCSentence) evt.getSentence();
						Util.checkTime(sen);
						previousSpeed = Util.movingAverage(ALPHA, previousSpeed, Util.kntToMs(sen.getSpeed()));
						sk.putWith(json, nav_speedOverGround , Util.kntToMs(sen.getSpeed()), "output");
					}
					if (evt.getSentence() instanceof VHWSentence) {
						VHWSentence sen = (VHWSentence) evt.getSentence();
						//VHW sentence types have both, but true can be empty
						try {
							sk.putWith(json, nav_courseOverGroundMagnetic , sen.getMagneticHeading(), "output");
							sk.putWith(json, nav_courseOverGroundTrue , sen.getHeading(), "output");
							
						} catch (DataNotAvailableException e) {
							logger.error(e.getMessage());
						}
						previousSpeed = Util.movingAverage(ALPHA, previousSpeed, Util.kntToMs(sen.getSpeedKnots()));
						sk.putWith(json, nav_speedOverGround , previousSpeed, "output");
						
					}
	
					// MWV wind
					// Mega sends $IIMVW with 0-360d clockwise from bow, (relative to bow)
					// Mega value is int+'.0'
					if (evt.getSentence() instanceof MWVSentence) {
						MWVSentence sen = (MWVSentence) evt.getSentence();
						//TODO: check relative to bow or compass + sen.getSpeedUnit()
						// relative to bow
						double angle = sen.getAngle();
						sk.putWith(json, env_wind_angleApparent , angle, "output");
						sk.putWith(json, env_wind_speedApparent , Util.kntToMs(sen.getSpeed()), "output");
						
					}
					// Cruzpro BVE sentence
					// TODO: how to deal with multiple engines??
					if (evt.getSentence() instanceof BVESentence) {
						BVESentence sen = (BVESentence) evt.getSentence();
						if (sen.isFuelGuage()) {
							sk.putWith(json, tanks_id_level , sen.getFuelRemaining(), "output");
							sk.putWith(json, propulsion_id_fuelUsageRate , sen.getFuelUseRateUnitsPerHour(), "output");
							
							// map.put(Constants.FUEL_USED, sen.getFuelUsedOnTrip());
							// sk.putWith(tempSelfNode, JsonConstants.tank_level, sen.getFuelRemaining(), "output");
						}
						if (sen.isEngineRpm()) {
							sk.putWith(json, propulsion_id_rpm , sen.getEngineRpm(), "output");
							// map.put(Constants.ENGINE_HOURS, sen.getEngineHours());
							//sk.putWith(tempSelfNode, JsonConstants.propulsion_hours, sen.getEngineHours(), "output");
							// map.put(Constants.ENGINE_MINUTES, sen.getEngineMinutes());
							//sk.putWith(tempSelfNode, JsonConstants.propulsion_minutes, sen.getEngineMinutes(), "output");
	
						}
						if (sen.isTempGuage()) {
							sk.putWith(json, propulsion_id_engineTemperature , sen.getEngineTemp(), "output");
							// map.put(Constants.ENGINE_VOLTS, sen.getVoltage());
							//sk.putWith(tempSelfNode, JsonConstants.propulsion_engineVolts, sen.getVoltage(), "output");
							// map.put(Constants.ENGINE_TEMP_HIGH_ALARM, sen.getHighTempAlarmValue());
							// map.put(Constants.ENGINE_TEMP_LOW_ALARM, sen.getLowTempAlarmValue());
	
						}
						if (sen.isPressureGuage()) {
							sk.putWith(json, propulsion_id_oilPressure , sen.getPressure(), "output");
							// map.put(Constants.ENGINE_PRESSURE_HIGH_ALARM, sen.getHighPressureAlarmValue());
							// map.put(Constants.ENGINE_PRESSURE_LOW_ALARM, sen.getLowPressureAlarmValue());
	
						}
	
					}
					if (evt.getSentence() instanceof DepthSentence) {
						DepthSentence sen = (DepthSentence) evt.getSentence();
						// in meters
						sk.putWith(json, env_depth_belowTransducer , sen.getDepth(), "output");
						
					}
				}catch (DataNotAvailableException e){
					logger.error(e.getMessage()+":"+evt.getSentence().toSentence());
					//logger.debug(e.getMessage(),e);
				}
				
			}

			public void readingStopped() {
			}

			public void readingStarted() {
			}

			public void readingPaused() {
			}
		});
	}

}
