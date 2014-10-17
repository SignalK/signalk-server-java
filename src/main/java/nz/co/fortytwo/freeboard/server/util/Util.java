/*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
 * 
 * This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
 *
 *  FreeBoard is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  FreeBoard is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with FreeBoard.  If not, see <http://www.gnu.org/licenses/>.
 */

package nz.co.fortytwo.freeboard.server.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import mjson.Json;
import net.sf.marineapi.nmea.sentence.RMCSentence;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Place for all the left over bits that are used across freeboard
 * @author robert
 *
 */
public class Util {
	
	private static Logger logger = Logger.getLogger(Util.class);
	private static Properties props;
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
	public static File cfg = null;
	private static boolean timeSet=false;
	private static DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	
	/**
	 * Smooth the data a bit
	 * @param prev
	 * @param current
	 * @return
	 */
	public static  double movingAverage(double ALPHA, double prev, double current) {
	    prev = ALPHA * prev + (1-ALPHA) * current;
	    return prev;
	}

	/**
	 * Load the config from the named dir, or if the named dir is null, from the default location
	 * The config is cached, subsequent calls get the same object 
	 * @param dir
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Properties getConfig(String dir) throws FileNotFoundException, IOException{
		if(props==null){
			//we do a quick override so we get nice sorted output :-)
			props = new Properties() {
			    /**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
			    public Set<Object> keySet(){
			        return Collections.unmodifiableSet(new TreeSet<Object>(super.keySet()));
			    }

			    @Override
			    public synchronized Enumeration<Object> keys() {
			        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
			    }
			};
			Util.setDefaults(props);
			if(StringUtils.isNotBlank(dir)){
				//we provided a config dir, so we use it
				props.setProperty(Constants.CFG_DIR, dir);
				cfg = new File(props.getProperty(Constants.CFG_DIR)+props.getProperty(Constants.CFG_FILE));
			}else if(Util.getUSBFile()!=null){
				//nothing provided, but we have a usb config dir, so use it
				cfg = new File(Util.getUSBFile(),props.getProperty(Constants.CFG_DIR)+props.getProperty(Constants.CFG_FILE));
			}else{
				//use the default config
				cfg = new File(props.getProperty(Constants.CFG_DIR)+props.getProperty(Constants.CFG_FILE));
			}
			
			if(cfg.exists()){
				props.load(new FileReader(cfg));
			}
		}
		return props;
	}
	
	/**
	 * Save the current config to disk.
	 * @throws IOException
	 */
	public static void saveConfig() throws IOException{
		if(props==null)return;
		props.store(new FileWriter(cfg), null);
		
	}

	/**
	 * Config defaults
	 * 
	 * @param props
	 */
	public static void setDefaults(Properties props) {
		//populate sensible defaults here
		props.setProperty(Constants.FREEBOARD_URL,"/freeboard");
		props.setProperty(Constants.FREEBOARD_RESOURCE,"freeboard/");
		props.setProperty(Constants.MAPCACHE_RESOURCE,"./mapcache");
		props.setProperty(Constants.MAPCACHE,"/mapcache");
		props.setProperty(Constants.HTTP_PORT,"8080");
		props.setProperty(Constants.WEBSOCKET_PORT,"9090");
		props.setProperty(Constants.CFG_DIR,"./conf/");
		props.setProperty(Constants.CFG_FILE,"freeboard.cfg");
		props.setProperty(Constants.DEMO,"false");
		props.setProperty(Constants.SERIAL_URL,"./src/test/resources/motu.log&scanStream=true&scanStreamDelay=500");
		props.setProperty(Constants.VIRTUAL_URL,"");
		props.setProperty(Constants.USBDRIVE,"/media/usb0");
		props.setProperty(Constants.TRACKS,"/tracks");
		props.setProperty(Constants.TRACKS_RESOURCE,"./tracks");
		props.setProperty(Constants.TRACK_CURRENT,"current.gpx");
		props.setProperty(Constants.WAYPOINTS,"/tracks");
		props.setProperty(Constants.WAYPOINTS_RESOURCE,"./tracks");
		props.setProperty(Constants.WAYPOINT_CURRENT,"waypoints.gpx");
		props.setProperty(Constants.SERIAL_PORTS,"/dev/ttyUSB0,/dev/ttyUSB1,/dev/ttyUSB2,/dev/ttyACM0,/dev/ttyACM1,/dev/ttyACM2");
		if(SystemUtils.IS_OS_WINDOWS){
			props.setProperty(Constants.SERIAL_PORTS,"COM1,COM2,COM3,COM4");
		}
		props.setProperty(Constants.DNS_USE_CHOICE,Constants.DNS_USE_BOAT);
		props.setProperty(Constants.ENABLE_COMET,"false");
	}
	

