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
package nz.co.fortytwo.signalk.processor;

import static nz.co.fortytwo.signalk.util.SignalKConstants.LIST;
import static nz.co.fortytwo.signalk.util.SignalKConstants.dot;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels_dot_self;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Multiset.Entry;
import com.google.common.eventbus.Subscribe;

import nz.co.fortytwo.signalk.model.event.PathEvent;
import nz.co.fortytwo.signalk.util.Util;

/**
 * Periodically run through the AIS entries and remove expired.
 * @author robert
 *
 */
public class AisExpiryProcessor extends SignalkProcessor  implements Processor {

	private static Logger logger = LogManager.getLogger(AisExpiryProcessor.class);
	private ConcurrentMap<String, Long> map = new ConcurrentHashMap<>();
	
	
	public AisExpiryProcessor(){
		signalkModel.getEventBus().register(this);
		for(String key: signalkModel.getKeys()){
			processPath(key);
		}
	}
	
	@Override
	public void process(Exchange exchange) throws Exception {
		//event based, just triggers the checking
		//30 min ago.
		long millis = System.currentTimeMillis()-(30*60*1000);
		 Iterator<java.util.Map.Entry<String, Long>> itr = map.entrySet().iterator();
		while(itr.hasNext()){
			java.util.Map.Entry<String, Long> e = itr.next();
			if(millis > e.getValue()){
				//drop the entry
				signalkModel.put(e.getKey(), null, "self.ais.expiry");
				itr.remove();
			}
		}
	}
	
	  /**
     * @param pathEvent the path that was changed
     */
    @Subscribe
    public void recordEvent(PathEvent pathEvent) {
        if (pathEvent == null)
            return;

        String path = pathEvent.getPath();
        processPath(path);
        
    }

	private void processPath(String path) {
		if(StringUtils.isBlank(path)) return;
		if (path.startsWith(vessels + dot)) {
			int p1 = path.indexOf(vessels) + vessels.length() + 1;
			int pos = path.indexOf(".", p1);
			if (pos < 0)return;
			path = path.substring(0, pos);
			if(vessels_dot_self.equals(path))return;
			map.put(path, System.currentTimeMillis());
		}
	}

}
