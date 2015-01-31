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
package nz.co.fortytwo.signalk.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import mjson.Json;

import org.joda.time.DateTime;

import com.google.common.eventbus.EventBus;

public interface SignalKModel{
		
		/**
		 * Shortcut for this.at(VESSELS).at(SELF)
		 * @return
		 */
		public Json self();
		/**
		 * Merge tempNode into rootNode
		 * @param tempNode
		 * @return
		 */
		public Json merge(Json tempNode);
		
		/**
		 * Merge tempNode into parentNode as a child of parentNode
		 * @param parentNode
		 * @param tempNode
		 * @return
		 */
		public Json mergeAtPath(Json parentNode, String key, Json tempNode);
		
		/**
		 *  Merge tempNode into parentNode as a child of parentPath
		 * @param path
		 * @param key
		 * @param tempNode
		 * @return
		 */
		public Json mergeAtPath(String path, String key, Json tempNode);
		
		/**
		 *  Merge tempNode into parentNode as a child of parentPath, using the last part as the element key.
		 *  eg "vessels.self.environment.wind" add the Json to "environment" as key "wind"
		 * @param path
		 * @param tempNode
		 * @return
		 */
		public Json mergeAtPath(String path,  Json tempNode);
		/**
		 * Get the Json node at "person.address.city"
		 * @param path
		 * @return
		 */
		public Json atPath(String path);
		/**
		 * Get the json node at {"person","address","city"}
		 * Convenient when using CONSTANTS, atPath(PERSON,ADDRESS,CITY)
		 * @param path
		 * @return
		 */
		public Json atPath(String ... path );
		/**
		 * Sets the value, adding "source":"self" and "timestamp":now()
		 * @param key
		 * @param value
		 * @return
		 */
		public Json setKey(String key, Object value);
		
		/**
		 * Sets the value, adding "source":"self"
		 * @param key
		 * @param value
		 * @param timestamp
		 * @return
		 */
		public Json setKey(String key,Object value, DateTime timestamp);
		
		/**
		 * Sets the value, adding "timestamp":now()
		 * @param key
		 * @param value
		 * @param source
		 * @return
		 */
		public Json setKey(String key,Object value, String source);
		
		/**
		 * 
		 * Set the value, timestamp and source
		 * @param key
		 * @param value
		 * @param timestamp
		 * @param source
		 * @return
		 */
		public Json setKey(String key,Object value, DateTime timestamp, String source);

		/**
		 * Delete the key from the parent path
		 * @param path
		 * @param key
		 */
		public void delete(String path, String key) ;
		/**
		 * Delete the key from the parent node
		 * @param parentNode
		 * @param key
		 */
		public void delete(Json parentNode, String key) ;
		
		/**
		 * Return a Json node which is a deep copy of the signalk model root node
		 * @return
		 */
		public SignalKModel duplicate();
		
		/**
		 * Return a Json node which is a deep copy of the signalk model root node
		 * and has the keys starting with _ removed.
		 * @return
		 */
		public SignalKModel safeDuplicate();
		public ConcurrentHashMap<String, Json> getNodeMap();
		/**
		 * Recursive addNode()
		 * Same as findNode, but will make a new node if any node on the path is empty
		 * Assumes path is relative from 'node'.
		 * @param node
		 * @param fullPath
		 * @param value 
		 * @return
		 */
		public  Json addNode(Json node, String fullPath) ;
		public  Json putWith(Json node, String fullPath, Object value);
		public  Json putWith(Json node, String fullPath, Object value, String source);
		public  Json putWith(Json node, String fullPath, Object value, String source, DateTime dateTime);
		
		
		/**
		 * Recursive addNode()
		 * Same as findNode, but will make a new node if any node on the path is empty
		 * Assumes path is relative from root node
		 * @param fullPath
		 * @return
		 */
		public  Json addNode(String fullPath) ;
		public  Json putWith(String fullPath, Object value);
		public  Json putWith(String fullPath, Object value, String source);
		public  Json putWith(String fullPath, Object value, String source, DateTime dateTime);
		/**
		 * Recursive findNode(), which returns the "value" object
		 * @param fullPath
		 * @return
		 */
		public Json findValue( String fullPath) ;
		/**
		 * Recursive findNode()
		 * @param fullPath
		 * @return
		 */
		public Json findNode( String fullPath) ;
		
		/**
		 * Recursive findNode(), which returns the "value" object
		 * @param node
		 * @param fullPath
		 * @return
		 */
		public Json findValue(Json node, String fullPath) ;
		/**
		 * Recursive findNode()
		 * @param node
		 * @param fullPath
		 * @return
		 */
		public Json findNode(Json node, String fullPath) ;
		
		public EventBus getEventBus();
	
		/**
		 * Retrieve the node by fullPath.
		 * @param node
		 */
		public Json getFromNodeMap(String fullPath) ;
		
		/**
		 * Get a full list of paths.
		 * @param node
		 */
		public Set<String> getFullPaths() ;
		
		/**
		 * Remove the node by fullPath.
		 * @param node
		 */
		public void removeFromNodeMap(String fullPath) ;
		/**
		 * Adds the given node to the nodeMap, so we can retrieve by fullPath.
		 * @param node
		 */
		public void addToNodeMap(Json node) ;

}
