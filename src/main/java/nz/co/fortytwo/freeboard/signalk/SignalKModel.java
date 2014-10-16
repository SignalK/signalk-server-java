package nz.co.fortytwo.freeboard.signalk;

import mjson.Json;

import org.joda.time.DateTime;

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
		public Json duplicate();
		
		/**
		 * Return a Json node which is a deep copy of the signalk model root node
		 * and has the keys starting with _ removed.
		 * @return
		 */
		public Json safeDuplicate();
		
		
	

}
