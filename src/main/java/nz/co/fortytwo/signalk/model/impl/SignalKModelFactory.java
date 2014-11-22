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
package nz.co.fortytwo.signalk.model.impl;

import nz.co.fortytwo.signalk.model.SignalKModel;

/**
 * Factory to get signalKModel singleton
 * @author robert
 *
 */
public class SignalKModelFactory {
	private static SignalKModel signalKModel;
	/**
	 * Get the signalKModel singleton
	 * @return
	 */
	public static synchronized SignalKModel getInstance(){
		if(signalKModel==null){
			signalKModel=new SignalKModelImpl();
		}
		return signalKModel;
	}
	/**
	 * Returns a different clean instance - only needed for testing!
	 * @return
	 */
	public static synchronized SignalKModel getCleanInstance(){
		return new SignalKModelImpl();
	}
}
