package com.wayos.util;

import java.time.ZonedDateTime;
import java.util.TimerTask;

import com.wayos.Application;
import com.wayos.pusher.PusherUtil;

public class SilentPusherTask extends TimerTask {
	
	private SilentPusher repeatSilentPusher;	
	
	private String cronExpression;
	
	private String contextName;
	
	private String channel;
	
	private String sessionId;	
	
	private String accountId;
	
	private String botId;
	
	private String messageToFire;
	
	private boolean repeat;
	
	public SilentPusherTask(String cronExpression, String contextName, String channel, String sessionId, String messageToFire) {
				
		this.cronExpression = cronExpression;
		
		this.contextName = contextName;
		
		this.channel = channel;
		
		this.sessionId = sessionId;
		
		this.messageToFire = messageToFire;
		
		String [] tokens = this.contextName.split("/");
		
		this.accountId = tokens[0];
		
		this.botId = tokens[1];
		
		this.repeat = true;		
	}
	
	public String id() {
		
		return contextName.replace("/", ".") + "." + channel + "." + sessionId + "." + messageToFire;
	}
	
	public String cronExpression() {
		
		return cronExpression;
	}
	
	public SilentPusherTask clone() {
		
		return new SilentPusherTask(cronExpression, contextName, channel, sessionId, messageToFire);
	}
	
	public void stop() {
		
		this.repeat = false;
		
	}

	@Override
	public void run() {
				
		try {
			
			System.out.println("Task " + id() + " executed..");
			
			PusherUtil pusherUtil = Application.instance().get(PusherUtil.class);
			
			pusherUtil.parse(accountId, botId, channel, sessionId, messageToFire);
			
			if (repeat && repeatSilentPusher!=null) {
				
				ZonedDateTime next = repeatSilentPusher.register(this.clone());
				
				System.out.println(id() + " will execute at " + next);
				
			}
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}

	public void repeatIfFinishBy(SilentPusher repeatSilentPusher) {
		
		this.repeatSilentPusher = repeatSilentPusher;		
		
	}

}
