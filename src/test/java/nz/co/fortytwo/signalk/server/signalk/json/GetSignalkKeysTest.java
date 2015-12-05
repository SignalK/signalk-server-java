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
package nz.co.fortytwo.signalk.server.signalk.json;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import mjson.Json;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class GetSignalkKeysTest {

	private SortedSet<String> strings = new TreeSet<String>();
	private Map<String, String> keys = new TreeMap<String,String>();
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	@Ignore
	public void printKeys() throws IOException{

		getAlarmKeys();
		getCommunicationKeys();
		getEnvironmentKeys();
		getNavigationKeys();
		getPropulsionKeys();
		getResourcesKeys();
		getSensorsKeys();
		getSteeringKeys();
		getTanksKeys();
		for(String str: strings){
			System.out.println(str.toUpperCase()+" = \""+str+"\"");
		}
		for(String keyName: keys.keySet()){
			String constantName = keyName.replaceAll("\\.", "_");
			constantName=constantName.replaceAll("environment_", "env_");
			constantName=constantName.replaceAll("navigation_", "nav_");
			System.out.println(constantName.toUpperCase()+" = \""+keyName+"\"");
		}
	}
	
	public void getEnvironmentKeys() throws IOException {
		String env = FileUtils.readFileToString(new File(("/home/robert/gitrep/specification/schemas/groups/environment.json")));
		//env = env.replaceAll("$","");
		Json schema = Json.read(env);
		System.out.println("public static final String environment = \"environment\";");
		extractKeys(schema, "environment");
	}
	
	
	public void getNavigationKeys() throws IOException {
		String env = FileUtils.readFileToString(new File(("/home/robert/gitrep/specification/schemas/groups/navigation.json")));
		Json schema = Json.read(env);
		System.out.println("public static final String navigation = \"navigation\";");
		extractKeys(schema, "navigation");
	}
	
	
	public void getAlarmKeys() throws IOException {
		String env = FileUtils.readFileToString(new File(("/home/robert/gitrep/specification/schemas/groups/alarms.json")));
		Json schema = Json.read(env);
		System.out.println("public static final String alarms = \"alarms\";");
		extractKeys(schema, "alarms");
	}
	
	
	public void getCommunicationKeys() throws IOException {
		String env = FileUtils.readFileToString(new File(("/home/robert/gitrep/specification/schemas/groups/communication.json")));
		Json schema = Json.read(env);
		System.out.println("public static final String communication = \"communication\";");
		extractKeys(schema, "communication");
	}
	
	
	public void getPropulsionKeys() throws IOException {
		String env = FileUtils.readFileToString(new File(("/home/robert/gitrep/specification/schemas/groups/propulsion.json")));
		Json schema = Json.read(env);
		System.out.println("public static final String propulsion = \"propulsion\";");
		extractKeys(schema, "propulsion");
	}
	
	
	public void getResourcesKeys() throws IOException {
		String env = FileUtils.readFileToString(new File(("/home/robert/gitrep/specification/schemas/groups/resources.json")));
		Json schema = Json.read(env);
		System.out.println("public static final String resources = \"resources\";");
		extractKeys(schema, "resources");
	}
	
	
	public void getSensorsKeys() throws IOException {
		String env = FileUtils.readFileToString(new File(("/home/robert/gitrep/specification/schemas/groups/sensors.json")));
		Json schema = Json.read(env);
		System.out.println("public static final String sensors = \"sensors\";");
		extractKeys(schema, "sensors");
	}
	
	
	public void getSteeringKeys() throws IOException {
		String env = FileUtils.readFileToString(new File(("/home/robert/gitrep/specification/schemas/groups/steering.json")));
		Json schema = Json.read(env);
		System.out.println("public static final String steering = \"steering\";");
		extractKeys(schema, "steering");
	}
	
	
	public void getTanksKeys() throws IOException {
		String env = FileUtils.readFileToString(new File(("/home/robert/gitrep/specification/schemas/groups/tanks.json")));
		Json schema = Json.read(env);
		System.out.println("public static final String tanks = \"tanks\";");
		extractKeys(schema, "tanks");
	}
	
	public void extractKeys(Json schema, String path) {
		
		Iterator<String> fieldNames = schema.asMap().keySet().iterator();
		while (fieldNames.hasNext()) {

			String fieldName = fieldNames.next();
			if(!strings.contains(fieldName)){
				strings.add(fieldName);
			}
			//ignore stuff here
			if("id".equals(fieldName))continue;
			if("title".equals(fieldName))continue;
			if("source".equals(fieldName))continue;
			if("timestamp".equals(fieldName))continue;
			if("description".equals(fieldName))continue;
			if("type".equals(fieldName))continue;
			if("enum".equals(fieldName))continue;
			if("example".equals(fieldName))continue;
			if(fieldName.indexOf("$")>=0)continue;
			if(fieldName.indexOf("patternProperties")>=0)continue;
			if(!"properties".equals(fieldName)){
				String keyName = path+"."+fieldName;
				String type = null;
				if( schema.at(fieldName)!=null && schema.at(fieldName).isObject() && schema.at(fieldName).at("type")!=null){
					type = schema.at(fieldName).at("type").toString();
					type=type.replace('"', ' ').trim();
				}
				if(type==null){
					if( schema.at(fieldName)!=null && schema.at(fieldName).isObject() && schema.at(fieldName).at("$ref")!=null){
						type = schema.at(fieldName).at("$ref").toString();
						if(type.contains("numberValue"))type="number";
						if(type.contains("stringValue"))type="string";
						if(type.contains("version"))type="string";
						if(type.contains("mmsi"))type="string";
						if(type.contains("uuid"))type="string";
						if(type.contains("version"))type="string";
						if(type.contains("floatValue"))type="float";
						if(type.contains("timestamp"))type="timestamp";
						if(type.contains("nullValue"))type="null";
						if(type.contains("alarmValue"))type="alarmValue";
						if(type.contains("alarmMethod"))type="alarmMethod";
					}
				}
				keyName=keyName.replaceAll(".properties","");
				keyName=keyName.replaceAll(".additionalProperties","");
				
				keys.put(keyName, type);
				//System.out.println("public static final String "+constantName.toUpperCase()+ " = \"" + keyName+"\";");
				//System.out.println("public static final "+type+" "+keyName+"=null;");
			}
			
			Json json = schema.at(fieldName);
			if (json != null && json.isObject()) {
				extractKeys(json, path+"."+fieldName);
			} 
		
		}
	}

}
