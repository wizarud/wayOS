package com.wayos.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wayos.Configuration;
import com.wayos.PathStorage;

public class ConsoleUtil {
		
	static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	static final DateFormat timestampFormat = new SimpleDateFormat("HH:mm:ss.SSSSSS");
	
	private final PathStorage storage;
	
	public ConsoleUtil(PathStorage storage) {
		
		this.storage = storage;
	}
	
	public JSONArray sessionIdList(String accountId, String botId, String channel) {
		
		String resourcePath = Configuration.VARS_PATH + accountId + "/" + botId + "/" + channel + "/";
		
		List<String> objectList = storage.listObjectsWithPrefix(resourcePath);
		
		JSONArray array = new JSONArray();
		
		for (String object:objectList) {
		
			if (object.equals(resourcePath)) continue;
			
			/**
			 * For GCP Storage
			 */
			if (object.contains("/")) {
				
				object = object.substring(resourcePath.length(), object.lastIndexOf(".json"));
				
			} else {
				
				object = object.substring(0, object.lastIndexOf(".json"));				
				
			}
			
			array.put(object);
		}
		
		return array;
	}
		
	public String nowString() {
		
		return dateFormat.format(new Date());
	}
	
	public String timestampString() {
		
		return timestampFormat.format(new Date());
	}
	
	public String timestampString(Long timestamp) {
		
		return timestampFormat.format(new Date(timestamp));
	}
	
	public JSONArray dateList(String accountId, String botId) {
		
		String resourcePath = Configuration.LOGS_PATH + accountId + "/" + botId + "/";
		
		List<String> objectList = storage.listObjectsWithPrefix(resourcePath);
		
		/**
		 * Descending date
		 */
		Collections.sort(objectList, Collections.reverseOrder());
		
		JSONArray array = new JSONArray();
		
		for (String object:objectList) {
		
			if (object.equals(resourcePath)) continue;
			
			/**
			 * For GCP Storage
			 */
			if (object.contains("/")) {
				
				object = object.substring(resourcePath.length(), object.lastIndexOf("/"));				
				
			}
			
			array.put(object);
		}
		
		return array;
	}	
	
	/**
	 * Log vars
	 * values
	 * 
	 * @param timestamp
	 * @param accountId
	 * @param botId
	 * @param channel
	 * @param sessionId
	 * @param message
	 */
	public void appendVars(Long timestamp, String accountId, String botId, String channel, String sessionId, String values, String delimeter) {
		
		String resourcePath = Configuration.LOGS_PATH + accountId + "/" + botId + "/" + nowString() + "/vars.json";
		
		JSONObject lastLogJSON = storage.readAsJSONObject(resourcePath);
		
		String records = channel + delimeter + sessionId + delimeter + values;
		
		if (lastLogJSON==null) {
			
			lastLogJSON = new JSONObject();
		}
		
		if (timestamp!=null) {
			
			lastLogJSON.put(timestampString(timestamp), records);
			
		} else {
			
			lastLogJSON.put(timestampString(), records);
			
		}
		
		
		storage.write(lastLogJSON.toString(), resourcePath);
		
	}
	
	public JSONObject readVarsFromDate(String accountId, String botId, String dateString) {
		
		String resourcePath = Configuration.LOGS_PATH + accountId + "/" + botId + "/" + dateString + "/vars.json";
		
		JSONObject logJSON = storage.readAsJSONObject(resourcePath);
		
		if (logJSON==null) {
			
			return new JSONObject();
		}
		
		/*
		 TODO: cannot sort by descending key
		Map<String, Object> logMap = logJSON.toMap();
		
		Map<String, Object> reverseSortedMap = new TreeMap<>(Collections.reverseOrder());
		
		reverseSortedMap.putAll(logMap);
		
		return new JSONObject(reverseSortedMap);
		*/
		
		return logJSON;
	}
	
	/**
	 * Append Log to last date json file and update date json index
	 * @param accountId
	 * @param botId
	 * @param channel
	 * @param sessionId
	 * @param message
	 */
	public void appendMessage(Long timestamp, String accountId, String botId, String channel, String sessionId, String message) {
						
		String resourcePath = Configuration.LOGS_PATH + accountId + "/" + botId + "/" + channel + "/" + sessionId + "/" + nowString() + ".log.json";
		
		JSONObject lastLogJSON = storage.readAsJSONObject(resourcePath);
		
		if (lastLogJSON==null) {
			
			lastLogJSON = new JSONObject();
		}
		
		if (timestamp!=null) {
			
			lastLogJSON.put(timestampString(timestamp), message);
			
		} else {
			
			lastLogJSON.put(timestampString(), message);
			
		}
		
		
		storage.write(lastLogJSON.toString(), resourcePath);
		
	}
	
