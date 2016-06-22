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
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.fortytwo.signalk.processor;

import static nz.co.fortytwo.signalk.util.ConfigConstants.STORAGE_ROOT;
import static nz.co.fortytwo.signalk.util.SignalKConstants.FORMAT_DELTA;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_FORMAT;
import static nz.co.fortytwo.signalk.util.SignalKConstants.dot;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;

import mjson.Json;
import nz.co.fortytwo.signalk.handler.JsonStorageHandler;
import nz.co.fortytwo.signalk.util.JsonSerializer;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.handler.codec.http.multipart.FileUpload;

import com.google.common.collect.Multiset.Entry;

/**
 * Intercept and store/retrieve payloads in the storage system.
 * 
 * @author robert
 * 
 */
public class StorageProcessor extends SignalkProcessor implements Processor {

	private static Logger logger = LogManager.getLogger(StorageProcessor.class);
	private File storageDir = new File(Util.getConfigProperty(STORAGE_ROOT));

	private JsonStorageHandler storageHandler = null;

	public StorageProcessor() throws IOException {
		storageHandler = new JsonStorageHandler();
		// make sure we check and re-attach objects in the storage dir
		if (logger.isDebugEnabled())
			logger.debug("Checking storage objects at:" + storageDir.getAbsolutePath());
		
		Iterator<File> files = FileUtils.iterateFiles(storageDir, new String[] { "json" }, true);
		while (files.hasNext()) {
			File f = files.next();
			if (logger.isDebugEnabled())
				logger.debug("Checking file:" + f);
			Json json = Json.read(FileUtils.readFileToString(f));
			if (json != null && json.has(JsonStorageHandler.PARENT_PATH)) {
				String path = json.at(JsonStorageHandler.PARENT_PATH).asString();
				if (signalkModel.get(path) == null) {
					Map<String, Object> map = json.asMap();
					for(String key:map.keySet()){
						signalkModel.getFullData().put(path+dot+key, map.get(key));
					}
					if (logger.isDebugEnabled())
						logger.debug("Attached storage object at:" + path);
				}
			}
		}
	}

	public void process(Exchange exchange) throws Exception {

		try {
			if (exchange.getIn().getBody() == null)
				return;
			if (logger.isTraceEnabled())
				logger.trace("Processing :" + exchange.getIn());
			if (!(exchange.getIn().getBody() instanceof Json))
				return;
			if (FORMAT_DELTA.equals(exchange.getIn().getHeader(SIGNALK_FORMAT))) {
				Json json = storageHandler.handle(exchange.getIn().getBody(Json.class));
				if (logger.isDebugEnabled())
					logger.debug("Processed storage :" + json);
				exchange.getIn().setBody(json);
			}
			if (logger.isTraceEnabled())
				logger.trace("Outputting :" + exchange.getIn());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
