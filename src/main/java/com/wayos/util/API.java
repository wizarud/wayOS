package com.wayos.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class API {
	
	public static String call(String domain, String contextName, String sessionId, String message) {
		
		String apiURL = domain + "/webhooks/" + contextName;
		
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		
		String body = "sessionId=" + sessionId + "&message=" + message;
		
		String responseText = post(apiURL, headerMap, body);
				
		return responseText;
	}
	
    private static String post(String apiURL, Map<String, String> headerMap, String body) {

        StringBuffer response = new StringBuffer();
        
        try {
        	
            URL url = new URL(apiURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            
            if (headerMap!=null) {
                for (Map.Entry<String, String> entry:headerMap.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());                    
                }
            }
            
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(body.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            int respCode = conn.getResponseCode();  // New items get NOT_FOUND on PUT
            String line;

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append(System.lineSeparator());
            }
            reader.close();

        } catch (Exception e) {
        	e.printStackTrace();
            response.append(/*apiURL + ":" + */ e.getMessage());
        }
        
        return response.toString().trim();
        
    }		

}
