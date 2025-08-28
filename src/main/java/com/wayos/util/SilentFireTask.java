package com.wayos.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import com.wayos.Configuration;

import x.org.json.JSONObject;

public class SilentFireTask extends TimerTask {
	
	public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
	
	private SilentFire repeatSilentFire;	
	
	private String timeExpression;
	
	private String contextName;
		
	private String messageToFire;
	
	private String lastExecute;
	
	private String lastResponseText;
	
	private ZonedDateTime nextExecute;	
	
	private boolean available;
	
	public SilentFireTask(String timeExpression, String contextName, String messageToFire) {
				
		this.timeExpression = timeExpression;
		
		this.contextName = contextName;
				
		this.messageToFire = messageToFire;
				
		this.available = true;		
	}
	
	public static SilentFireTask build(String contextName, JSONObject taskObj) {
		
		String timeExpression = taskObj.getString("interval");
		String messageToFire = taskObj.optString("message");
		
		if (messageToFire==null || messageToFire.isEmpty()) {
			messageToFire = "silent";
		}
		
		String lastExecute = taskObj.optString("lastExecute");
		String lastResponseText = taskObj.optString("lastResponseText");
		
		SilentFireTask silentFireTask = new SilentFireTask(timeExpression, contextName, messageToFire);
		
		silentFireTask.lastExecute = lastExecute;
		silentFireTask.lastResponseText = lastResponseText;		
		
		return silentFireTask;

	}
	
	public String id() {
		
		return contextName;
	}
	
	public String timeExpression() {
		
		return timeExpression;
	}
	
	public SilentFireTask clone() {
		
		SilentFireTask silentFireTask = new SilentFireTask(timeExpression, contextName, messageToFire);
		
		silentFireTask.lastExecute = Instant.now().atZone(ZoneId.systemDefault()).format(dateTimeFormatter);
		silentFireTask.lastResponseText = lastResponseText;
		
		return silentFireTask;
	}
	
	public void stop() {
		
		this.available = false;
		
	}
	
	public void setNextExecute(ZonedDateTime nextExecute) {
		
		this.nextExecute = nextExecute;
		
	}
	
    private final String post(String apiURL, Map<String, String> headerMap, String body) {

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
	
	public JSONObject toJSONObject() {
		
		JSONObject taskObj = new JSONObject();
		
		taskObj.put("interval", timeExpression);
		taskObj.put("message", messageToFire);
				
		if (lastExecute!=null) {
			taskObj.put("lastExecute", lastExecute);
		} else {
			taskObj.put("lastExecute", "-");		
		}
		
		if (lastResponseText!=null) {
			taskObj.put("lastResponseText", lastResponseText);			
		} else {
			taskObj.put("lastResponseText", "-");			
		}
		
		taskObj.put("nextExecute", nextExecute.format(dateTimeFormatter));

		return taskObj;
	}

	@Override
	public void run() {

		try {
			
			if (!available) return;
			
			String apiURL = Configuration.domain + "/webhooks/" + contextName;
			Map<String, String> headerMap = new HashMap<>();
			headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			
			String body = "sessionId=dashboard&message=" + messageToFire;			
			lastResponseText = post(apiURL, headerMap, body);

			System.out.println("Task " + id() + " executed..");			
			
			if (repeatSilentFire!=null) {
				
				repeatSilentFire.register(this.clone());
				
			}
						
		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}

	public void repeatIfFinishBy(SilentFire repeatSilentFire) {
		
		this.repeatSilentFire = repeatSilentFire;		
		
	}

	public ZonedDateTime nextExecute() {
		
		return nextExecute;
	}

}
