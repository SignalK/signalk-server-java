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

import static nz.co.fortytwo.signalk.server.util.JsonConstants.*;

import java.util.Iterator;
import java.util.Map;

import mjson.Json;
import mjson.Json.ObjectJson;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.event.JsonEvent;
import nz.co.fortytwo.signalk.model.event.JsonEvent.EventType;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.eventbus.EventBus;

public class SignalKModelImpl extends ObjectJson implements SignalKModel{
	
	private static Logger logger = Logger.getLogger(SignalKModelImpl.class);
	private static DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	//private Json this;
	private EventBus eventBus = new EventBus();
	
	protected SignalKModelImpl(){
		super();
		this.set(VESSELS,Json.object().set(SELF,Json.object()));
	}
	
	public Json self(){
		return this.at(VESSELS).at(SELF);
	}
	
	/**
	 * Merge tempNode into this
	 * @param tempNode
	 * @return
	 */
	public Json merge(Json tempNode){
		//only vessels subtree
		if(!(tempNode.has(VESSELS)))return this;
		if(tempNode.asJsonMap().size()>1)return this;
		return merge(this, tempNode);
	}
	
	/**
	 * Merge two json treemaps 
	 * @param mainNode
	 * @param updateNode
	 * @return
	 */
	private Json merge(Json mainNode, Json updateNode) {
		//logger.debug("Merge objects");
		if(updateNode==null)return mainNode;
		if (mainNode != null && mainNode.isArray() && updateNode != null && updateNode.isArray()) {
			//mergeArrays( mainNode, updateNode);
		}
		Iterator<String> fieldNames = updateNode.asMap().keySet().iterator();
		while (fieldNames.hasNext()) {

			String fieldName = fieldNames.next();
			logger.debug("Merge " + fieldName);
			Json json = mainNode.at(fieldName);
			// if field exists and is an embedded object
			if (json != null && json.isArray()) {
				//mergeArrays( json,  updateNode.at(fieldName));
			} else if (json != null && json.isObject()) {
				json = merge(json, updateNode.at(fieldName));
				//eventBus.post(new JsonEvent(mainNode, EventType.EDIT));
			} else {
				if (mainNode.isObject()) {
					// Overwrite field
					Json value = updateNode.at(fieldName);
					//logger.debug(fieldName + "=" + value);
					mainNode.set(fieldName, value);
					eventBus.post(new JsonEvent(mainNode.up(), EventType.EDIT));
					logger.debug(fieldName + "=" + value);
				}
			}

		}

		return mainNode;
	}
	
	/**
	 * Recursive findNode(), which returns the "value" object
	 * @param node
	 * @param fullPath
	 * @return
	 */
	public Json findValue( String fullPath) {
		return findValue(this, fullPath);
	}
	/**
	 * Recursive findNode(), which returns the "value" object
	 * @param node
	 * @param fullPath
	 * @return
	 */
	public Json findValue(Json node, String fullPath) {
		node=findNode(node, fullPath);
		if(node==null)return null;
		
		return node.at(VALUE);
	}
	/**
	 * Recursive findNode()
	 * @param node
	 * @param fullPath
	 * @return
	 */
	public Json findNode(String fullPath) {
		return findNode(this,fullPath);
	}
	/**
	 * Recursive findNode()
	 * @param node
	 * @param fullPath
	 * @return
	 */
	public Json findNode(Json node, String fullPath) {
		String[] paths = fullPath.split("\\.");
		//Json endNode = null;
		for(String path : paths){
			logger.debug("findValue:"+path);
			node = node.at(path);
			if(node==null)return null;
		}
		return node;
	}
	/**
	 * Merge tempNode into parentNode as a child of parentNode
	 * @param parentNode
	 * @param tempNode
	 * @return
	 */
	public Json mergeAtPath(Json parentNode, String key, Json tempNode){
		Json newNode = parentNode.at(key);
		if(newNode==null){
			parentNode.set(key, tempNode);
		}else{
			merge(newNode, tempNode);
		}
		return parentNode;
	}
	
	/**
	 *  Merge tempNode into parentNode as a child of parentPath
	 * @param path
	 * @param key
	 * @param tempNode
	 * @return
	 */
	public Json mergeAtPath(String path, String key, Json tempNode){
		Json parentNode = findNode(this, path);
		return mergeAtPath(parentNode, key, tempNode);
	}
	
	/**
	 *  Merge tempNode into parentNode as a child of parentPath, using the last part as the element key.
	 *  eg "vessels.self.environment.wind" add the Json to "environment" as key "wind"
	 * @param path
	 * @param tempNode
	 * @return
	 */
	public Json mergeAtPath(String path,  Json tempNode){
		if(path.indexOf(".")>0){
			String key=path.substring(path.lastIndexOf(".")+1, path.length());
			path=path.substring(0,path.lastIndexOf("."));
			Json parentNode = findNode(this, path);
			return mergeAtPath(parentNode, key, tempNode);
		}else{
			return mergeAtPath(this, path, tempNode);
		}
		
	}
	/**
	 * Get the Json node at "person.address.city"
	 * @param path
	 * @return
	 */
	public Json atPath(String path){
		return findNode(this, path);
	}
	/**
	 * Get the json node at {"person","address","city"}
	 * Convenient when using CONSTANTS, atPath(PERSON,ADDRESS,CITY)
	 * @param path
	 * @return
	 */
	public Json atPath(String ... path ){
		Json json=this;
		for(String k:path){
			json=findNode(json, k);
			if(json==null) return null;
		}
		return json;
	}
	/**
	 * Sets the value, adding "source":"self" and "timestamp":now()
	 * @param key
	 * @param value
	 * @return
	 */
	public Json setKey(String key, Object value){
		return setKey(key,value, new DateTime(), SELF);
	}
	