	/**
	 * Round to specified decimals
	 * @param val
	 * @param places
	 * @return
	 */
	public static double round(double val, int places){
		double scale = Math.pow(10, places);
		long iVal = Math.round (val*scale);
		return iVal/scale;
	}
	
	/**
	 * Updates and saves the scaling values for instruments
	 * @param scaleKey
	 * @param amount
	 * @param scaleValue
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static double updateScale(String scaleKey, double amount, double scaleValue) throws FileNotFoundException, IOException {
			scaleValue = scaleValue*amount;
			scaleValue= Util.round(scaleValue, 2);
			//logger.debug(" scale now = "+scale);
			
			//write out to config
			Util.getConfig(null).setProperty(scaleKey, String.valueOf(scaleValue));
			Util.saveConfig();
			
		return scaleValue;
	}

	/**
	 * Checks if a usb drive is inserted, and returns the root dir.
	 * Returns null if its not there
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static File getUSBFile() throws FileNotFoundException, IOException {
		File usbDrive = new File(Util.getConfig(null).getProperty(Constants.USBDRIVE));
		if(usbDrive.exists() && usbDrive.list().length>0){
			//we return it
			return usbDrive;
		}
		return null;
	}

	/**
	 * Attempt to set the system time using the GPS time
	 * @param sen
	 */
	@SuppressWarnings("deprecation")
	public static void checkTime(RMCSentence sen) {
			if(timeSet)return;
			try {
				net.sf.marineapi.nmea.util.Date dayNow = sen.getDate();
				//if we need to set the time, we will be WAAYYY out
				//we only try once, so we dont get lots of native processes spawning if we fail
				timeSet=true;
				Date date = new Date();
				if((date.getYear()+1900)==dayNow.getYear()){
					logger.debug("Current date is " + date);
					return;
				}
				//so we need to set the date and time
				net.sf.marineapi.nmea.util.Time timeNow = sen.getTime();
				String yy = String.valueOf(dayNow.getYear());
				String MM = pad(2,String.valueOf(dayNow.getMonth()));
				String dd = pad(2,String.valueOf(dayNow.getDay()));
				String hh = pad(2,String.valueOf(timeNow.getHour()));
				String mm = pad(2,String.valueOf(timeNow.getMinutes()));
				String ss = pad(2,String.valueOf(timeNow.getSeconds()));
				logger.debug("Setting current date to " + dayNow + " "+timeNow);
				String cmd = "sudo date --utc " + MM+dd+hh+mm+yy+"."+ss;
				Runtime.getRuntime().exec(cmd.split(" "));// MMddhhmm[[yy]yy]
				logger.debug("Executed date setting command:"+cmd);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			} 
			
		}

	/**
	 * pad the value to i places, eg 2 >> 02
	 * @param i
	 * @param valueOf
	 * @return
	 */
	private static String pad(int i, String value) {
		while(value.length()<i){
			value="0"+value;
		}
		return value;
	}
	
	/**
	 * Merge two json treemaps 
	 * @param mainNode
	 * @param updateNode
	 * @return
	 */
	public static Json merge(Json mainNode, Json updateNode) {
		//logger.debug("Merge objects");
		if(updateNode==null)return mainNode;
		if (mainNode != null && mainNode.isArray() && updateNode != null && updateNode.isArray()) {
			//mergeArrays( mainNode, updateNode);
		}
		Iterator<String> fieldNames = updateNode.asMap().keySet().iterator();
		while (fieldNames.hasNext()) {

			String fieldName = fieldNames.next();
			//logger.debug("Merge " + fieldName);
			Json json = mainNode.at(fieldName);
			// if field exists and is an embedded object
			if (json != null && json.isArray()) {
				//mergeArrays( json,  updateNode.at(fieldName));
			} else if (json != null && json.isObject()) {
				json = merge(json, updateNode.at(fieldName));
			} else {
				if (mainNode.isObject()) {
					// Overwrite field

					Json value = updateNode.at(fieldName);
					//logger.debug(fieldName + "=" + value);
					mainNode.set(fieldName, value);
				}
			}

		}

		return mainNode;
	}

