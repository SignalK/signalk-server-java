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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package nz.co.fortytwo.signalk.server.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mjson.Json;
import net.minidev.json.JSONArray;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.jsonpath.JsonPath;

public class GenerateN2KMappings {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws IOException {
		File schemaFile = new File("./src/test/resources/samples/n2kMappings.json");
		String schemaString = FileUtils.readFileToString(schemaFile);
		
		//System.out.println(schemaString);
		Json mappingJson = Json.read(schemaString);
		System.out.println(mappingJson);
		List<String> keyList = new ArrayList<String>();
		
	}
	
	@Test
	public void testSimpleJsonPath() throws Exception {
		File schemaFile = new File("./src/test/resources/samples/n2kMappings.json");
		String schemaString = FileUtils.readFileToString(schemaFile);
		Json mappings = Json.read(schemaString);
		String json = "{\"timestamp\":\"2013-10-08-16:04:06.060\",\"prio\":\"6\",\"src\":\"1\",\"dst\":\"255\",\"pgn\":\"128275\",\"description\":\"Distance Log\",\"fields\":{\"Log\":\"2229808\",\"Trip Log\":\"4074\"}}";
		json=json.replace(" ", "_");
		String pgn = JsonPath.read(json,"$.pgn");
		System.out.println("Pgn = "+pgn);
		Json mapping = mappings.at(pgn);
		for(Json map : mapping.asJsonList()){
			System.out.println("map = "+map);
			String var = JsonPath.read(json,map.at("source").asString());
			System.out.println(map.at("node").asString()+":"+var);
		}
	
	}
	
	@Test
	public void testComplexJsonPath() throws Exception {
		String json = "{\"timestamp\":\"2014-08-15-18:00:10.005\",\"prio\":\"2\",\"src\":\"160\",\"dst\":\"255\",\"pgn\":\"129026\",\"description\":\"COG & SOG, Rapid Update\",\"fields\":{\"COG_Reference\":\"True\",\"COG\":\"206.1\",\"SOG\":\"3.65\"}}";
		//"source": "COG",
        //"node": "navigation.courseOverGroundTrue",
        //"filter": "n2k.fields[COGReference]===True"
		JSONArray cogTrue = JsonPath.read(json,"$.fields[?(@.COG_Reference=='True' && @.COG)].COG");
		System.out.println("COGTrue:"+cogTrue.toJSONString());
	
	}

}
