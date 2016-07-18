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

import static nz.co.fortytwo.signalk.util.ConfigConstants.MAP_DIR;
import static nz.co.fortytwo.signalk.util.ConfigConstants.STATIC_DIR;

import static nz.co.fortytwo.signalk.util.SignalKConstants.dot;
import static nz.co.fortytwo.signalk.util.SignalKConstants.name;
import static nz.co.fortytwo.signalk.util.SignalKConstants.resources;
import static nz.co.fortytwo.signalk.util.SignalKConstants.resources_charts;
import static nz.co.fortytwo.signalk.util.SignalKConstants.routes;
import static nz.co.fortytwo.signalk.util.SignalKConstants.value;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.http.HttpMessage;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.restlet.data.MediaType;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.InputRepresentation;

import mjson.Json;
import nz.co.fortytwo.signalk.util.SignalKConstants;
import nz.co.fortytwo.signalk.util.Util;
import nz.co.fortytwo.signalk.util.ZipUtils;

public class UploadProcessor extends SignalkProcessor implements Processor {
	private static Logger logger = LogManager.getLogger(UploadProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {

		logger.debug("UploadProcessor starts");
		HttpServletRequest request = exchange.getIn(HttpMessage.class).getRequest();
		logger.debug("Session = " + request.getSession().getId());
		HttpSession session = request.getSession();
		if (logger.isDebugEnabled()) {

			logger.debug("Request = " + exchange.getIn().getHeader(Exchange.HTTP_SERVLET_REQUEST).getClass());
			logger.debug("Session = " + session.getId());
		}

		if (session.getId() != null) {

			String remoteAddress = request.getRemoteAddr();
			String localAddress = request.getLocalAddr();
			if (Util.sameNetwork(localAddress, remoteAddress)) {
				exchange.getIn().setHeader(SignalKConstants.MSG_TYPE, SignalKConstants.INTERNAL_IP);
			} else {
				exchange.getIn().setHeader(SignalKConstants.MSG_TYPE, SignalKConstants.EXTERNAL_IP);
			}
			if (exchange.getIn().getHeader(Exchange.HTTP_METHOD).equals("POST")) {
				 processUpload(exchange);
			}
		} else {
			exchange.getIn().setHeader("Location", SignalKConstants.SIGNALK_AUTH);
			exchange.getIn().setBody("Authentication Required");
		}
	}

	private void processUpload(Exchange exchange) throws Exception {
		logger.debug("Begin import:"+ exchange.getIn().getHeaders());
		if(exchange.getIn().getBody()!=null){
			logger.debug("Body class:"+ exchange.getIn().getBody().getClass());
		}else{
			logger.debug("Body class is null");
		}
		//logger.debug("Begin import:"+ exchange.getIn().getBody());
		MediaType mediaType = exchange.getIn().getHeader(Exchange.CONTENT_TYPE, MediaType.class);
		InputRepresentation representation = new InputRepresentation((InputStream) exchange.getIn().getBody(), mediaType);
		logger.debug("Found MIME:"+ mediaType+", length:"+exchange.getIn().getHeader(Exchange.CONTENT_LENGTH, Integer.class));
		//make a reply
		Json reply = Json.read("{\"files\": []}");
		Json files = reply.at("files");
		
		try {
			List<FileItem> items = new RestletFileUpload(new DiskFileItemFactory()).parseRepresentation(representation);
			logger.debug("Begin import files:"+items);
			for (FileItem item : items) {
				if (!item.isFormField()) {
					InputStream inputStream = item.getInputStream();
					
					Path destination = Paths.get(Util.getConfigProperty(STATIC_DIR)+Util.getConfigProperty(MAP_DIR)+item.getName());
					logger.debug("Save import file:"+destination);
					long len = Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
					Json f = Json.object();
					f.set("name",item.getName());
					f.set("size",len);
					files.add(f);
					install(destination);
				}
			}
		} catch (FileUploadException | IOException e) {
			logger.error(e.getMessage(),e);
		}
		exchange.getIn().setBody(reply);
	}

	private void install(Path destination) throws Exception {
		if(!destination.toString().endsWith(".zip"))return;
		//unzip here
		logger.debug("Unzipping file:"+destination);
		try{
			File zipFile = destination.toFile();
			String f = destination.toFile().getName();
			f= f.substring(0,f.indexOf("."));
			File destDir = new File(Util.getConfigProperty(STATIC_DIR)+Util.getConfigProperty(MAP_DIR)+f);
			if(!destDir.exists()){
				destDir.mkdirs();
			}
			ZipUtils.unzip(destDir, zipFile);
			logger.debug("Unzipped file:"+destDir);
			//now add a reference in resources
			
			 SAXReader reader = new SAXReader();
		     Document document = reader.read(new File(destDir, "tilemapresource.xml"));
		     
		     String title = document.getRootElement().element("Title").getText();
		     String scale = document.getRootElement().element("scale").getText();
		     double maxRes = 0.0;
		     double minRes = Double.MAX_VALUE;
		     int maxZoom = 0;
		     int minZoom = 99;
		     Element tileSets = document.getRootElement().element("TileSets");
		     for(Object o: tileSets.elements("TileSet")){
		    	 Element e = (Element)o;
		    	 int href = Integer.parseInt(e.attribute("href").getValue());
		    	 maxZoom=Math.max(href, maxZoom);
		    	 minZoom=Math.min(href, minZoom);
		    	 double units = Double.parseDouble(e.attribute("units-per-pixel").getValue());
		    	 maxRes=Math.max(units, maxRes);
		    	 minRes=Math.min(units, minRes);
		     }
		     //now make an entry in resources
		     Json resource = createChartMsg(f, title, scale);
		     inProducer.asyncSendBody(inProducer.getDefaultEndpoint(),resource);
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}
	}
	
	private Json createChartMsg(String f, String title, String scale){
		Json val = Json.object();
		val.set(SignalKConstants.PATH, "charts." + "urn:mrn:signalk:uuid:"+UUID.randomUUID().toString());
		Json currentChart = Json.object();
		val.set(value, currentChart);
		String time = Util.getIsoTimeString();
		time = time.substring(0, time.indexOf("."));
		currentChart.set("identifier", f);
		currentChart.set(name, title);
		currentChart.set("description", title);
		currentChart.set("tilemapUrl", "/"+Util.getConfigProperty(MAP_DIR)+f);
		try{
			int scaleInt = Integer.valueOf(scale);
			currentChart.set("scale", scaleInt);
		}catch(Exception e){
			currentChart.set("scale", 0);
		}
		
		Json values = Json.array();
		values.add(val);

		Json update = Json.object();
		
		update.set(SignalKConstants.values, values);

		Json updates = Json.array();
		updates.add(update);
		Json msg = Json.object();
		msg.set(SignalKConstants.CONTEXT, resources);
		msg.set(SignalKConstants.PUT, updates);
		
		
		if(logger.isDebugEnabled())logger.debug("Created new chart msg:"+msg);
		return msg;
	}

}
