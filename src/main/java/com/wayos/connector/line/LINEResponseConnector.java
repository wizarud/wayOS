package com.wayos.connector.line;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wayos.connector.ResponseConnector;
import com.wayos.connector.ResponseObject;

public class LINEResponseConnector extends ResponseConnector {
	
	public static final String REPLY_URL = "https://api.line.me/v2/bot/message/reply";
	
	public static final String PUSH_URL = "https://api.line.me/v2/bot/message/push";
	
	public static final String PROFILE_URL = "https://api.line.me/v2/bot/profile";
	
	public LINEResponseConnector(LINEHttpRequestObject requestObject) {
		super(requestObject);
	}
	
	private LINEHttpRequestObject lineHttpRequestObject() {
		return (LINEHttpRequestObject) super.requestObject;
	}

	@Override
	public String execute(ResponseObject responseObject) {
		
    	if (lineHttpRequestObject().getChannelAccessToken()==null) throw new IllegalArgumentException("Missing PageAccessToken");
    	
        JSONArray messageArray = LINEAPI.instance().createMessages(responseObject);
        List<JSONArray> messagesList = LINEAPI.instance().pagination(messageArray);
        StringBuilder logCollector = new StringBuilder();
        StringBuilder response = new StringBuilder();

        try {
        	
        	if (!messagesList.isEmpty()) {
        		
        		JSONArray firstMessage = messagesList.get(0);
                reply(response, firstMessage);
        		
                JSONArray nextMessages;
                for (int i=1; i<messagesList.size(); i++) {
                	nextMessages = messagesList.get(i);
                	push(response, nextMessages);
                	response.append(System.lineSeparator());
                }
        		
        	}
        	
        	
        } catch (Exception e) {
        	
            logCollector.append(e.getMessage());
            //throw new RuntimeException(e + ":" + response + ":" + responseObject.responseText + ":" + messages);
            throw new RuntimeException(e + ":" + response + ":" + messageArray);
        } 
        
        return response.toString();		
		
	}
	
	private void reply(StringBuilder response, JSONArray messages) throws Exception {
		
		try {
			
			URL url = new URL(REPLY_URL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "Bearer {" + lineHttpRequestObject().getChannelAccessToken() + "}");

			JSONObject obj = new JSONObject();
			obj.put("replyToken", lineHttpRequestObject().getReplyToken());
			obj.put("messages", messages);

			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(obj.toString().getBytes("UTF-8"));
			outputStream.flush();
			outputStream.close();

			//int respCode = conn.getResponseCode();  // New items get NOT_FOUND on PUT
			response.append(conn.getResponseMessage());
			
		} catch (Exception e) {
			
			throw new RuntimeException(e.getMessage() + " - " + response);
		}
	}    
	
	private void push(StringBuilder response, JSONArray messages) throws Exception {
		
        URL url = new URL(PUSH_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer {" + lineHttpRequestObject().getChannelAccessToken() + "}");
        
        JSONObject obj = new JSONObject();
        obj.put("to", lineHttpRequestObject().sessionId());
        obj.put("messages", messages);

        OutputStream outputStream = conn.getOutputStream();
        outputStream.write(obj.toString().getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();

		//int respCode = conn.getResponseCode();  // New items get NOT_FOUND on PUT
		response.append(conn.getResponseMessage());
	} 
	

}
