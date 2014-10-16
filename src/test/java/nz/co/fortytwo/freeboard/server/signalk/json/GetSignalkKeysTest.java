package nz.co.fortytwo.freeboard.server.signalk.json;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import mjson.Json;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GetSignalkKeysTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
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
			if(!"properties".equals(fieldName)){
				String keyName = path+"."+fieldName;
				keyName=keyName.replaceAll(".properties","");
				keyName=keyName.replaceAll(".additionalProperties","");
				String constantName = keyName.replaceAll("\\.", "_");
				constantName=constantName.replaceAll("environment_", "env_");
				constantName=constantName.replaceAll("navigation_", "nav_");
				System.out.println("public static final String "+constantName+ " = \"" + keyName+"\";");
			}
			
			Json json = schema.at(fieldName);
			if (json != null && json.isObject()) {
				extractKeys(json, path+"."+fieldName);
			} 
		
		}
	}

}
