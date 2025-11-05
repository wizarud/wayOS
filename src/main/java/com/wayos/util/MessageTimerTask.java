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

public class MessageTimerTask extends TimerTask {
	
	public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
	
	private MessageTimer repeatSilentFire;	
	
	private String timeExpression;
	
	private String contextName;
	
	private String sessionId;
	
	private String messageToFire;
	
	private String lastExecute;
	
	private String lastResponseText;
	
	private ZonedDateTime nextExecute;	
	
	private boolean available;
	
	public MessageTimerTask(String timeExpression, String contextName, String sessionId, String messageToFire) {
				
		this.timeExpression = timeExpression;
		
		this.contextName = contextName;
		
		this.sessionId = sessionId;
				
		this.messageToFire = messageToFire;
				
		this.available = true;		
	}
	
	public static MessageTimerTask build(String contextName, JSONObject taskObj) {
		
		String timeExpression = taskObj.getString("interval");
		
		String sessionId = taskObj.optString("sessionId");
		if (sessionId==null || sessionId.isEmpty()) {
			sessionId = "dashboard";
		}
		
		String messageToFire = taskObj.optString("message");		
		if (messageToFire==null || messageToFire.isEmpty()) {
			messageToFire = "silent";
		}
		
		String lastExecute = taskObj.optString("lastExecute");
		String lastResponseText = taskObj.optString("lastResponseText");
		
		MessageTimerTask messageTimerTask = new MessageTimerTask(timeExpression, contextName, sessionId, messageToFire);
		
		messageTimerTask.lastExecute = lastExecute;
		messageTimerTask.lastResponseText = lastResponseText;		
		
		return messageTimerTask;

	}
	
	public String id() {
		
		return contextName;
	}
	
	public String timeExpression() {
		
		return timeExpression;
	}
	
	public MessageTimerTask clone() {
		
		MessageTimerTask messageTimerTask = new MessageTimerTask(timeExpression, contextName, sessionId, messageToFire);
		
		messageTimerTask.lastExecute = Instant.now().atZone(ZoneId.systemDefault()).format(dateTimeFormatter);
		messageTimerTask.lastResponseText = lastResponseText;
		
		return messageTimerTask;
	}
	
	public void stop() {
		
		this.available = false;
		
	}
	
	public void setNextExecute(ZonedDateTime nextExecute) {
		
		this.nextExecute = nextExecute;
		
	}
	
	public JSONObject toJSONObject() {
		
		JSONObject taskObj = new JSONObject();
		
		taskObj.put("interval", timeExpression);
		
		taskObj.put("sessionId", sessionId);
		
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
			
			lastResponseText = API.call(Configuration.api_domain, contextName, sessionId, messageToFire);

			System.out.println("Task " + id() + " executed!, Response=" + lastResponseText);			
			
			if (repeatSilentFire!=null) {
				
				repeatSilentFire.register(this.clone());
				
			}
						
		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}

	public void repeatIfFinishBy(MessageTimer repeatSilentFire) {
		
		this.repeatSilentFire = repeatSilentFire;		
		
	}

	public ZonedDateTime nextExecute() {
		
		return nextExecute;
	}

}
