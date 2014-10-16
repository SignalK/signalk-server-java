package nz.co.fortytwo.freeboard.signalk.impl;

import mjson.Json;
import nz.co.fortytwo.freeboard.server.util.JsonConstants;
import nz.co.fortytwo.freeboard.server.util.Util;
import nz.co.fortytwo.freeboard.signalk.SignalKModel;

import org.joda.time.DateTime;

public class SignalKModelImpl  implements SignalKModel{
	
	private Json rootNode;
	
	protected SignalKModelImpl(){
		rootNode=Util.getEmptyRootNode();
	}
	
	public Json self(){
		return rootNode.at(JsonConstants.VESSELS).at(JsonConstants.SELF);
	}
	
	/**
	 * Merge tempNode into rootNode
	 * @param tempNode
	 * @return
	 */
	public Json merge(Json tempNode){
		return Util.merge(rootNode, tempNode);
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
			Util.merge(newNode, tempNode);
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
		Json parentNode = Util.findNode(rootNode, path);
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
			Json parentNode = Util.findNode(rootNode, path);
			return mergeAtPath(parentNode, key, tempNode);
		}else{
			return mergeAtPath(rootNode, path, tempNode);
		}
		
	}
	/**
	 * Get the Json node at "person.address.city"
	 * @param path
	 * @return
	 */
	public Json atPath(String path){
		return Util.findNode(rootNode, path);
	}
	/**
	 * Get the json node at {"person","address","city"}
	 * Convenient when using CONSTANTS, atPath(PERSON,ADDRESS,CITY)
	 * @param path
	 * @return
	 */
	public Json atPath(String ... path ){
		Json json=rootNode;
		for(String k:path){
			json=Util.findNode(json, k);
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
		return setKey(key,value, new DateTime(), "self");
	}
	
	/**
	 * Sets the value, adding "source":"self"
	 * @param key
	 * @param value
	 * @param timestamp
	 * @return
	 */
	public Json setKey(String key,Object value, DateTime timestamp){
		return setKey(key,value, timestamp, "self");
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
		json.set("timestamp",timestamp);
		json.set("source",source);
		return json.set("value",value);
	}

	/**
	 * Delete the key from the parent path
	 * @param path
	 * @param key
	 */
	public void delete(String path, String key) {
		Json parentNode = Util.findNode(rootNode, path);
		delete(parentNode, key);
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
		return rootNode.dup();
	}
	
	/**
	 * Return a Json node which is a deep copy of the signalk model root node
	 * and has the keys starting with _ removed.
	 * @return
	 */
	public Json safeDuplicate(){
		return Util.safe(rootNode.dup());
	}
	
	
}
