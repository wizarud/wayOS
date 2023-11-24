package com.wayos.expression;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import com.wayos.MessageObject;
import com.wayos.Session;

public class RESTHTTPExpression extends HTTPExpression {

    private final String method;

    public RESTHTTPExpression(String method, Session session, String[] arguments) {
        super(session, arguments);
        this.method = method;
    }
    
    @Override
    public String execute(MessageObject messageObject) {

        String [] args = parameterized(messageObject, arguments);

        Map<String, String> headerMap = signatureMap(messageObject);

        if (args.length==4) {

            String url = super.unsecureURLSupport(args[3]);

            headerMap.putAll(createParamMapFromQueryString(args[1]));
            
            String body = args[2];

            messageObject.addResult(request(url, headerMap, body));
            
            return "";

        } else if (args.length==3) {

            String url = super.unsecureURLSupport(args[2]);
            
            String body = args[1];
            
            messageObject.addResult(request(url, headerMap, body));
            
            return "";
        }

        return super.execute(messageObject);
    }

    protected final String request(String apiURL, Map<String, String> headerMap, String body) {

        StringBuffer response = new StringBuffer();
        try {
            System.out.println("POSTING URL:" + apiURL);
            URL url = new URL(apiURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setDoOutput(true);
			conn.addRequestProperty("User-Agent", "Mozilla/4.0");//For Special Connection Ex. Bitkub
			conn.addRequestProperty("User-Agent", "wayOS");
            
            if (headerMap!=null) {
                for (Map.Entry<String, String> entry:headerMap.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                    System.out.println("POSTING Header:" + entry.getKey() + ": " + entry.getValue());
                    
                }
            }
            
            System.out.println("POSTING Content:" + body);
            
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
