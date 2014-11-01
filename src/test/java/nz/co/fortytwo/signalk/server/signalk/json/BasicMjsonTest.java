package nz.co.fortytwo.signalk.server.signalk.json;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import mjson.Json;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BasicMjsonTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	public Json at(Json json, String key){
		String[] keys = key.split("\\.");
		for(String k:keys){
			json=json.at(k);
			if(json==null) return null;
		}
		return json;
	}
	
	public void printKeys(Json json, String prev){
		try{
		for(String key:json.asJsonMap().keySet()){
			System.out.println(prev+key);
			if(json.at(key) != null){
				printKeys(json.at(key), prev+key+".");
			}
		}
		}catch(UnsupportedOperationException e){}
		
	}
	@Test
	public void test() throws URISyntaxException, IOException {
		//Json.Schema schema = Json.schema(new URI("https://raw.githubusercontent.com/SignalK/specification/master/schemas/signalk.json"));
		Json basicNav = Json.read(FileUtils.readFileToString(new File("./src/test/resources/samples/basic_nav.json")));
		System.out.println(basicNav.toString());
		System.out.println(basicNav.at("vessels").at("motu").at("navigation").at("position").toString());
		Json newNav = basicNav.dup();
		newNav.at("vessels").at("motu").at("navigation").at("position").at("altitude").set("value", 23.6);
		System.out.println(at(newNav,"vessels.motu.navigation.position").toString());
		basicNav.with(newNav);
		System.out.println(at(basicNav,"vessels.motu.navigation.position.altitude").toString());
		System.out.println(at(basicNav,"vessels.motu.navigation.position").toString());
		printKeys(basicNav, "");
		//fail("Not yet implemented");
	}

}