	public JSONObject readMessagesFromDate(String accountId, String botId, String channel, String sessionId, String dateString) {
		
		String resourcePath = Configuration.LOGS_PATH + accountId + "/" + botId + "/" + channel + "/" + sessionId + "/" + dateString + ".log.json";
		
		JSONObject lastLogJSON = storage.readAsJSONObject(resourcePath);
		
		if (lastLogJSON==null) {
			
			lastLogJSON = new JSONObject();
		}
		
		return lastLogJSON;
	}
	
	public long timestamp() {
		
		return new Date().getTime();
	}
	
	public boolean isEmptyOrExpired(JSONObject message) {
		
		if (message==null) return true;
		
		if (message.optString("message").isEmpty()) return true;
		
		long timestamp = message.getLong("timestamp");
		
		long currentTimestamp = new Date().getTime();
		
		if ((currentTimestamp - timestamp) > 3000) return true;
		
		return false;
		
	}
	
	public JSONObject pickNewer(JSONObject...messages) {
		
		long timestamp = Long.MIN_VALUE;
		
		JSONObject newerMessage = null;
		
		for (JSONObject message:messages) {
			
			if (message==null) continue;
			
			if (message.optLong("timestamp") >= timestamp) {
				
				timestamp = message.optLong("timestamp");
				
				newerMessage = message;
				
			}
			
		}
		
		return newerMessage;		
	}
	
	public JSONObject read(String accountId, String botId, String channel, String dateString) {
		
		String resourcePath = Configuration.MESSAGES_PATH + accountId + "/" + botId + "/" + channel + "/" + dateString + ".message.json";
		
		JSONObject lastMessageJSON = storage.readAsJSONObject(resourcePath);
		
		if (lastMessageJSON==null) {
			
			lastMessageJSON = new JSONObject();
		}
		
		return lastMessageJSON;
	}	
	
	public JSONObject read(String accountId, String botId, String channel, String sessionId, String dateString) {
		
		String resourcePath = Configuration.MESSAGES_PATH + accountId + "/" + botId + "/" + channel + "/" + sessionId + "/" + dateString + ".message.json";
		
		JSONObject lastMessageJSON = storage.readAsJSONObject(resourcePath);
		
		if (lastMessageJSON==null) {
			
			lastMessageJSON = new JSONObject();
		}
		
		return lastMessageJSON;
	}	
	
	/**
	 * Push message to target <accountId>/<botId>/<channel>.
	 * @param accountId
	 * @param botId
	 * @param channel
	 * @param message
	 */
	public void write(String accountId, String botId, String channel, String message) {
		
		String resourcePath = Configuration.MESSAGES_PATH + accountId + "/" + botId + "/" + channel + "/" + nowString() + ".message.json";
		
		JSONObject lastEventJSON = storage.readAsJSONObject(resourcePath);
		
		if (lastEventJSON==null) {
			
			lastEventJSON = new JSONObject();
		}
		
		lastEventJSON.put("timestamp", timestamp());
		lastEventJSON.put("message", message);
		
		storage.write(lastEventJSON.toString(), resourcePath);
		
	}			
	
	/**
	 * Push message to target <accountId>/<botId>/<channel>/<sessionId>.
	 * @param accountId
	 * @param botId
	 * @param channel
	 * @param sessionId
	 * @param message
	 */
	public void write(String accountId, String botId, String channel, String sessionId, String message) {
		
		String resourcePath = Configuration.MESSAGES_PATH + accountId + "/" + botId + "/" + channel + "/" + sessionId + "/" + nowString() + ".message.json";
		
		JSONObject lastEventJSON = storage.readAsJSONObject(resourcePath);
		
		if (lastEventJSON==null) {
			
			lastEventJSON = new JSONObject();
		}
		
		lastEventJSON.put("timestamp", timestamp());
		lastEventJSON.put("message", message);
		
		storage.write(lastEventJSON.toString(), resourcePath);
		
	}	
	
}