	/**
	 * Merge two json treemap arrays ex jackson
	 * 
	 * @param mainNode
	 * @param updateNode
	 */
	/*private static void mergeArrays(Json mainNode, Json updateNode) {
		//logger.debug("Merge arrays");
		boolean found = false;
		Json main = null;
		List<Json> mainList = mainNode.asJson();
		for(Json node : updateNode.asJsonList()){
			if(mainhas(node.))
			key = node.fields().next().getKey();
			//logger.debug("Merge array item " + key);
			// if its not there the just add it
			Iterator<Json> mainItr = mainNode.elements();
			found = false;
			while (mainItr.hasNext()) {
				main = (ObjectNode) mainItr.next();
				mainKey = main.fields().next().getKey();
				if (mainKey.equals(key)) {
					merge(main, node);
					found = true;
				}
			}
			if (!found) {
				mainNode.add(node);
			}

		}

	}*/
	
	/*public static Object findValue(Json node, String fullPath) throws UnsupportedOperationException{
		Json targetNode = findNode(node, fullPath);
		if(targetNode==null)return null;
		targetNode = targetNode.at("value");
		if(targetNode.isString())return targetNode.asString();
		if(targetNode.isBoolean())return targetNode.asBoolean();
		if(targetNode.isNumber()){
			Object num = ((Double)targetNode.asDouble()).
			if(targetNode.isNumber())return targetNode.asByte();
			if(targetNode.is())return targetNode.as();
			if(targetNode.isFloat())return targetNode.asDouble();
			if(targetNode.isLong())return targetNode.asLong();
		}
			
		throw new UnsupportedOperationException("json node type not recognised:"+targetNode);
	}*/
	/**
	 * Recursive findNode(), which returns the "value" object
	 * @param node
	 * @param fullPath
	 * @return
	 */
	public static Json findValue(Json node, String fullPath) {
		node=findNode(node, fullPath);
		if(node==null)return null;
		
		return node.at("value");
	}
	/**
	 * Recursive findNode()
	 * @param node
	 * @param fullPath
	 * @return
	 */
	public static Json findNode(Json node, String fullPath) {
		String[] paths = fullPath.split("\\.");
		//Json endNode = null;
		for(String path : paths){
			logger.debug("findValue:"+path);
			node = node.at(path);
			if(node==null)return null;
		}
		return node;
	}
	
	/**
	 * Recursive addNode()
	 * Same as findNode, but will make a new node if any node on the path is empty
	 * @param node
	 * @param fullPath
	 * @param value 
	 * @return
	 */
	public static Json addNode(Json node, String fullPath) {
		String[] paths = fullPath.split("\\.");
		Json lastNode=node;
		for(String path : paths){
			logger.debug("findValue:"+path);
			node = node.at(path);
			if(node==null){
				node = lastNode.set(path,Json.object());
				node = node.at(path);
			}
			lastNode=node;
		}
		return node;
	}
	
	/*public static Json addVesselToArray(Json arrayNode, String vesselName){
		Json newNode = arrayNode.objectNode();
		Json vesselNode = ((ObjectNode)newNode).objectNode();
		 arrayNode.put(vesselName, vesselNode);
		 //arrayNode.put(newNode);
		 return vesselNode;
	}*/

	

	public static Json putWith(Json node, String fullPath, Object value) {
		return putWith(node, fullPath,value,JsonConstants.SELF);
		
	}
	public static Json putWith(Json node, String fullPath, Object value, String source) {
		return putWith(node,fullPath,value, source, new DateTime());
	}
	public static Json putWith(Json node, String fullPath, Object value, String source, DateTime dateTime) {
		node=addNode(node,fullPath);
		
		node.set("value",value);
		node.set("timestamp",dateTime.toDateTime(DateTimeZone.UTC).toString(fmt));
		node.set("source",source);
		
		return node;
		
	}
	
	/**
	 * Iterate through the object and remove all keys starting with _
	 * @param mainNode
	 * @return
	 */
	public static Json safe(Json mainNode) {
		Iterator<String> fieldNames = mainNode.asMap().keySet().iterator();
		while (fieldNames.hasNext()) {

			String fieldName = fieldNames.next();
			logger.debug("Merge " + fieldName);
			// if field exists and starts with _, delete it
			if(fieldName.startsWith("_")){
				mainNode.delAt(fieldName);
			}else{
				Json json = mainNode.at(fieldName);
				if (json != null && json.isObject()) {
					safe(json);
				} 
			}
		}
		return mainNode;
	}
	
	/**
	 * Creates an empty root node, with vessels.ownboat structure.
	 * @return
	 */
	public static Json getEmptyRootNode(){
		Json tempRootNode = Json.object().set(JsonConstants.VESSELS,Json.object().set(JsonConstants.SELF,Json.object()));
		return tempRootNode;
	}
}
