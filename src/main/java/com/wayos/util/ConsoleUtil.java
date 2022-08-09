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
	
	static final DateFormat timestampFormat = new SimpleDateFormat("HH-mm-sss");
	
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
			
			object = object.substring(resourcePath.length(), object.lastIndexOf(".json"));
			
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
			
			object = object.substring(resourcePath.length(), object.lastIndexOf("/"));
			
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
}
