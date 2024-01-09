package com.wayos.pusher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.wayos.Configuration;
import com.wayos.PathStorage;
import com.wayos.connector.ResponseObject;
import com.wayos.connector.line.LINEAPI;

import x.org.json.JSONArray;
import x.org.json.JSONObject;

public class LinePusher extends Pusher {
	
	public final String apiURL = "https://api.line.me/v2/bot/message/push";	
	
	public LinePusher(PathStorage storage) {
		super(storage);
		
	}
	
	@Override
	public void push(String contextName, String sessionId, String message) {
		
		Configuration configuration = new Configuration(contextName);
		
		String lineJSONPath = configuration.linePath();
		
		JSONObject configObject = storage.readAsJSONObject(lineJSONPath);
		
		if (configObject==null) throw new RuntimeException("Line Config Not Found!");
			
		String channelAccessToken;
		try {
			channelAccessToken = configObject.getString("act");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
        JSONArray messages = LINEAPI.instance().createMessages(new ResponseObject(message));
        
        try {
        	
            URL url = new URL(apiURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer {" + channelAccessToken + "}");
            

            JSONObject obj = new JSONObject();
            obj.put("to", sessionId);
            obj.put("messages", messages);

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(obj.toString().getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            int respCode = conn.getResponseCode();  // New items get NOT_FOUND on PUT
            String line;

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

        } catch (Exception e) {
        	
        	System.err.println("Messages:" + messages);
        	e.printStackTrace();
        } 
		
	}	

}
