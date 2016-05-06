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

import java.io.IOException;

import nz.co.fortytwo.signalk.model.event.PathEvent;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;

/**
 * Periodic saves of the signalkModel
 * 
 * @author robert
 * 
 */
public class SaveProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = LogManager.getLogger(SaveProcessor.class);
	private SaveThread saver = new SaveThread();
	
	public SaveProcessor(){
		signalkModel.getEventBus().register(this);
	}
	
	public void process(Exchange exchange) throws Exception {
		//do nothing, event driven
		
	}

	@Subscribe
	public void recordEvent(PathEvent pathEvent) {
		if (pathEvent == null)
			return;
		if (pathEvent.getPath() == null)
			return;
		//if (logger.isTraceEnabled())logger.trace(this.wsSession + " received event " + pathEvent.getPath());

		// do we care?
		saver.startSave();

	}

	@Subscribe
	public void recordEvent(DeadEvent e) {
		logger.debug("Received dead event" + e.getSource());
	}

	
	
	class SaveThread implements Runnable {
		long lastSave = 0;

		Thread t = null;

		public void startSave() {
			if(logger.isDebugEnabled())logger.debug("Checking save..");
			if (t != null && t.isAlive())
				return;
			if(logger.isDebugEnabled())logger.debug("Starting save..");
			t = new Thread(this);
			t.start();
		}

		@Override
		public void run() {
			
			while (true) {
				if (System.currentTimeMillis() - lastSave > 60000) {
					try {
						SignalKModelFactory.save(signalkModel);
						lastSave = System.currentTimeMillis();
					} catch (IOException e) {
						logger.error(e.getMessage());
						logger.debug(e);
					}
					
					break;
				} else {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
			}

		}

		

	}
}
