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
package nz.co.fortytwo.signalk.server;

import java.io.FileNotFoundException;
import java.io.IOException;

import nz.co.fortytwo.signalk.util.Util;

import org.apache.log4j.Logger;

public class RouteManagerFactory {

	private static Logger logger = Logger.getLogger(RouteManagerFactory.class);
	static RouteManager manager = null;
	
	public static RouteManager getInstance() throws FileNotFoundException, IOException{
		Util.getConfig();
		if(manager==null){
			
			manager=new RouteManager();
			//must do this early!
			CamelContextFactory.setContext(manager);
			
		}
		return manager;
	}
	
	/**
	 * For testing
	 */
	public static void clear(){
		manager = null;
	}
}