	/**
	 * Sets the value, adding "source":"self"
	 * @param key
	 * @param value
	 * @param timestamp
	 * @return
	 */
	public Json setKey(String key,Object value, DateTime timestamp){
		return setKey(key,value, timestamp, SELF);
	}
	
	/**
	 * Sets the value, adding "timestamp":now()
	 * @param key
	 * @param value
	 * @param source
	 * @return
	 */
	public Json setKey(String key,Object value, String source){
		return setKey(key,value, new DateTime(), source);
	}
	
	/**
	 * 
	 * Set the value, timestamp and source
	 * @param key
	 * @param value
	 * @param timestamp
	 * @param source
	 * @return
	 */
	public Json setKey(String key,Object value, DateTime timestamp, String source){
		Json json = atPath(key);
		if(json==null) return null;
		json.set(TIMESTAMP,timestamp.toString());
		json.set(SOURCE,source);
		json.set(VALUE,value);
		eventBus.post(new JsonEvent(json, EventType.EDIT));
		return json;
	}

	/**
	 * Delete the key from the parent path
	 * @param path
	 * @param key
	 */
	public void delete(String path, String key) {
		Json parentNode = findNode(this, path);
		delete(parentNode, key);
		//eventBus.post(new JsonEvent(json, EventType.DEL));
	}
	/**
	 * Delete the key from the parent node
	 * @param parentNode
	 * @param key
	 */
	public void delete(Json parentNode, String key) {
		if(parentNode!=null){
			parentNode.delAt(key);
		}
	}
	
	/**
	 * Return a Json node which is a deep copy of the signalk model root node
	 * @return
	 */
	public Json duplicate(){
		return this.dup();
	}
	
	/**
	 * Return a Json node which is a deep copy of the signalk model root node
	 * and has the keys starting with _ removed.
	 * @return
	 */
	public Json safeDuplicate(){
		return safe(this.dup());
	}

	/**
	 * Iterate through the object and remove all keys starting with _
	 * @param mainNode
	 * @return
	 */
	public Json safe(Json mainNode) {
		Iterator<String> fieldNames = mainNode.asMap().keySet().iterator();
		while (fieldNames.hasNext()) {

			String fieldName = fieldNames.next();
			logger.trace("Safe parse " + fieldName);
			// if field exists and starts with _, delete it
			if(fieldName.startsWith("_")){
				mainNode.delAt(fieldName);
			}else{
				Json json = mainNode.at(fieldName);
				if (json != null && json.isObject()) {
					safe(json);
				} 
			}
		}
		return mainNode;
	}
	
	public  Json addNode(String fullPath) {
		return addNode(this,fullPath);
	}
	public  Json putWith(String fullPath, Object value){
		return putWith(this,fullPath, value);
	}
	public  Json putWith(String fullPath, Object value, String source){
		return putWith(this,fullPath, value, source);
	}
	public  Json putWith(String fullPath, Object value, String source, DateTime dateTime){
		return putWith(this,fullPath, value,source,dateTime);
	}
	/**
	 * Recursive addNode()
	 * Same as findNode, but will make a new node if any node on the path is empty
	 * @param node
	 * @param fullPath
	 * @param value 
	 * @return
	 */
	public  Json addNode(Json node, String fullPath) {
		String[] paths = fullPath.split("\\.");
		Json lastNode=node;
		for(String path : paths){
			logger.debug("findValue:"+path);
			node = node.at(path);
			if(node==null){
				node = lastNode.set(path,Json.object());
				node = node.at(path);
				
			}
			lastNode=node;
		}
		return node;
	}
	
	public  Json putWith(Json node, String fullPath, Object value) {
		return putWith(node, fullPath,value,SELF);
		
	}
	public  Json putWith(Json node, String fullPath, Object value, String source) {
		return putWith(node,fullPath,value, source, new DateTime());
	}
	public  Json putWith(Json node, String fullPath, Object value, String source, DateTime dateTime) {
		node=addNode(node,fullPath);
		
		node.set(VALUE,value);
		node.set(TIMESTAMP,dateTime.toDateTime(DateTimeZone.UTC).toString(fmt));
		node.set(SOURCE,source);
		eventBus.post(new JsonEvent(node, EventType.EDIT));
		return node;
		
	}
	
	
	public EventBus getEventBus() {
		return eventBus;
	}
	
	SignalKModelImpl(Json e) { super(e); }
	

	public Json dup() 
	{ 
		SignalKModelImpl j = (SignalKModelImpl) SignalKModelFactory.getCleanInstance();
	    for (Map.Entry<String, Json> e : object.entrySet())
	    {
	        Json v = e.getValue().dup();
	        v.attachTo(j);
	        j.object.put(e.getKey(), v);
	    }
	    return j;
	}
	
	public boolean equals(Object x)
	{			
		return x instanceof SignalKModelImpl && ((SignalKModelImpl)x).object.equals(object); 
	}
}
