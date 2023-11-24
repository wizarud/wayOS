package com.wayos.expression;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.wayos.MessageObject;
import com.wayos.Session;

public class GetHTTPExpression extends HTTPExpression {

    public GetHTTPExpression(Session session, String[] arguments) {
        super(session, arguments);
    }

    @Override
    public String execute(MessageObject messageObject) {

        String [] args = parameterized(messageObject, arguments);

        Map<String, String> headerMap = signatureMap(messageObject);

        if (args.length==3) {

            String url = super.unsecureURLSupport(args[2]);

            headerMap.putAll(createParamMapFromQueryString(args[1]));

            messageObject.addResult(get(url, headerMap));
            
            return "";

        } else if (args.length==2) {

            String url = super.unsecureURLSupport(args[1]);

            messageObject.addResult(get(url, headerMap));
            
            return "";
        }

        return super.execute(messageObject);
    }

    protected final String get(String apiURL, Map<String, String> headerMap) {
    	
        StringBuffer response = new StringBuffer();
        
        try {
        	
            URL url = new URL(apiURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.addRequestProperty("User-Agent", "Mozilla/4.0");
            
            if (headerMap!=null) {
            	
                for (Map.Entry<String, String> entry:headerMap.entrySet()) {
                	
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                    
                }
                
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
            	
                response.append(line);
                response.append(System.lineSeparator());
                
            }
            
            reader.close();

        } catch (Exception e) {
        	
        	e.printStackTrace();
        	
            response.append(e.getMessage());
        }
        
        return response.toString().trim();
    }
}
