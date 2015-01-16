/*
 * 
 * Copyright (C) 2012-2014 R T Huitema. All Rights Reserved.
 * Web: www.42.co.nz
 * Email: robert@42.co.nz
 * Author: R T Huitema
 * 
 * This file is part of the signalk-server-java project
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.fortytwo.signalk.server.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import mjson.Json;
import mjson.Json.Schema;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GenerateSignalkModel {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {

		File schemaFile = new File("./../specification/schemas/signalk.json");
		String schemaString = FileUtils.readFileToString(schemaFile);
		//System.out.println(schemaString);
		Json schemaJson = Json.read(schemaString);
		//System.out.println("OK");
		List<String> keyList = new ArrayList<String>();
		recurse(schemaJson, "", schemaFile, keyList);
		String [] keys =  keyList.toArray(new String[0]);
		Arrays.sort(keys);
		for(String k : keys){
			print(k);
		}
		
		for(String k :keys){
			printJsonConstants(k);
		}
	}

	private void printJsonConstants(String k) {
		//public static final String env_wind_directionChangeAlarm = "environment.wind.directionChangeAlarm";
		k = k.replace("(^[2-7][0-9]{8,8}$|^[A-F0-9]{8,8}$)", "[ID]");
		k = k.replace("(^[A-Za-z0-9]+$)", "[ID]");
		k=k.replace("vessels.[ID].", "");
		String c = k;
		k=k.replaceAll("[ID]", "UUID");
		c=c.replace('.','_');
		c=c.replace("[ID]","id");
		c=c.replace("navigation", "nav");
		c=c.replace("environment", "env");
		System.out.println("public static final String "+c+ "=\""+k+"\";");
		
	}

	private void recurse(Json schemaJson, String pad, File schemaFile, List<String> keyList) throws IOException {
		if(schemaJson.at("$ref")!=null){
			String src = schemaJson.at("$ref").asString();
			//System.out.println(pad + "ref:" + src);
			if(src.contains("definitions.json#"))return;
			src=src.replace('#',' ').trim();
			File next = new File(schemaFile.getParentFile(),src);
			if(next.exists()){
				Json srcJson = Json.read(FileUtils.readFileToString(next));
				recurse(srcJson, pad, schemaFile, keyList);
			}else{
				System.out.println("   err:Cant find "+next.getAbsolutePath());
			}
			return;
		}
		Json props = schemaJson.at("properties");
		if (props != null) {
			Map<String, Json> map = props.asJsonMap();
			for (String e : map.keySet()) {
				if (e.equals("timestamp"))
					continue;
				if (e.equals("source"))
					continue;
				
				keyList.add(pad +  e);
				if (props.at(e).isObject()) {
					recurse(props.at(e), pad +e+"." , schemaFile, keyList);
				}

			}
		}
		/*Json addProps = schemaJson.at("additionalProperties");
		if (addProps != null) {

			Map<String, Json> map = addProps.asJsonMap();
			for (String e : map.keySet()) {
				if (e.equals("timestamp"))
					continue;
				if (e.equals("source"))
					continue;
				
				keyList.add(pad +  e);
				if (addProps.at(e).isObject()) {
					recurse(addProps.at(e), pad +e+"." , schemaFile, keyList);
				}

			}
		}*/
		Json patternProps = schemaJson.at("patternProperties");
		if (patternProps != null) {
			Map<String, Json> map = patternProps.asJsonMap();
			for (String e : map.keySet()) {
				if (e.equals("timestamp"))
					continue;
				if (e.equals("source"))
					continue;
				keyList.add(pad + e);
				if (patternProps.at(e).isObject()) {
					recurse(patternProps.at(e), pad +e+".", schemaFile, keyList);
				}

			}
		}
		
	}

	private void print(String string) {
		string = string.replace("(^[2-7][0-9]{8,8}$|^[A-F0-9]{8,8}$)", "[ID]");
		string = string.replace("(^[A-Za-z0-9]+$)", "[ID]");
		System.out.println(string);
		
	}

}
