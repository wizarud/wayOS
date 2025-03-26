package com.wayos.command.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.wayos.Configuration;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.util.SignatureValidator;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
@SuppressWarnings("serial")
public class LoadDataCommandNode extends CommandNode {
	    
	private SignatureValidator signatureValidator;

    public LoadDataCommandNode(Session session, String [] hooks) {
        super(session, hooks);
        
        this.signatureValidator = new SignatureValidator(Configuration.brainySecret.getBytes());
    }

    @Override
    public String execute(MessageObject messageObject) {
        try {
            session.context().load();
            
            /**
             * Forward reload command to Microservices if exists, Must be call from GAE only!
             */
            String storageBucket = "";
            
            if (storageBucket.endsWith(".appspot.com")) {
            	
                String webHookURL = session.context().prop("webHookURL");
                
                if (webHookURL!=null) {
                	
                    Executors.newFixedThreadPool(1).execute(new Runnable() {

						@Override
						public void run() {
							
		                	String message = messageObject.toString();
		                    
		                    Map<String, String> headerMap = new HashMap<>();
		                    
		                    String signed = signatureValidator.generateSignature(message.getBytes());
		                    
		                    headerMap.put("Brainy-Signature", "555" + signed);
		                    
		                    post(webHookURL, headerMap, "message=" + message + "&sessionId=" + session.vars("#sessionId"));
							
						}
                    	
                    });                    
                	                	
                }
            	
            }
                                    
            return successMsg();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return failMsg();
    }
    
    protected final String post(String apiURL, Map<String, String> headerMap, String body) {

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
            response.append(e.getMessage());
        }
        return response.toString().trim();
    }    
}
