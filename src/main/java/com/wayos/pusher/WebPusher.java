package com.wayos.pusher;

import org.json.JSONObject;

import com.wayos.PathStorage;

public class WebPusher extends Pusher {
	
	public WebPusher(PathStorage storage) {
		
		super(storage);
		

	}
	
	@Override
	public void push(String contextName, String sessionId, String message) {
		
		String [] tokens = contextName.split("/");
		
		String accountId = tokens[0];
		String botId = tokens[1];
		
		/**
		 * TODO: Push to temp, remove if read
		 * temp/<sessionId>/<date>/<timestamp>.json
		 * {
		 * 	accountId: <accountId>
		 *  botId: <botId>
		 *  message: <message>
		 * }
		 */
		
		JSONObject messageObj = new JSONObject();
		
		messageObj.put("accountId", accountId);
		messageObj.put("botId", botId);
		messageObj.put("message", message);
		
		storage.write(messageObj.toString(), "temp/" + sessionId + "/" + System.currentTimeMillis() + ".json");
		
	    
	}

}
