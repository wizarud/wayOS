package com.wayos.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.wayos.Configuration;
import com.wayos.PathStorage;

import x.org.json.JSONArray;
import x.org.json.JSONObject;

public class ConsoleUtil {
		
	static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	static final DateFormat directoryDateFormat = new SimpleDateFormat("yyyy/MM/dd");
	
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
	
	public String directoryNowString() {
		
		return directoryDateFormat.format(new Date());
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
	 * Filter by year and month
	 * @param accountId
	 * @param botId
	 * @param yearAndMonth
	 * @return
	 */
	public JSONArray dateList(String accountId, String botId, String yearAndMonth) {
		
		String directoryYearAndMonth = yearAndMonth.replace("-", "/");
		
		String resourcePath = Configuration.LOGS_PATH + accountId + "/" + botId + "/" + directoryYearAndMonth + "/";
		
		List<String> objectList = storage.listObjectsWithPrefix(resourcePath);
		
		/**
		 * Descending date
		 */
		Collections.sort(objectList, Collections.reverseOrder());
		
		JSONArray array = new JSONArray();
		
		for (String object:objectList) {
			
			if (object.equals(resourcePath)) continue;
			
			if (object.startsWith(".")) continue; //Skip MacOS hidden files
			
			/**
			 * For GCP Storage
			 */
			if (object.contains("/")) {
				
				object = object.substring(resourcePath.length(), object.lastIndexOf("/"));				
				
			}
			
			array.put(yearAndMonth + "-" + object);

		}
				
		/**
		 * At current date if yearAndMonth is now and not exists in array
		 */
		DateFormat yearAndMonthFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		String nowDateString = yearAndMonthFormat.format(new Date());
		
		if (nowDateString.startsWith(yearAndMonth)) {
			
			if (array.length()==0 || 
					!array.getString(0).equals(nowDateString)) {
				
				//Insert to the first position
				JSONArray newArray = new JSONArray();				
				newArray.put(nowDateString);			
				for (int i=0;i<array.length();i++) {
					newArray.put(array.get(i));
				}
				
				return newArray;
			}
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
	public void appendLogVars(Long timestamp, String accountId, String botId, String channel, String sessionId, String values, String delimeter) {
		
		String directoryNowString = directoryNowString();
		
		String timestampString, timestampIndex;
		
		String records = channel + delimeter + sessionId + delimeter + values;
		
		JSONObject json = new JSONObject();
		
		if (timestamp==null) {
			
			timestamp = new Date().getTime();
		}
				
		timestampString = timestampString(timestamp);
		
		json.put(timestampString, records);
		
		String resourcePath = Configuration.LOGS_PATH + accountId + "/" + botId + "/" + directoryNowString + "/" + timestamp + ".json";
				
		storage.write(json.toString(), resourcePath);
		
		/**
		 * TODO: Logging unread!
		 */		
		String unreadPath = Configuration.PRIVATE_PATH + accountId + "/" + botId + ".unread.json";
		
		JSONObject unreadJSON = storage.readAsJSONObject(unreadPath);
		
		if (unreadJSON==null) {
			
			unreadJSON = new JSONObject();
			
		}
		
		String nowString = directoryNowString.replace("/", "-");
		
		JSONArray timestampArray = unreadJSON.optJSONArray(nowString);
		
		if (timestampArray==null) {
		
			timestampArray = new JSONArray();
		
		}
		
		timestampArray.put(timestampString);
		
		unreadJSON.put(nowString, timestampArray);
		
		storage.write(unreadJSON.toString(), unreadPath);		
		
	}
	
	public JSONArray readLogVarsFromDate(String accountId, String botId, String dateString) {
		
		String directoryNowString = dateString.replace("-", "/");
		
		String resourcePath = Configuration.LOGS_PATH + accountId + "/" + botId + "/" + directoryNowString + "/";
		
		List<String> objectList = storage.listObjectsWithPrefix(resourcePath);
		
		/**
		 * Descending date
		 */
		Collections.sort(objectList, Collections.reverseOrder());
		
		JSONArray array = new JSONArray();
		JSONObject json;
		
		for (String object:objectList) {
			
			if (object.equals(resourcePath)) continue;
			
			if (object.startsWith(".")) continue; //Skip MacOS hidden files
			
			if (object.equals("vars.json")) continue; //Skip old version logs
			
			json = storage.readAsJSONObject(Configuration.LOGS_PATH + accountId + "/" + botId + "/" + directoryNowString + "/" + object);
			
			array.put(json);

		}
		
		return array;
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
	
	public JSONArray logsGroupAsYearAndMonth(String accountId, String botId) {
		
		Set<String> yearMonthSet = new HashSet<>();
		
		String resourcePath = Configuration.LOGS_PATH + accountId + "/" + botId + "/";
		
		List<String> yearList = storage.listObjectsWithPrefix(resourcePath);
		
		List<String> monthList;
				
		for (String year:yearList) {
		
			if (year.equals(resourcePath)) continue;
			
			if (year.contains("-")) continue;//Skip old version of logging
			
			if (year.startsWith(".")) continue;//Skip MacOS hidden files
			
			/**
			 * For GCP Storage
			 */
			if (year.contains("/")) {
				
				year = year.substring(resourcePath.length(), year.lastIndexOf("/"));				
				
			}
			
			
			monthList = storage.listObjectsWithPrefix(resourcePath + year + "/");
			
			for (String month:monthList) {
				
				if (month.startsWith(".")) continue;//Skip MacOS hidden files
				
				yearMonthSet.add(year + "-" + month);
				
			}
			
		}
		
		/**
		 * At current year and month
		 */
		DateFormat yearAndMonthFormat = new SimpleDateFormat("yyyy-MM");
		
		String currentYearAndMonth = yearAndMonthFormat.format(new Date());
		
		yearMonthSet.add(currentYearAndMonth);
		
		/**
		 * Convert to List
		 */
		List<String> yearMonthList = new ArrayList<>(yearMonthSet);
				
		/**
		 * Descending date
		 */
		Collections.sort(yearMonthList, Collections.reverseOrder());
		
		
		/**
		 * Pack as JSONArray
		 */
		JSONArray yearMonthArray = new JSONArray();
		
		for (String yearAndMonth:yearMonthList) {
			
			yearMonthArray.put(yearAndMonth);
			
		}
				
		return yearMonthArray;
	}
	
	/**
	 * Unread logging for botId
	 */
	private JSONObject unreadLogVars(String accountId, String botId) {
		
		String unreadPath = Configuration.PRIVATE_PATH + accountId + "/" + botId + ".unread.json";
		
		JSONObject unreadJSON = storage.readAsJSONObject(unreadPath);
		
		if (unreadJSON==null) {
			
			unreadJSON = new JSONObject();
			
		}
		
		return unreadJSON;
		
	}
	
	/**
	* Get all Unread Report Amount from unreadLogVars
	*/
	public int allUnreadLogVarsCount(String accountId, String botId) {
		
		JSONObject unreadJSON = unreadLogVars(accountId, botId);

		int allUnreadCount = 0;
		
		JSONArray timestampArray;
		
		for (String dateString:unreadJSON.keySet()) {
			
			timestampArray = unreadJSON.getJSONArray(dateString);
			
			allUnreadCount += timestampArray.length();
			
		}
		
		return allUnreadCount;
	}
	
	/**
	* Get Unread Report Amount from unreadLogVars filter by dateString
	*/
	public int unreadLogVarsAtDate(String accountId, String botId, String dateString) {
		
		JSONObject unreadJSON = unreadLogVars(accountId, botId);
		
		JSONArray timestampArray = unreadJSON.optJSONArray(dateString);
		
		if (timestampArray!=null) {
			
			return timestampArray.length();
			
		}
		
		return 0;
	}
	
	/**
	 * remove read logs target date string index
	 * @param accountId
	 * @param botId
	 */
	public void removeReadLogVars(String accountId, String botId, String targetDate) {

		String unreadPath = Configuration.PRIVATE_PATH + accountId + "/" + botId + ".unread.json";
		
		JSONObject unreadJSON = storage.readAsJSONObject(unreadPath);
		
		if (unreadJSON==null) {
			
			unreadJSON = new JSONObject();
			
		}
		
		JSONArray timestampArray = unreadJSON.optJSONArray(targetDate);
		
		if (timestampArray==null) return;//Not found targetDate, Do nothing!
		
		unreadJSON.remove(targetDate);
		
		storage.write(unreadJSON.toString(), unreadPath);		
		
	}

}
